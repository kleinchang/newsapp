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
import com.kc.newsapp.viewmodel.ListViewModel.Companion.KEY_BOOKMARKS
import com.kc.newsapp.viewmodel.ListViewModel.Companion.KEY_COUNTRIES
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


class ArticleListActivity : AppCompatActivity() {

    private lateinit var rvAdapter: ArticlesAdapter
    private lateinit var viewModel: ListViewModel
    private val pref by lazy { getSharedPreferences("config", Context.MODE_PRIVATE) }
    private val appConfig by lazy { AppConfig(pref) }


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
        rvAdapter = ArticlesAdapter(viewModel.bookmarks) {
            position, title ->
            pref.updateBookmarks(title)
            rvAdapter.notifyItemChanged(position)
        }
        LinearLayoutManager(this@ArticleListActivity).let {
            list.layoutManager = it
            list.addItemDecoration(DividerItemDecoration(list.context, it.orientation))
        }
        list.adapter = rvAdapter

        viewModel.getCombinedList().observe(this, Observer {
            it?.let {
                launch (UI) {
                    viewModel.articles.value = it.await()
                }
            }
        })
        viewModel.articles.observe(this, Observer {
            it?.let {
                rvAdapter.data = it
                list.show()
                errorView.hide()
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

    private fun openDialog(viewModel: ListViewModel) {

        AlertDialog.Builder(this@ArticleListActivity).apply {

            val array = resources.getStringArray(R.array.country)
            val checked = BooleanArray(array.size, { i -> viewModel.countryOfInterest.value?.contains(array[i]) ?: false })
            val set = mutableSetOf<String>()
            viewModel.countryOfInterest.value?.forEach { set.add(it) }

            setTitle(R.string.title).setMultiChoiceItems(array, checked) {
                dialog, which, isChecked ->
                if (isChecked)
                    set.add(array[which])
                else
                    set.remove(array[which])
            }.setPositiveButton(R.string.ok) {
                dialog, which ->
                log("OK ${set.joinToString() }")
                pref.updateStringSet(set, KEY_BOOKMARKS)
                //pref.updateCountries(set)
                //pref.updateStringSet(set, KEY_COUNTRIES)
            }.setNegativeButton(R.string.cancel) {
                dialog, which ->
                log("Cancel $which")
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
            openDialog(viewModel)
            true
        }
        R.id.action_favorite -> {
            openDialog(viewModel)
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
    updateStringSet(countries, KEY_COUNTRIES)
}

fun SharedPreferences.updateBookmarks(title: String) {
    val originalSet = getStringSet(KEY_BOOKMARKS, mutableSetOf())
    val currentSet = mutableSetOf<String>()
    originalSet.forEach { currentSet.add(it) }
    if (currentSet.contains(title)) {
        currentSet.remove(title)
        log("remove $title from ${currentSet.size}")
    } else {
        currentSet.add(title)
        log("add $title into ${currentSet.size}")
    }
    updateStringSet(currentSet, KEY_BOOKMARKS)
}

fun SharedPreferences.updateStringSet(countries: Set<String>, key: String) {
    edit().putStringSet(key, countries).commit()
}



fun View.show() = show(true)
fun View.hide() = show(false)
fun View.show(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}