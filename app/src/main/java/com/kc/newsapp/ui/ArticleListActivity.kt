package com.kc.newsapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
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
import com.kc.newsapp.util.hide
import com.kc.newsapp.util.show
import com.kc.newsapp.util.updateBookmarkKeys
import com.kc.newsapp.util.updateCountries
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


class ArticleListActivity : AppCompatActivity() {

    private lateinit var rvAdapter: ArticlesAdapter
    private lateinit var viewModel: ListViewModel

    companion object {
        const val CURRENT_TAB = "tab"
    }

    private var currentTab = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        currentTab = savedInstanceState?.getBoolean(CURRENT_TAB, true) ?: true
        if (currentTab)
            addFragment(ArticleListFragment.TAG)
        else
            addFragment(BookmarkFragment.TAG)

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_favorites -> {
                    currentTab = addFragment(BookmarkFragment.TAG)
                    true
                }
                R.id.action_live_news -> {
                    currentTab = addFragment(ArticleListFragment.TAG)
                    true
                }
                else -> {
                    throw Exception("Artificial")
                    true
                }
            }
        }

        viewModel = getViewModel()

        /*
        viewModel = getViewModel().also {
            initRecyclerView(it)
            initSwipeToRefresh(it)
            initErrorView(it)
        }.apply {
            fetchArticles()
        }

        viewModel.bookmarks.observe(this, Observer {
            log("liveData in ViewModel bookmarks $it")
        })
        */
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(CURRENT_TAB, currentTab)
    }

    private fun addFragment(tag: String): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            supportFragmentManager.beginTransaction().
                    replace(R.id.content, if (tag == ArticleListFragment.TAG) ArticleListFragment() else BookmarkFragment(), tag)
                    .addToBackStack(null).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.content, fragment, tag)
                    .addToBackStack(null).commit()
        }
        return tag == ArticleListFragment.TAG
    }

    private fun getViewModel(): ListViewModel {
        val repo = ArticlesRepository(ArticlesLocalData(applicationContext), ArticlesRemoteData(ArticlesService()))
        return ViewModelProviders.of(this, ListViewModelFactory(applicationContext, repo))[ListViewModel::class.java]
    }

    /*
    private fun initRecyclerView(viewModel: ListViewModel) {
        rvAdapter = ArticlesAdapter(viewModel.bookmarks, viewModel.bookmarkJson) {
            position, article ->
            viewModel.sharedPreferences.updateBookmarkKeys(article.title)
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
    */

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
                viewModel.sharedPreferences.updateCountries(set)
                //viewModel.sharedPreferences.updateStringSet(set, KEY_BOOKMARKS)
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
//        R.id.action_favorite -> {
//            openDialog(viewModel)
//            true
//        }
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