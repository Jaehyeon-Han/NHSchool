package com.example.nhdormmealqr

import java.net.URLEncoder

class UriEncoder {
    companion object{
        fun encode(value: String): String {
            return URLEncoder.encode(value, "UTF-8")
                .replace("+", "%20") // Replace '+' with '%20' for spaces
                .replace("%21", "!") // Decode specific reserved characters
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~")
        }
    }
}