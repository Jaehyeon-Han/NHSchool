package com.example.nhdormmealqr

import org.json.JSONObject

interface HttpRequestInterface {
    suspend fun get(url: String) : JSONObject
    suspend fun postForm(url: String, formData: Map<String, String>) : JSONObject
}