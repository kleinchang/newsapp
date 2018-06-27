package com.kc.newsapp.data.model

class Articles(val status: String = "", val totalResults: Int = 0, val articles: List<Article> = emptyList())

class Article(val source: Source, val author: String, val title: String,
              val description: String, val url: String,
              val urlToImage: String, val publishedAt: String)

class Source(val id: Any? = null, val name: String)