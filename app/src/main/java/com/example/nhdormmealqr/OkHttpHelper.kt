package com.example.nhdormmealqr

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Callback
import okhttp3.Call
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OkhttpHelper {
    private var httpClient: OkHttpClient
    private var cookieJar = CookieJar()

    init {
        httpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }

    suspend fun get(url: String) : JSONObject {
        val getRequest = Request.Builder()
            .url(url)
            .get()
            .build()
        return getJsonResponse(getRequest)
    }

    // For now, post only accepts url-encoded body of form data
    suspend fun post(url: String, formData: Map<String, String>): JSONObject {
        val bodyBuilder = FormBody.Builder()
        formData.forEach {(key, value) ->
            bodyBuilder.add(key, value)
        }
        val body = bodyBuilder.build()

        val postRequest = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return getJsonResponse(postRequest)
    }

    private suspend fun getJsonResponse(request: Request): JSONObject {
        return suspendCoroutine { continuation ->
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: java.io.IOException) {
                    continuation.resumeWithException(e) // 실패 시 예외 전달
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string() ?: throw NullPointerException("Response body is null")
                        continuation.resume(JSONObject(body)) // 성공 시 JSON 결과 반환
                    } catch (e: Exception) {
                        continuation.resumeWithException(e) // JSON 파싱 중 예외 처리
                    }
                }
            })
        }
    }

    fun clearCookie() {
        cookieJar.clear()
    }
}