package com.kc.newsapp.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.kc.newsapp.data.Contract


class ListViewModelFactory(private val context: Context, private val repo: Contract.Repository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ListViewModel(context, repo) as T
    }
}