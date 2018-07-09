package com.kc.newsapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.kc.newsapp.data.ArticlesRepository
import com.kc.newsapp.data.local.ArticlesLocalData
import com.kc.newsapp.data.remote.ArticlesRemoteData
import com.kc.newsapp.data.remote.ArticlesService
import com.kc.newsapp.viewmodel.ListViewModel
import com.kc.newsapp.viewmodel.ListViewModelFactory
import kotlinx.android.synthetic.main.activity_article_list.*
import kotlinx.android.synthetic.main.activity_main.*
import com.kc.newsapp.*
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.data.remote.Endpoint
import com.kc.newsapp.data.util.AppConfig
import com.kc.newsapp.data.util.AppConfig.Companion.KEY_COUNTRIES
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


class ArticleListActivity : AppCompatActivity() {

    val KEY_COUNTRIES = "country_list"

    private lateinit var rvAdapter: ArticlesAdapter
    private lateinit var viewModel: ListViewModel
    private val pref by lazy { getSharedPreferences("config", Context.MODE_PRIVATE) }
    private val appConfig by lazy { AppConfig(pref) }
//    var currentSet = setOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = getViewModel().also {
            initRecyclerView(it)
            initSwipeToRefresh(it)
            initErrorView(it)
        }.apply {
            fetchArticles()
        }
    }

    private fun getViewModel(): ListViewModel {
        val repo = ArticlesRepository(ArticlesLocalData(applicationContext), ArticlesRemoteData(ArticlesService()))
        return ViewModelProviders.of(this, ListViewModelFactory(applicationContext, repo))[ListViewModel::class.java]
    }

    private fun initRecyclerView(viewModel: ListViewModel) {
        rvAdapter = ArticlesAdapter()
        LinearLayoutManager(this@ArticleListActivity).let {
            list.layoutManager = it
            list.addItemDecoration(DividerItemDecoration(list.context, it.orientation))
        }
        list.adapter = rvAdapter

        viewModel.getArticles().observe(this, Observer {
            it?.let {
                launch (UI) {
                    rvAdapter.data = it.await()
                    list.show()
                    errorView.hide()
                }
            }
        })
    }

    private fun initErrorView(viewModel: ListViewModel) {
        viewModel.getError().observe(this, Observer {
            if (it == true) {
                log("Error ->")
                list.hide()
                errorView.show()
            }
        })
    }

    private fun initSwipeToRefresh(viewModel: ListViewModel) {
        viewModel.getLoading().observe(this, Observer {
            log("getLoading $it")
            if (it != null) {
                swipeRefresh.isRefreshing = it
                if (it) {
                    errorView.hide()
                    list.hide()
                }
            }
        })
        swipeRefresh.setOnRefreshListener {
            viewModel.fetchArticles(forceUpdate = true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun openDialog() {

        AlertDialog.Builder(this@ArticleListActivity).apply {

            val array = resources.getStringArray(R.array.country)
            val set = mutableSetOf<String>()

            setTitle(R.string.title).setMultiChoiceItems(array, null) {
                dialog, which, isChecked ->
                if (isChecked)
                    set.add(array[which])
                else
                    set.remove(array[which])
            }.setPositiveButton(R.string.ok) {
                dialog, which ->
                log("positive ${set.joinToString() }")
                pref.updateCountries(set)
            }.setNegativeButton(R.string.cancel) {
                dialog, which ->
                log("negative $which")
            }.create().show()
        }

    }

    fun query(value: Set<String>) {
        log("query ${value.joinToString() }}")
        val service = ArticlesService()
        val deferred = value?.map { c ->
            async { service.fetchArticles(url = Endpoint.URL, country = c) }
        }
        async (CommonPool) {
            log("async start")
            deferred?.flatMap { it.await().articles }?.let {
                rvAdapter.data = Articles(articles = it.sortedByDescending { it.publishedAt })
                log("async Size: ${it.size}")
                list.show()
                errorView.hide()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            openDialog()
            true
        }
        R.id.action_favorite -> {
            openDialog()
            true
        }
//        R.id.jp -> {
//            item.isChecked = !item.isChecked
//            pref.updateCountry("jp", item.isChecked)
//            false
//        }
//        R.id.tw -> {
//            item.isChecked = !item.isChecked
//            pref.updateCountry("tw", item.isChecked)
//            true
//        }
//        R.id.us -> {
//            item.isChecked = !item.isChecked
//            pref.updateCountry("us", item.isChecked)
//            false
//        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}

//fun SharedPreferences.updateCountry(country: String, toAddOrRemove: Boolean) {
//    val current = getStringSet(KEY_COUNTRIES, setOf()).toMutableSet()
//    if (toAddOrRemove)
//        current.add(country)
//    else
//        current.remove(country)
//    edit().putStringSet(KEY_COUNTRIES, current).apply()
//}

fun SharedPreferences.updateCountries(countries: Set<String>) {
    edit().putStringSet(KEY_COUNTRIES, countries).apply()
}


fun View.show() = show(true)
fun View.hide() = show(false)
fun View.show(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}