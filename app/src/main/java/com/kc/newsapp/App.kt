package com.kc.newsapp

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.kc.newsapp.di.AppComponent
import com.kc.newsapp.di.AppModule
import com.kc.newsapp.di.DaggerAppComponent
import io.fabric.sdk.android.Fabric


class App : Application() {

    companion object {
        @JvmStatic lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this)).build()

        Fabric.Builder(this).kits(Crashlytics()).debuggable(true).build().let {
            Fabric.with(it)
        }
    }
}