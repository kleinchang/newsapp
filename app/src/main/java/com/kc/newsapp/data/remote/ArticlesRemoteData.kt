package com.kc.newsapp.data.remote

import android.arch.lifecycle.MutableLiveData
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.testing.TestOpen
import com.kc.newsapp.util.Util
import kotlinx.coroutines.experimental.async

@TestOpen
class ArticlesRemoteData(private val service: ArticlesService) : Contract.Remote {

    override val network = MutableLiveData<Articles>()
    override val loading = MutableLiveData<Boolean>()
    override val error = MutableLiveData<Boolean>()

    override suspend fun fetchArticles(endpoint: String, countries: Set<String>) {
        Util.log("ArticlesRemoteData fetchArticles $countries")
        try {
            loading.postValue(true)

            val deferred = countries.map {
                async { service.fetchArticles(url = Endpoint.URL, country = it) }
            }
            deferred?.flatMap { it.await().articles }?.let {
                Articles(articles = it.sortedByDescending { it.publishedAt })

                if (it.isNotEmpty()) {
                    val response = Articles(articles = it.sortedByDescending { it.publishedAt })
                    network.postValue(response)
                } else {
                    error.postValue(true)
                }
            }
        } catch (e: Throwable) {
            error.postValue(true)
        } finally {
            loading.postValue(false)
        }
    }
}