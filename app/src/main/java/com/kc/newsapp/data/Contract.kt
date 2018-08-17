package com.kc.newsapp.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.remote.Endpoint

interface Contract {

    interface Repository {
        val fetched: LiveData<Articles>
        val error: LiveData<Boolean>
        val loading: MutableLiveData<Boolean>
        fun fetchArticles(forceUpdate: Boolean, countries: Set<String> = setOf("us")): Listing<Articles>
    }

    interface Local {
        val storage: LiveData<Articles>
        fun fetchArticles()
    }

    interface Remote {
        val network: LiveData<Articles>
        val error: LiveData<Boolean>
        val loading: MutableLiveData<Boolean>
        suspend fun fetchArticles(endpoint: String = Endpoint.URL, countries: Set<String> = setOf("us"))
    }

}