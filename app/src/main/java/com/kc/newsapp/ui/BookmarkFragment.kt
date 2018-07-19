package com.kc.newsapp.ui

import android.arch.lifecycle.Observer
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.util.hide
import com.kc.newsapp.util.show
import com.kc.newsapp.util.updateBookmarkKeys
import com.kc.newsapp.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.activity_article_list.*


class BookmarkFragment : ArticleListFragment() {

    override fun initRecyclerView(viewModel: ListViewModel) {
        rvAdapter = ArticlesAdapter(viewModel.bookmarks, { url -> openArticle(url) }) {
            position, article ->
            viewModel.sharedPreferences.updateBookmarkKeys(article.title)
            rvAdapter.notifyItemChanged(position)
        }
        LinearLayoutManager(activity).let {
            list.layoutManager = it
            list.addItemDecoration(DividerItemDecoration(list.context, it.orientation))
        }
        list.adapter = rvAdapter

        viewModel.bookmarkArticleList.observe(this, Observer {
            it?.let {
                rvAdapter.data = Articles(articles = it)
                list.show()
                errorView.hide()
            }
        })
    }

    companion object {
        const val TAG = "BookmarkFragment"
    }
}