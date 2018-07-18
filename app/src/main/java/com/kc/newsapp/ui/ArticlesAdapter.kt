package com.kc.newsapp.ui

import android.arch.lifecycle.LiveData
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.kc.newsapp.R
import com.kc.newsapp.data.model.Article
import com.kc.newsapp.data.model.Articles
import com.kc.newsapp.util.Util
import com.kc.newsapp.util.hide
import com.kc.newsapp.util.show
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.view_article_tile.view.*
import java.text.SimpleDateFormat
import java.util.*

class ArticlesAdapter(private val bookmarksLiveData: LiveData<Set<String>>,
                      private val toggle: (Int, String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var articleList: List<Article> = emptyList()
    var data: Articles = Articles()
    set(value) {
        articleList = value.articles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ArticleVH.create(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ArticleVH).itemView.apply {
            source.text = articleList[position].source.name
            title.text = articleList[position].title
            description.text = articleList[position].description
            published_at.text = Util.formatTimestamp(articleList[position].publishedAt,
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                    TimeZone.getTimeZone("Australia/Sydney"))
            //log("${articleList[position].title} load ${articleList[position].urlToImage}")
            image.load(articleList[position].urlToImage, progress)
            bookmark.setImageResource(if (bookmarksLiveData.value?.contains(articleList[position].title) == true)
                R.drawable.bookmarked else R.drawable.unbookmarked )
            bookmark.setOnClickListener { toggle(position, articleList[position].title) }
        }
    }

    override fun getItemCount(): Int = articleList.size

    class ArticleVH(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun create(parent: ViewGroup) =
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.view_article_tile, parent, false)
                            .let { ArticleVH(it) }
        }
    }
}

fun log(msg: String) = println("Kai: [${Thread.currentThread().name}] $msg")

fun ImageView.load(url: String?, progressBar: ProgressBar? = null) {
    if (!TextUtils.isEmpty(url)) {
        show()
        progressBar?.show()
        Picasso.with(context).load(url).into(this, object : Callback {
            override fun onSuccess() {
                progressBar?.hide()
            }
            override fun onError() {
                progressBar?.hide()
            }
        })
    } else {
        hide()
    }
}
