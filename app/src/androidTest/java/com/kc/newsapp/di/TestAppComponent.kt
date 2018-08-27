package com.kc.newsapp.di

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, MockDataModule::class])
interface TestAppComponent : AppComponent