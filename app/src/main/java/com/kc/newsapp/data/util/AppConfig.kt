package com.kc.newsapp.data.util

import android.content.Context

class AppConfig(private val context: Context) {

    companion object {
        val KEY_COUNTRIES = "country_list"
    }

    private val pref by lazy { context.getSharedPreferences("config", Context.MODE_PRIVATE) }

    fun getCountryList(): Set<String> {
        return pref.getStringSet(KEY_COUNTRIES, emptySet())
    }

    fun updateCountryList(countries: Set<String>) {
        pref.edit().remove(KEY_COUNTRIES).putStringSet(KEY_COUNTRIES, countries).apply()
    }

}