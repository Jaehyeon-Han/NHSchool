package com.example.nhdormmealqr

import android.util.Log
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Test

class OkHttpAdapterTest {
    @Test
    fun getTest() {
        runBlocking {
            val adapter = OkHttpAdapter()
            val url = "https://www.nhschool.co.kr/mealQRCheck"
            val res: JSONObject = adapter.get(url)
            Log.d("TAG", res.toString())
        }
    }
}