package com.example.nhdormmealqr

import org.json.JSONObject

class OkHttpAdapter : HttpRequestInterface {
    private val okHttpHelper = OkhttpHelper()

    override suspend fun get(url: String): JSONObject {
        return okHttpHelper.get(url)
    }

    override suspend fun postForm(url: String, formData: Map<String, String>): JSONObject {
        return okHttpHelper.post(url, formData)
    }
}