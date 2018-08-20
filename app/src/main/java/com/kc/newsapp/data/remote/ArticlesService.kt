package com.kc.newsapp.data.remote

import com.google.gson.Gson
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.remote.Endpoint.API_KEY
import com.kc.newsapp.testing.TestOpen
import com.kc.newsapp.util.Util
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

@TestOpen
class ArticlesService {

    fun fetchArticles(url: String, country: String, delayDuration: Int = 0): Articles {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                    .addHeader("X-Api-Key", API_KEY)
                    .build()
            chain.proceed(request)
        }.build()

        val params = hashMapOf("country" to country)
        val httpBuilder = HttpUrl.parse(url)!!.newBuilder().apply {
            params.keys.forEach { addQueryParameter(it, params[it]) }
        }

        val request = Request.Builder().url(httpBuilder.build()).build()
        val data = arrayOfNulls<Any>(1)
        Util.log("ArticlesService fetchArticles $country")
        // TODO: https://stackoverflow.com/questions/30538640/javax-net-ssl-sslexception-read-error-ssl-0x9524b800-i-o-error-during-system
        val res = client.newCall(request).execute()
        if (res.isSuccessful) {
            Util.log("ArticlesService fetchArticles $country successful")
            val response = res.body()?.string()
            val content: Articles = Gson().fromJson(response, Articles::class.java)
            data[0] = content
            runBlocking { delay(delayDuration) }
        }

        return data[0] as Articles
    }
}