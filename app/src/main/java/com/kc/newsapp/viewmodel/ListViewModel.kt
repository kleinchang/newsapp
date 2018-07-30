package com.kc.newsapp.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.model.Article
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.remote.ArticlesService
import com.kc.newsapp.data.remote.Endpoint
import com.kc.newsapp.ui.log
import com.kc.newsapp.util.stringLiveData
import com.kc.newsapp.util.stringSetLiveData
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async


class ListViewModel(context: Context, private val repo: Contract.Repository) : ViewModel() {

    companion object {
        const val KEY_COUNTRIES = "country_list"
        const val KEY_BOOKMARKS = "article_list"
        const val KEY_BOOKMARKS_JSON = "article_list_json"
    }
    private val gson by lazy { Gson() }
    private val service by lazy { ArticlesService() }
    val sharedPreferences by lazy { context.getSharedPreferences("config", MODE_PRIVATE) }

    val bookmarkSharedPref = sharedPreferences.stringSetLiveData(KEY_BOOKMARKS, mutableSetOf())
    val bookmarks = MediatorLiveData<Set<String>>().apply {
        addSource(bookmarkSharedPref) {
            if (value != it) {
                log("Bookmarks: $value != $it")
                value = it
            } else {
                log("Bookmarks: $value == $it")
            }
        }
    }

    private val bookmarkJson = sharedPreferences.stringLiveData(KEY_BOOKMARKS_JSON, "[]")
    val bookmarkArticleList: LiveData<List<Article>> = map(bookmarkJson, {
        val type = object: TypeToken<List<Article>>() {}.type
        gson.fromJson<List<Article>>(it, type)
    })

    // TODO: Inject Application Dagger
    private val sharedPreferenceLiveData = sharedPreferences.stringSetLiveData(KEY_COUNTRIES, mutableSetOf())
    val countryOfInterest = MediatorLiveData<Set<String>>().apply {
        addSource(sharedPreferenceLiveData) {
            if (value != it) {
                log("CountryOfInterest: $value != $it")
                value = it
            } else {
                log("CountryOfInterest: $value == $it")
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
