package com.kc.newsapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.kc.newsapp.R
import com.kc.newsapp.data.ArticlesRepository
import com.kc.newsapp.data.local.ArticlesLocalData
import com.kc.newsapp.data.remote.ArticlesRemoteData
import com.kc.newsapp.data.remote.ArticlesService
import com.kc.newsapp.viewmodel.ListViewModel
import com.kc.newsapp.viewmodel.ListViewModelFactory
import kotlinx.android.synthetic.main.activity_article_list.*
import kotlinx.android.synthetic.main.activity_main.*
import android.view.MenuInflater



class ArticleListActivity : AppCompatActivity() {

    private lateinit var rvAdapter: ArticlesAdapter
    private lateinit var viewModel: ListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = getViewModel().also {
            initRecyclerView(it)
            initSwipeToRefresh(it)
            initErrorView(it)
        }.apply { fetchArticles() }
    }

    private fun getViewModel(): ListViewModel {
        val repo = ArticlesRepository(ArticlesLocalData(applicationContext), ArticlesRemoteData(ArticlesService()))
        return ViewModelProviders.of(this, ListViewModelFactory(repo))[ListViewModel::class.java]
    }

    private fun initRecyclerView(viewModel: ListViewModel) {
        rvAdapter = ArticlesAdapter()
        LinearLayoutManager(this@ArticleListActivity).let {
            list.layoutManager = it
            list.addItemDecoration(DividerItemDecoration(list.context, it.orientation))
        }
        list.adapter = rvAdapter

        viewModel.getArticles().observe(this, Observer {
            rvAdapter.data = it!!
            list.show()
            errorView.hide()
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
            true
        }
        R.id.tw -> {
            item.isChecked = !item.isChecked
            true
        }
        R.id.us -> {
            item.isChecked = !item.isChecked
            true
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