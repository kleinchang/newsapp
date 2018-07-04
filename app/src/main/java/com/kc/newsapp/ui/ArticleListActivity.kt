package com.kc.newsapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
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
import android.view.MenuInflater
import com.kc.newsapp.*
import com.kc.newsapp.data.util.AppConfig


class ArticleListActivity : AppCompatActivity() {

    private lateinit var rvAdapter: ArticlesAdapter
    private lateinit var viewModel: ListViewModel
    val pref by lazy { getSharedPreferences("config", Context.MODE_PRIVATE) }
    private val appConfig by lazy { AppConfig(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = getViewModel().also {
            initRecyclerView(it)
            initSwipeToRefresh(it)
            initErrorView(it)
        }.apply { fetchArticles() }


        var liveData = pref.intLiveData("int_key", 0) as SharedPreferenceIntLiveData
//        if (liveData.value == null) liveData.updateValue(0)
//        log("LiveData ${liveData.value}")
//        liveData.value?.let {
//            liveData.updateValue(it + 1)
//        }
        liveData.observe(this, Observer { value -> log("IntLiveData $value")})
        pref.edit().putInt("int_key", 3).apply()
        pref.edit().putInt("int_key", 4)
        pref.edit().putInt("int_key", 5)

        val curr = pref.getStringSet(AppConfig.KEY_COUNTRIES, mutableSetOf())
        log("Curr size: ${curr.size} or ${appConfig.getCountryList()}")
        pref.stringSetLiveData(AppConfig.KEY_COUNTRIES, curr).observe(this, Observer {
            value -> log("StringSetLiveData $value")
        })

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

        viewModel.getArticles2().observe(this, Observer {
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
            if (it != null) {
                swipeRefresh.isRefreshing = it
                if (it) {
                    errorView.hide()
                    list.hide()
                }
            }
        })
        swipeRefresh.setOnRefreshListener { viewModel.fetchArticles(forceUpdate = true) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            true
        }
        R.id.action_favorite -> {
            true
        }
        R.id.jp -> {
            item.isChecked = !item.isChecked
            viewModel.countryCode.value = "jp"
            //viewModel.addCountry("jp", item.isChecked)
            pref.edit().putInt("int_key", 3).apply()
            appConfig.addRemoveCountry("jp", item.isChecked)
            log("Country list: ${appConfig.getCountryList()}")
            false
        }
        R.id.tw -> {
            item.isChecked = !item.isChecked
            viewModel.countryCode.value = "tw"
            //viewModel.addCountry("tw")
            pref.edit().putInt("int_key", 2).apply()
            appConfig.addRemoveCountry("tw", item.isChecked)
            log("Country list: ${appConfig.getCountryList()}")
            false
        }
        R.id.us -> {
            item.isChecked = !item.isChecked
            viewModel.countryCode.value = "us"
            //viewModel.addCountry("us")
            pref.edit().putInt("int_key", 1).apply()
            appConfig.addRemoveCountry("us", item.isChecked)
            log("Country list: ${appConfig.getCountryList()}")
            false
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
        val inflater = popup.getMenuInflater()
        inflater.inflate(R.menu.actions, popup.menu)
        popup.show()
    }
}


fun View.show() = show(true)
fun View.hide() = show(false)
fun View.show(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}