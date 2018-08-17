package com.kc.newsapp.di

import com.kc.newsapp.ui.ArticleListActivity
import com.kc.newsapp.viewmodel.ListViewModel
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [AppModule::class, DataModule::class])
interface AppComponent {

    fun inject(activity: ArticleListActivity)
}