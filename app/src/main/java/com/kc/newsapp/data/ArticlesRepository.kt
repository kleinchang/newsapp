package com.kc.newsapp.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.ui.log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

class ArticlesRepository(private val local: Contract.Local,
                         private val remote: Contract.Remote): Contract.Repository {

    override val fetched: LiveData<Articles> = MediatorLiveData<Articles>().apply {
        addSource(remote.network) { value = it }
        addSource(local.storage) { value = it }
    }

    override val loading = remote.loading
    override val error = remote.error

    override fun fetchArticles(forceUpdate: Boolean) {
        log("fetchArticles $forceUpdate")
        local.fetchArticles()
//        if (remote.network.value == null || forceUpdate) {
//            launch(CommonPool) { remote.fetchArticles() }
//        }
    }
}