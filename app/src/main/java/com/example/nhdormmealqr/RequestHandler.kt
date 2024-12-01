package com.example.nhdormmealqr

import kotlinx.coroutines.flow.first

const val loginUrl = "https://www.nhschool.co.kr/user/login"
const val qrUrl = "https://www.nhschool.co.kr/mealQRCheck"

const val loginResultKey = "process"
const val authCodeKey = "key"

class RequestHandler {
    private val okHttpAdapter = OkHttpAdapter()
    private var lastRequestTimeInMills: Long = 0
    private var loggedIn: Boolean = false

    suspend fun login(id: String, password: String): Boolean {
        val formData: Map<String, String> = mapOf("loginId" to id, "loginPwd" to password)
        val resultJson = okHttpAdapter.postForm(loginUrl, formData)
        if(resultJson[loginResultKey] == true) {
            // Add login data to the DataStore
            LoginHelper.saveLoginInfo(id, password)
            lastRequestTimeInMills = System.currentTimeMillis()
            loggedIn = true
            println("Successfully logged in!")
            return true
        }
        return false
    }

    suspend fun getMealQR() : QrCode {
        println("Log in status: ${loggedIn}")
        if(hasPassed1hour() || !loggedIn) {
            relogin()
        }
        return createMealQr()
    }

    suspend fun relogin() {
        val loginInfo = LoginHelper.getLoginInfo().first()
        val id = loginInfo.id
        val password = loginInfo.password
        login(id!!, password!!)
    }

    private suspend fun getAuthCode(): String {
        val resultJson = okHttpAdapter.get(qrUrl)
        return resultJson[authCodeKey].toString()
    }

    private suspend fun createMealQr(): QrCode {
        println("Creating QR")
        val authCode = getAuthCode()
        return QrCode(UriEncoder.encode(authCode))
    }

    private fun hasPassed1hour(): Boolean {
        val now = System.currentTimeMillis()
        println("Last access: ${lastRequestTimeInMills}, Now: ${now}")
        return (now - lastRequestTimeInMills) > 3_600_000
    }
}