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
import com.kc.newsapp.data.remote.ArticlesService
import com.kc.newsapp.data.remote.Endpoint
import com.kc.newsapp.data.util.AppConfig
import com.kc.newsapp.ui.log
import com.kc.newsapp.util.stringSetLiveData
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async


class ListViewModel(context: Context, private val repo: Contract.Repository) : ViewModel() {

    val KEY_COUNTRIES = "country_list"
    private val service by lazy { ArticlesService() }

    // TODO: Inject Application Dagger
//    var perfLiveData: MutableLiveData<Set<String>>
//    init {
//        log("ListViewModel init")
//        val prefs = context.getSharedPreferences("config", MODE_PRIVATE)
//        perfLiveData = prefs.stringSetLiveData("list", mutableSetOf())
//    }

//    val perfLiveData by lazy {
//        context.getSharedPreferences("config", MODE_PRIVATE).getStringSet(KEY_COUNTRIES, mutableSetOf())?.let {
//            log("Pref $it")
//        }
//        context.getSharedPreferences("config", MODE_PRIVATE).stringSetLiveData(KEY_COUNTRIES, mutableSetOf())
//    }

    val perfLiveData = context.getSharedPreferences("config", MODE_PRIVATE).stringSetLiveData(KEY_COUNTRIES, mutableSetOf())

    private val _combinedList = map(perfLiveData, {
        log("perfLiveData trigger fetchArticles $it")

        context.getSharedPreferences("config", MODE_PRIVATE).getStringSet(KEY_COUNTRIES, mutableSetOf())?.let {
            log("Pref $it")
        }
        _loading.postValue(true)
        val deferred = it.map { async { service.fetchArticles(url = Endpoint.URL, country = it) } }
        async (CommonPool) {
            deferred?.flatMap { it.await().articles }?.let {
                log("ListViewModel fetchArticles ${it.size}")
                _loading.postValue(false)
                Articles(articles = it.sortedByDescending { it.publishedAt })
            }
        }
    })

//    val _combinedList = AsyncLiveData.create {
//        set -> suspendingFun(set)
//    }

//    suspend fun suspendingFun(set: Set<String>) {
//        val deferred = set.map { async { service.fetchArticles(url = Endpoint.URL, country = it) } }
//        async (CommonPool) {
//            deferred?.flatMap { it.await().articles }?.let {
//                Articles(articles = it.sortedByDescending { it.publishedAt })
//            }
//        }
//    }

//    val combinedList = _combinedList

    val options = MutableLiveData<MutableSet<String>>()

//    fun addOrRemoveCountry(country: String, toAddOrRmove: Boolean) {
//        if (toAddOrRmove)
//            options.value?.add(country)
//        else
//            options.value?.remove(country)
//    }




//    var countries: SharedPreferenceStringSetLiveData(AppConfig.KE)
//    set(value) {
//        val currentSet = AppConfig(context).getCountryList()
//
//
//    }

//    fun addCountry(country: String, toAdd: Boolean) {
//        if (perfLiveData.value == null) {
//            perfLiveData.value = mutableSetOf(country)
//        } else {
//            val set = perfLiveData.value
//            (set as MutableSet).add(country)
//            perfLiveData.value = set
//        }
//    }

//    private val _articles: LiveData<Articles> by lazy {
//        MediatorLiveData<Articles>().apply {
//            addSource(repo.fetched) {
//                if (it != null) {
//                    value = it
//                }
//            }
//        }
//    }

//    val countryCode = MutableLiveData<String>()
//    private val repoResult = map(countryCode, {
//        repo.fetchArticles(forceUpdate = false, country = it)
//    })

//    private val repoResultSet = map(perfLiveData, {
//        val deferred = it.map { c ->
//            async {
//                repo.fetchArticles(forceUpdate = false, country = c)
//            }
//        }
//        runBlocking {
//            deferred.map { it.await().articles.value }
//        }
//
//    })



//    private val __articles = switchMap(repoResult, { it.articles })
//    private val repoResult = MediatorLiveData<Articles>().apply {
//        addSource(countryCode) {
//            value = repo.fetchArticles(forceUpdate = false, country = it!!).articles
//        }
//    }


    private val _loading: MutableLiveData<Boolean> = repo.loading
    private val _error: LiveData<Boolean> = repo.error

//    fun getArticles3() = combinedList
//    fun getArticles2() = __articles
//    fun getArticles() = _articles
    fun getArticles() = _combinedList
    fun getLoading() = _loading
    fun getError() = _error

    fun fetchArticles(forceUpdate: Boolean = false) {
        if (/*_articles.value == null */_combinedList.value == null || forceUpdate) {
            //repo.fetchArticles(forceUpdate)
            //countryCode.value = "us"
            perfLiveData.value = setOf("cn")
        }
    }
}
