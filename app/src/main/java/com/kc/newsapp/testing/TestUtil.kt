package com.kc.newsapp.testing

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


object TestUtil {

    fun getStringFromFile(context: Context, path: String): String {
        val inputStream = context.assets.open(path)
        val json = convertStreamToString(inputStream)
        inputStream.close()
        return json
    }

    fun convertStreamToString(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String? = null
        while ({line = reader.readLine(); line}() != null) {
            sb.append(line).append("\n")
        }
        reader.close()
        return sb.toString()
    }

}