package com.svksricharan.animeapp

import android.app.Application
import com.svksricharan.animeapp.di.AppContainer

class AnimeApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
