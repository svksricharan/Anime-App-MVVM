package com.svksricharan.animeapp.di

import android.content.Context
import com.svksricharan.animeapp.data.api.JikanApiService
import com.svksricharan.animeapp.data.local.AnimeDatabase
import com.svksricharan.animeapp.data.local.dao.AnimeDao
import com.svksricharan.animeapp.data.repository.AnimeRepository
import com.svksricharan.animeapp.utils.NetworkHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Manual DI container — kept it simple instead of Dagger/Hilt since the
// dependency graph is small. Everything here is effectively a singleton
// because AppContainer itself is created once in AnimeApplication.
class AppContainer(context: Context) {

    companion object {
        const val BASE_URL = "https://api.jikan.moe/v4/"
    }

    // BODY level logs full request/response — helpful during development,
    // would switch to BASIC or NONE for release builds
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: JikanApiService = retrofit.create(JikanApiService::class.java)

    private val database: AnimeDatabase = AnimeDatabase.getInstance(context)

    val animeDao: AnimeDao = database.animeDao()

    val networkHelper: NetworkHelper = NetworkHelper(context)

    val animeRepository: AnimeRepository = AnimeRepository(
        apiService = apiService,
        animeDao = animeDao,
        networkHelper = networkHelper
    )
}
