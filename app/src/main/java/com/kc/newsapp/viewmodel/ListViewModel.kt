package com.kc.newsapp.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.util.AppConfig
import com.kc.newsapp.util.SharedPreferenceStringSetLiveData
import com.kc.newsapp.util.stringSetLiveData


class ListViewModel(context: Context, private val repo: Contract.Repository) : ViewModel() {

    // TODO: Inject Application Dagger
    var perfLiveData: MutableLiveData<Set<String>>
    init {
        val prefs = context.getSharedPreferences("pref", MODE_PRIVATE)
        perfLiveData = prefs.stringSetLiveData("list", mutableSetOf())
        if (perfLiveData.value == null) {
            perfLiveData.value = mutableSetOf()
        }

    }




//    var countries: SharedPreferenceStringSetLiveData(AppConfig.KE)
//    set(value) {
//        val currentSet = AppConfig(context).getCountryList()
//
//
//    }

    fun addCountry(country: String) {
        if (perfLiveData.value == null) {
            perfLiveData.value = mutableSetOf(country)
        } else {
            val set = perfLiveData.value
            (set as MutableSet).add(country)
            perfLiveData.value = set
        }
    }

    private val _articles: LiveData<Articles> by lazy {
        MediatorLiveData<Articles>().apply {
            addSource(repo.fetched) {
                if (it != null) {
                    value = it
                }
            }
        }
    }

    val countryCode = MutableLiveData<String>()
    val repoResult = map(countryCode, {
        repo.fetchArticles(forceUpdate = false, country = it)
    })

    private val __articles = switchMap(repoResult, { it.articles })
//    private val repoResult = MediatorLiveData<Articles>().apply {
//        addSource(countryCode) {
//            value = repo.fetchArticles(forceUpdate = false, country = it!!).articles
//        }
//    }


    private val _loading: LiveData<Boolean> = repo.loading
    private val _error: LiveData<Boolean> = repo.error

    fun getArticles2() = __articles
    fun getArticles() = _articles
    fun getLoading() = _loading
    fun getError() = _error

    fun fetchArticles(forceUpdate: Boolean = false) {
        if (_articles.value == null || forceUpdate) {
            //repo.fetchArticles(forceUpdate)
            countryCode.value = "us"
        }
    }
}
