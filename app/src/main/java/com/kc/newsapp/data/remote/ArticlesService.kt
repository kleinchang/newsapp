package com.kc.newsapp.data.remote

import com.google.gson.Gson
import com.kc.newsapp.data.model.Articles
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request

class ArticlesService {

    fun fetchRecipes(url: String, delayDuration: Int): Articles {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val data = arrayOfNulls<Any>(1)
        val res = client.newCall(request).execute()
        if (res.isSuccessful) {
            val response = res.body()?.string()
            val content: Articles = Gson().fromJson(response, Articles::class.java)
            data[0] = content
            runBlocking { delay(delayDuration) }
        }

        return data[0] as Articles
    }
}