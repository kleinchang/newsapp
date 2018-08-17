package com.kc.newsapp.data.model

import com.kc.newsapp.testing.TestOpen

class Articles(val status: String = "", val totalResults: Int = 0,
               val articles: List<Article> = emptyList()) {

    // TODO: a simplified comparator here
    override fun equals(other: Any?): Boolean {
        return status == (other as Articles).status &&
                totalResults == other.totalResults &&
                articles.size == other.articles.size
    }
}

@TestOpen
class Article(val source: Source, val author: String, val title: String,
              val description: String, val url: String,
              val urlToImage: String, val publishedAt: String)

class Source(val id: Any? = null, val name: String)