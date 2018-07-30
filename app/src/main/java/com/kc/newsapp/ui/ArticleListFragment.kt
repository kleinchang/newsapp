package com.kc.newsapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kc.newsapp.R
import com.kc.newsapp.data.ArticlesRepository
import com.kc.newsapp.data.local.ArticlesLocalData
import com.kc.newsapp.data.remote.ArticlesRemoteData
import com.kc.newsapp.data.remote.ArticlesService
import com.kc.newsapp.util.hide
import com.kc.newsapp.util.show
import com.kc.newsapp.util.updateBookmarkContent
import com.kc.newsapp.util.updateBookmarkKeys
import com.kc.newsapp.viewmodel.ListViewModel
import com.kc.newsapp.viewmodel.ListViewModelFactory
import kotlinx.android.synthetic.main.activity_article_list.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Created by kaichang on 18/7/18.
 */
open class ArticleListFragment : Fragment() {

    lateinit var rvAdapter: ArticlesAdapter
    private lateinit var viewModel: ListViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_article_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = getViewModel(activity!!).also {
            initRecyclerView(it)
            initSwipeToRefresh(it)
            initErrorView(it)
        }.apply {
            fetchArticles()
        }

        viewModel.bookmarks.observe(activity!!, Observer {
            log("liveData in ViewModel bookmarks $it")
        })
    }

    private fun getViewModel(activity: FragmentActivity): ListViewModel {
        val repo = ArticlesRepository(ArticlesLocalData(activity.applicationContext), ArticlesRemoteData(ArticlesService()))
        return ViewModelProviders.of(activity, ListViewModelFactory(activity.applicationContext, repo))[ListViewModel::class.java]
    }

    open fun initRecyclerView(viewModel: ListViewModel) {
        rvAdapter = ArticlesAdapter(viewModel.bookmarks, { url -> openArticle(url) } ) {
            position, article ->
            viewModel.sharedPreferences.updateBookmarkKeys(article.title)
            viewModel.sharedPreferences.updateBookmarkContent(article)
            rvAdapter.notifyItemChanged(position)
        }
        LinearLayoutManager(activity).let {
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
            if (it?.articles?.isNotEmpty() == true) {
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
                errorView.text = "Error"
                errorView.show()
            }
        })
    }

    open fun initSwipeToRefresh(viewModel: ListViewModel) {
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

    protected fun openArticle(url: String) {
        context?.let { WebViewActivity.open(it, url) }
    }

    companion object {
        const val TAG = "ArticleListFragment"
    }
}