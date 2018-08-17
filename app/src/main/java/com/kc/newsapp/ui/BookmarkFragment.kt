package com.kc.newsapp.ui

import android.arch.lifecycle.Observer
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.kc.newsapp.R
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.util.hide
import com.kc.newsapp.util.show
import com.kc.newsapp.util.updateBookmarkContent
import com.kc.newsapp.util.updateBookmarkKeys
import com.kc.newsapp.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.activity_article_list.*


class BookmarkFragment : ArticleListFragment() {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun initRecyclerView(viewModel: ListViewModel) {
        rvAdapter = ArticlesAdapter(viewModel.bookmarks, { url -> openArticle(url) }, true) {
            position, article ->
            rvAdapter.apply {
                list.removeAt(position)
                notifyItemRemoved(position)
                notifyItemChanged(position)
            }
            handler.postDelayed({
                viewModel.updateBookmarkKeys(article.title)
                viewModel.updateBookmarkContent(article)
            }, 1000)
        }
        LinearLayoutManager(activity).let {
            list.layoutManager = it
            list.addItemDecoration(DividerItemDecoration(list.context, it.orientation))
        }
        list.adapter = rvAdapter

        viewModel.bookmarkArticleList.observe(this, Observer {
            if (it?.isNotEmpty() == true) {
                rvAdapter.data = Articles(articles = it)
                list.show()
                errorView.hide()
            } else {
                errorView.text = getString(R.string.prompt_empty_bookmark)
            }
        })
    }

    override fun initSwipeToRefresh(viewModel: ListViewModel) {
        swipeRefresh.isEnabled = false
    }

    companion object {
        const val TAG = "BookmarkFragment"
    }
}