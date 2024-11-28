package com.example.nhdormmealqr

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nhdormmealqr.databinding.ActivityMainBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

const val loginUrl = "https://www.nhschool.co.kr/user/login"
const val qrUrl = "https://www.nhschool.co.kr/mealQRCheck"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
        intializeListeners()

        // id and password is hardcoded for now
        // Should care about the life-cycle as qrCode is only valid for 30s
        // Might have to add a refresh button
        processRequest("id", "password")
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
    private fun intializeListeners() {
        binding.btnLogin.setOnClickListener {
            val id = binding.etUserId.text.toString()
            val password = binding.etPassword.text.toString()
            // processRequest(id, password)
        }
    }

    private fun processRequest(id: String, password: String) {
        // To add a refresh button, it is better to use a cookiejar
        val client = OkHttpClient()

        // Login information
        val body = FormBody.Builder()
            .add("loginId", "yourId")
            .add("loginPwd", "yourPassward")
            .build()
        // POST request
        val request = Request.Builder()
            .url(loginUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        println("Unexpected code $response")
                    } else {
                        val bitmap = getQrCodeBitmap(getQRContent(response))
                        binding.qrCode.post {binding.qrCode.setImageBitmap(bitmap) }
                    }
                }
            }
        }) // End of Http POST request
    }

    fun getQRContent (response: Response) : String {
        val sessionId = extractSessionId(response)
        val authCode = getAuthCode(sessionId)
        val jsonObject = JSONObject(authCode)
        val key = jsonObject.getString("key")
        Log.d("VALUE", key)
        val encoded = encodeURIComponent(key)
        Log.d("VALUE", encoded)
        return encoded
    }

    fun encodeURIComponent(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20") // Replace '+' with '%20' for spaces
            .replace("%21", "!") // Decode specific reserved characters
            .replace("%27", "'")
            .replace("%28", "(")
            .replace("%29", ")")
            .replace("%7E", "~")
    }

    fun getAuthCode(sessionId: String): String {
        val authCode: String

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(qrUrl)
            .addHeader("Cookie", "JSESSIONID=" + sessionId)
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected response code: ${response.code}")
                }
                authCode = response.body?.string()!!
            }
        } catch (e: IOException) {
            throw IOException("Request failed: ${e.message}", e)
        }

        return authCode
    }

    fun getQrCodeBitmap(qrContent: String): Bitmap {
        val size = 236 //pixels
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrContent, BarcodeFormat.QR_CODE, size, size, hints)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

    fun extractSessionId(response: Response): String {
        val cookieInfo = response.headers["Set-Cookie"]!!
        val regex = "JSESSIONID=([A-Z0-9]+);".toRegex()

        val sessionId = regex.find(cookieInfo)
        return sessionId?.groupValues?.get(1)!!
    }
}

