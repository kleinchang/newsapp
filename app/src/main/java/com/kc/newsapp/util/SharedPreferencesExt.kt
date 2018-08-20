package com.kc.newsapp.util

import android.content.SharedPreferences
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kc.newsapp.data.model.Article
import com.kc.newsapp.viewmodel.ListViewModel
import com.kc.newsapp.viewmodel.ListViewModel.Companion.KEY_BOOKMARKS_JSON

val gson by lazy { Gson() }

fun SharedPreferences.updateCountries(countries: Set<String>) {
    updateStringSet(countries, ListViewModel.KEY_COUNTRIES)
}

fun SharedPreferences.updateBookmarkKeys(title: String) {
    val originalSet = getStringSet(ListViewModel.KEY_BOOKMARKS, mutableSetOf())
    val currentSet = mutableSetOf<String>()
    originalSet.forEach { currentSet.add(it) }
    if (currentSet.contains(title)) {
        currentSet.remove(title)
        Util.log("remove $title from ${currentSet.size}")
    } else {
        currentSet.add(title)
        Util.log("add $title into ${currentSet.size}")
    }
    updateStringSet(currentSet, ListViewModel.KEY_BOOKMARKS)
}

fun SharedPreferences.updateStringSet(countries: Set<String>, key: String) {
    edit().putStringSet(key, countries).commit()
}

fun SharedPreferences.updateBookmarkContent(article: Article) {
    val type = object : TypeToken<List<Article>>(){}.type
    val saved = getString(ListViewModel.KEY_BOOKMARKS_JSON, "[]")
    val articles = gson.fromJson<MutableList<Article>>(saved, type)

    val articleToRemove = articles.firstOrNull { it.title == article.title }
    if (articleToRemove != null)
        articles.remove(articleToRemove)
    else
        articles.add(0, article)

    edit().putString(KEY_BOOKMARKS_JSON, gson.toJson(articles)).apply()
}


fun View.show() = show(true)
fun View.hide() = show(false)
fun View.show(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}