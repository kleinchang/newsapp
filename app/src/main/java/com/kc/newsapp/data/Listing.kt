package com.kc.newsapp.data

import android.arch.lifecycle.LiveData

data class Listing<T>(
    val articles: LiveData<T>,
    val loadingState: LiveData<Boolean>,
    val errorState: LiveData<Boolean>
)