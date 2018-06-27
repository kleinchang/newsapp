package com.kc.newsapp.data.remote

import android.arch.lifecycle.MutableLiveData
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.model.Articles

class ArticlesRemoteData(private val service: ArticlesService) : Contract.Remote {

    override val network = MutableLiveData<Articles>()
    override val loading = MutableLiveData<Boolean>()
    override val error = MutableLiveData<Boolean>()

    override suspend fun fetchArticles(endpoint: String) {
        try {
            loading.postValue(true)
            service.fetchRecipes(endpoint, 0)
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