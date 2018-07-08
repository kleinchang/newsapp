package com.kc.newsapp.data.remote

import android.arch.lifecycle.MutableLiveData
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.ui.log

class ArticlesRemoteData(private val service: ArticlesService) : Contract.Remote {

    override val network = MutableLiveData<Articles>()
    override val loading = MutableLiveData<Boolean>()
    override val error = MutableLiveData<Boolean>()

    //val params = hashMapOf("country" to "us")

    override suspend fun fetchArticles(endpoint: String, country: String) {
        log("ArticlesRemoteData fetchArticles $country")
        try {
            loading.postValue(true)
            service.fetchArticles(endpoint, country)
                    .let {
                        if (it.articles.isNotEmpty())
                            network.postValue(it)
                        else
                            error.postValue(true)
                    }
        } catch (e: Throwable) {
            error.postValue(true)
        } finally {
            loading.postValue(false)
        }
    }
}