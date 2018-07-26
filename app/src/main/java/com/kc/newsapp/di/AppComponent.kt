package com.kc.newsapp.di

import com.kc.newsapp.ui.ArticleListActivity
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(activity: ArticleListActivity)
}