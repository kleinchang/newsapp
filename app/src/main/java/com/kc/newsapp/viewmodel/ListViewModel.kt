package com.kc.newsapp.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import com.kc.newsapp.data.Contract
import com.kc.newsapp.data.model.Articles

class ListViewModel(private val repo: Contract.Repository) : ViewModel() {

    private val _articles: LiveData<Articles> by lazy {
        MediatorLiveData<Articles>().apply {
            addSource(repo.fetched) {
                if (it != null) {
                    value = it
                }
            }
        }
    }

    private val _loading: LiveData<Boolean> = repo.loading
    private val _error: LiveData<Boolean> = repo.error

    fun getArticles() = _articles
    fun getLoading() = _loading
    fun getError() = _error

    fun fetchArticles(forceUpdate: Boolean = false) {
        if (_articles.value == null || forceUpdate) {
            repo.fetchArticles(forceUpdate)
        }
    }
}
