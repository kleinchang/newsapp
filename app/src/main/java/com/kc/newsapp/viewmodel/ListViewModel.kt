package com.kc.newsapp.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.remote.ArticlesService
import com.kc.newsapp.data.remote.Endpoint
import com.kc.newsapp.ui.log
import com.kc.newsapp.util.stringSetLiveData
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async


class ListViewModel(context: Context, private val repo: Contract.Repository) : ViewModel() {

    val KEY_COUNTRIES = "country_list"
    private val service by lazy { ArticlesService() }

    // TODO: Inject Application Dagger
    private val sharedPreferenceLiveData = context.getSharedPreferences("config", MODE_PRIVATE).stringSetLiveData(KEY_COUNTRIES, mutableSetOf())
    val countryOfInterest = MediatorLiveData<Set<String>>().apply {
        addSource(sharedPreferenceLiveData) {
            if (value != it) {
                log("$value != $it")
                value = it
            } else {
                log("$value == $it")
            }
        }
    }

    private val _combinedList = map(countryOfInterest, {
        _loading.postValue(true)
        val deferred = it.map { async { service.fetchArticles(url = Endpoint.URL, country = it) } }
        async (CommonPool) {
            deferred?.flatMap { it.await().articles }?.let {
                _loading.postValue(false)
                Articles(articles = it.sortedByDescending { it.publishedAt })
            }
        }
    })

    private val _loading: MutableLiveData<Boolean> = repo.loading
    private val _error: LiveData<Boolean> = repo.error

    fun getCombinedList() = _combinedList
    val articles = MutableLiveData<Articles>()
    fun getLoading() = _loading
    fun getError() = _error

    fun fetchArticles(forceUpdate: Boolean = false) {
        if (_combinedList.value == null || forceUpdate) {
            countryOfInterest.value = sharedPreferenceLiveData.value
        }
    }
}
