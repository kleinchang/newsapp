package com.kc.newsapp.data.local

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.google.gson.Gson
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.testing.TestUtil

class ArticlesLocalData(private val context: Context) : Contract.Local {

    override val storage = MutableLiveData<Articles>()

    private val gson by lazy { Gson() }

    override fun fetchArticles() {
        TestUtil.getStringFromFile(context, "articles.json").let {
            gson.fromJson(it, Articles::class.java).let {
                storage.postValue(it)
            }
        }
    }
}