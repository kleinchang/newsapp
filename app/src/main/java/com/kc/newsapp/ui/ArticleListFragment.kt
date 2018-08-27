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
import android.widget.Toast
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
            initPromptListener(it)
        }.apply {
            fetchArticles()
        }

        viewModel.bookmarkTitles.observe(activity!!, Observer {
            Util.log("viewModel.bookmarkTitles $it")
        })
    }

    private fun getViewModel(activity: FragmentActivity): ListViewModel {
        return ViewModelProviders.of(activity)[ListViewModel::class.java]
    }

    open fun initRecyclerView(viewModel: ListViewModel) {
        rvAdapter = ArticlesAdapter(viewModel.bookmarkTitles, { url -> openArticle(url) } ) {
            position, article ->
            viewModel.updateBookmarkKeys(article.title)
            viewModel.updateBookmarkContent(article)
            rvAdapter.notifyItemChanged(position)
            viewModel.showPrompt("${article.title} is added to bookmark")
        }
        LinearLayoutManager(activity).let {
            list.layoutManager = it
            list.addItemDecoration(DividerItemDecoration(list.context, it.orientation))
        }
        list.setHasFixedSize(true)
        list.adapter = rvAdapter

        viewModel.combinedList().observe(this, Observer {
            it?.let {
                rvAdapter.data = it
                list.show()
                errorView.hide()
            }
        })
    }

    private fun initPromptListener(viewModel: ListViewModel) {
        viewModel.promptMessage.observe(this, Observer {
            it?.let { showPrompt(it) }
        })
    }

    private fun initErrorView(viewModel: ListViewModel) {
        viewModel.getError().observe(this, Observer {
            it?.let {
                errorView.text = if (it.isNotEmpty()) it else getString(R.string.prompt_select_countries)
                list.hide()
                errorView.show()
            }
        })
    }

    private fun showPrompt(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    open fun initSwipeToRefresh(viewModel: ListViewModel) {
        viewModel.getLoading().observe(this, Observer {
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