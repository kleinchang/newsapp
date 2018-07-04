package com.kc.newsapp

import android.app.Application
import com.kc.newsapp.di.AppComponent
import com.kc.newsapp.di.AppModule
import com.kc.newsapp.di.DaggerAppComponent


class App : Application() {

    companion object {
        @JvmStatic lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this)).build()
    }
}