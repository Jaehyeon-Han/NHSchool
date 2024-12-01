package com.example.nhdormmealqr

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.nhdormmealqr.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding // View binding
    private var requestHandler: RequestHandler = RequestHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
        initializeListeners()
    }

    private fun initializeView() {
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeListeners() {
        binding.btnLogin.setOnClickListener {
            val id = binding.etUserId.text.toString()
            val password = binding.etPassword.text.toString()
            lifecycleScope.launch {
                val isLoggedIn = requestHandler.login(id, password) // suspend 함수 호출
                if (isLoggedIn) {
                    Toast.makeText(this@MainActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                    refreshQr()
                } else {
                    Toast.makeText(this@MainActivity, "ID와 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnRefresh.setOnClickListener {
            refreshQr()
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            if(isLoginIdPresent()) {
                refreshQr()
            }
        }
    }

    private fun refreshQr() {
        lifecycleScope.launch {
            val qrCode = requestHandler.getMealQR()
            binding.qrCode.setImageBitmap(qrCode?.getQrCodeBitmap())
        }
    }

    private suspend fun isLoginIdPresent(): Boolean {
        val loginInfo = LoginHelper.getLoginInfo().first() // Get the first emitted value
        return loginInfo.id != null
    }
}




