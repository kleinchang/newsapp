package com.kc.newsapp.viewmodel

import android.arch.lifecycle.*
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.Listing
import com.kc.newsapp.data.model.Article
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.util.*


class ListViewModel(private val sharedPreferences: SharedPreferences, private val repo: Contract.Repository) : ViewModel() {

    companion object {
        const val KEY_COUNTRIES = "country_list"
        const val KEY_BOOKMARKS = "article_list"
        const val KEY_BOOKMARKS_JSON = "article_list_json"
    }

    private val gson by lazy { Gson() }

    private val bookmarkLiveData = sharedPreferences.stringSetLiveData(KEY_BOOKMARKS, mutableSetOf())
    val bookmarks = MediatorLiveData<Set<String>>().apply {
        addSource(bookmarkLiveData) {
            if (value != it) {
                Util.log("Bookmarks: $value != $it")
                value = it
            } else {
                Util.log("Bookmarks: $value == $it")
            }
        }
    }

    private val bookmarkJson = sharedPreferences.stringLiveData(KEY_BOOKMARKS_JSON, "[]")
    val bookmarkArticleList: LiveData<List<Article>> = map(bookmarkJson, {
        val type = object: TypeToken<List<Article>>() {}.type
        gson.fromJson<List<Article>>(it, type)
    })

    private val countryOfInterestLiveData = sharedPreferences.stringSetLiveData(KEY_COUNTRIES, mutableSetOf())
    val countryOfInterest = MediatorLiveData<Set<String>>().apply {
        addSource(countryOfInterestLiveData) {
            if (value != it) {
                Util.log("CountryOfInterest: $value != $it")
                value = it
            } else {
                Util.log("CountryOfInterest: $value == $it")
            }
        }
    }

    private val fetchedOutcome: LiveData<Listing<Articles>> = map(countryOfInterest, {
        repo.fetchArticles(false, it)
    })

    val articles: LiveData<Articles> = switchMap(fetchedOutcome, { it.articles })

    private val _loading: MutableLiveData<Boolean> = repo.loading
    private val _error: LiveData<Boolean> = repo.error

    fun updateCountries(countries: Set<String>) {
        sharedPreferences.updateCountries(countries)
    }

    fun updateBookmarkKeys(title: String) {
        sharedPreferences.updateBookmarkKeys(title)
    }

    fun updateBookmarkContent(article: Article) {
        sharedPreferences.updateBookmarkContent(article)
    }

    fun combinedList(): LiveData<Articles> = articles
    fun getLoading() = _loading
    fun getError() = _error

    fun fetchArticles(forceUpdate: Boolean = false) {
        if (combinedList().value == null || forceUpdate) {
            countryOfInterest.value = countryOfInterestLiveData.value
        }
    }
}
