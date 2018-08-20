package com.kc.newsapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kc.newsapp.R
import com.kc.newsapp.testing.EspressoIdlingResource
import com.kc.newsapp.util.Util
import com.kc.newsapp.util.hide
import com.kc.newsapp.util.show
import com.kc.newsapp.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.activity_article_list.*

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
            Util.log("liveData in ViewModel bookmarks $it")
        })
    }

    private fun getViewModel(activity: FragmentActivity): ListViewModel {
        return ViewModelProviders.of(activity)[ListViewModel::class.java]
    }

    open fun initRecyclerView(viewModel: ListViewModel) {
        rvAdapter = ArticlesAdapter(viewModel.bookmarks, { url -> openArticle(url) } ) {
            position, article ->
            viewModel.updateBookmarkKeys(article.title)
            viewModel.updateBookmarkContent(article)
            rvAdapter.notifyItemChanged(position)
        }
        LinearLayoutManager(activity).let {
            list.layoutManager = it
            list.addItemDecoration(DividerItemDecoration(list.context, it.orientation))
        }
        list.adapter = rvAdapter

        viewModel.combinedList().observe(this, Observer {
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
                Util.log("Error ->")
                list.hide()
                errorView.text = "Error"
                errorView.show()
            }
        })
    }

    open fun initSwipeToRefresh(viewModel: ListViewModel) {
        viewModel.getLoading().observe(this, Observer {
            Util.log("getLoading $it")
            if (it != null) {
                swipeRefresh.isRefreshing = it
                if (it) {
                    setIsAppBusy(true)
                    errorView.hide()
                    list.hide()
                } else {
                    setIsAppBusy(false)
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

    @VisibleForTesting
    private fun setIsAppBusy(busy: Boolean) {
        Util.log("setIsAppBusy $busy")
        if (busy) {
            EspressoIdlingResource.increment() // App is busy until further notice
        } else {
            if (!EspressoIdlingResource.countingIdlingResource.isIdleNow)
                EspressoIdlingResource.decrement() // Set app as idle.
        }
    }

    companion object {
        const val TAG = "ArticleListFragment"
    }
}