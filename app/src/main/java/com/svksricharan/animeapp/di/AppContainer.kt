package com.svksricharan.animeapp.di

import android.content.Context
import com.svksricharan.animeapp.data.api.JikanApiService
import com.svksricharan.animeapp.data.local.AnimeDatabase
import com.svksricharan.animeapp.data.local.dao.AnimeDao
import com.svksricharan.animeapp.data.repository.AnimeRepositoryImpl
import com.svksricharan.animeapp.domain.repository.AnimeRepository
import com.svksricharan.animeapp.domain.usecase.GetAnimeDetailUseCase
import com.svksricharan.animeapp.domain.usecase.GetTopAnimePageUseCase
import com.svksricharan.animeapp.domain.usecase.ObserveNetworkConnectivityUseCase
import com.svksricharan.animeapp.utils.NetworkHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    companion object {
        const val BASE_URL = "https://api.jikan.moe/v4/"
    }

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

    private val networkHelper: NetworkHelper = NetworkHelper(context)

    private val animeRepository: AnimeRepository = AnimeRepositoryImpl(
        apiService = apiService,
        animeDao = animeDao,
        networkMonitor = networkHelper
    )

    val getTopAnimePageUseCase = GetTopAnimePageUseCase(animeRepository)
    val getAnimeDetailUseCase = GetAnimeDetailUseCase(animeRepository)
    val observeNetworkConnectivityUseCase =
        ObserveNetworkConnectivityUseCase(networkHelper)
}
