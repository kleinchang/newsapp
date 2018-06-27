package com.kc.newsapp.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kc.newsapp.R
import com.kc.newsapp.data.model.Article
import com.kc.newsapp.data.model.Articles
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.view_article_tile.view.*

class ArticlesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            source.text = "Source: ${articleList[position].source.name}"
            title.text = "Title: ${articleList[position].title}"
            description.text = "Description: ${articleList[position].description}"
            Picasso.with(image.context).load(articleList[position].urlToImage).into(image)
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