package com.svksricharan.animeapp

import android.app.Application
import com.svksricharan.animeapp.di.AppContainer

// Custom Application class — the only place where we initialize the DI container.
// Activities grab it via (application as AnimeApplication).appContainer
class AnimeApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
