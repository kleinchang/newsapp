package com.kc.newsapp.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.kc.newsapp.viewmodel.ListViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Provides @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
//                .apply {
//                    edit().putStringSet(ListViewModel.KEY_COUNTRIES, mutableSetOf("ro", "ru")).apply()
//                }
    }
}