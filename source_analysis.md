# AJAX 통신
```
<script type = "text/javascript">
	var ignore_not_requested_meal = "N";

	setInterval(function(){
		refreshQR();		
	}, 30000);

	function QRPopupClose(){
		ignore_not_requested_meal = "N";
		$(".restaurantQR").hide();
		refreshQR();
	}
	
	function refreshQR(){
		var timestamp = $.now();
		var uno = "" || "";
		
		$('.restaurantQR .QRBox .img').html("");
	
		// 식사 신청 여부 체크
		$.ajax({ 
	        url: '/mealQRCheck',
	        type: "GET",
	        success: function(res) {
	        	var authCode = res.key || '';
	        	if (authCode == '') {
	        		console.log("QR코드를 생성할 수 없습니다. (key)값이 생성되지 않음");
	        	} else {
	        		authCode = encodeURIComponent(authCode);
	    			$('.restaurantQR .QRBox .img').qrcode({width: 236,height: 236,text: authCode});
	    			
	    			if (ignore_not_requested_meal == "Y" || res.userMealRequestedYn == "Y") {
	    				$('.restaurantQR .QRBox .img').show();	
	    			} else {
	    				$('.restaurantQR .QRBox .img').hide();
	    				$('.restaurantQR .QRBox .notRequestedWarn').show();
	    			}
	    			
	        	}
            
	        }, error: function(xhr, status, error){
	        	console.log(error);
	        }
	    });
		
	}
	
	function showNotRequestedMealQR(){
		ignore_not_requested_meal = "Y";
		$('.restaurantQR .QRBox .img').show();
		$('.restaurantQR .QRBox .notRequestedWarn').hide();
	}
	
	refreshQR();
 ```

30초마다 refreshQR()이 실행되고 있으며 refreshQR()의 기능은 다음과 같다.
- json 데이터에서 key를 추출하여 authcode에 저장
- authCode를 URI 인코딩
- 인코딩된 authCode를 QR을 생성 -> 해당하는 html 요소에 설정  

https://www.nhschool.co.kr/mealQRCheck uri로 GET 요청을 보내면 다음과 같은 json 데이터가 반환된다.
1. 로그아웃 상태: `{"logon":"N","key":"","userMealRequestedYn":"","login":"Y"}`
1. 로그인 상태: `{"key":"NHDORM|<yyyy>-<mm>-<dd> <hh>:<mm>:<ss>|<id>|<name>|","userMealRequestedYn":"<Y|N>","login":"Y"}`

로그인은 JSESSIONID cookie로 관리되고 있다. 따라서 정상적으로 qr을 가져오기 위해서는 1) 세션 ID 받기, 2) 로그인 요청, 3) /mealQRCheck 로 서버측 요청, 4) QR 코드 생성, 네 가지 작업을 해주어야 한다.  
json 인코딩을 알고 있으므로 그냥 만들면 되지 않을까도 생각해봤지만 오래된 QR을 사용했을 때 "유효하지 않은 QR입니다"라는 메시지가 나오는 것으로 보아 서버에서 시간을 재고 있는 것으로 보인다. 왜 이런 로직이 필요한지는 모르겠다.  
HTTP 패킷을 송수신하고 쿠키를 처리하는 (즉, 브라우저 역할) 라이브러리와 qr코드를 생성하는 라이브러리를 찾아봐야 한다. qr코드 생성기는 jquery 함수 이외에도 가능한지 실험이 필요하다.

# 로그인
```
<div class="generalForm">
  <form name="loginForm" id = "loginForm" method="post" action="?" onsubmit = "return false;">
    <input type="hidden" name="retUrl" value="" />
    
    <div class="loginInput">
      <p>
        <span>
          <img src="/res/img/user/id-icon.png" alt=""/>
        </span>
        <input title="아이디 입력" placeholder="아이디" type="text" name="loginId" value="" />
      </p>
      
      <p>
        <span>
          <img src="/res/img/user/pw_icon.png" alt=""/>
        </span>
        <input title="비밀번호 입력" placeholder="비밀번호" type="password" name="loginPwd" />
      </p> 
    </div>
    <p class = "error_msg" style = "display:none;"><!-- error message --></p>
    <button class="loginBtn" type="button" onclick = "doLogin()">로그인</button>
  </form>
</div>

<script type = "text/javascript">
			function doLogin(){
				$(".error_msg").hide();
				$(".error_msg").html("");
				
				$.ajax({
					type: "POST",
					url: '?&result=JSON',
					data: $("#loginForm").serialize(),
					async: false,
					success: function(row){
						console.log(row);
						if (row.process) {
							
						} else {
								
						} // debug용 dead code 추정
						
						var message = row.message || '';
						if (message != '') {
							$(".error_msg").show();
							$(".error_msg").html( message );
							alert(message);
						}

						var focusTarget = row.focus || '';
						if (focusTarget == "password") { 
							$("[name=loginPwd]").focus();
						} else if (focusTarget == "id") { 
							$("[name=loginId]").focus();
						}

						var redirectURL = row.redirect || '';
						if (!redirectURL == ""){
							location.href = redirectURL;
						}
					}
				});
			}
		</script>
```
