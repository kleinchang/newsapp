package com.kc.newsapp.data

import android.arch.lifecycle.LiveData
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.remote.Endpoint

interface Contract {

    interface Repository {
        val fetched: LiveData<Articles>
        val error: LiveData<Boolean>
        val loading: LiveData<Boolean>
        fun fetchArticles(forceUpdate: Boolean)
    }

    interface Local {
        val storage: LiveData<Articles>
        fun fetchArticles()
    }

    interface Remote {
        val network: LiveData<Articles>
        val error: LiveData<Boolean>
        val loading: LiveData<Boolean>
        suspend fun fetchArticles(endpoint: String = Endpoint.URL)
    }

}