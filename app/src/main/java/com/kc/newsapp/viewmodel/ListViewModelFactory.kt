package com.kc.newsapp.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.SharedPreferences
import com.kc.newsapp.data.Contract


class ListViewModelFactory(private val sharedPreferences: SharedPreferences, private val repo: Contract.Repository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ListViewModel(sharedPreferences, repo) as T
    }
}