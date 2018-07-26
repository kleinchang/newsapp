package com.kc.newsapp.data.util

import android.content.Context
import android.content.SharedPreferences

class AppConfig(private val pref: SharedPreferences) {

//    private val pref = context.getSharedPreferences("config", Context.MODE_PRIVATE)
//    private val KEY_COUNTRIES = "country_list"

    companion object {
//        val KEY_COUNTRIES = "country_list"
//        val KEY_BOOKMARKS = "article_list"

//        fun getInstance(pref: SharedPreferences): AppConfig {
//            return AppConfig(pref)
//        }
//
//        val instance: AppConfig by lazy { AppConfig }
    }

//    private val pref by lazy { context.getSharedPreferences("config", Context.MODE_PRIVATE) }

//    fun addRemoveCountry(country: String, toAdd: Boolean) {
//        val currentSet = getCountryList() as MutableSet
//        if (toAdd) currentSet.add(country)
//        else currentSet.remove(country)
//        updateCountryList(currentSet)
//    }
//
//    fun getCountryList(): Set<String> {
//        return pref.getStringSet(KEY_COUNTRIES, mutableSetOf())
//    }
//
//    private fun updateCountryList(countries: Set<String>) {
//        pref.edit().remove(KEY_COUNTRIES).putStringSet(KEY_COUNTRIES, countries).apply()
//    }

}