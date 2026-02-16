package com.svksricharan.animeapp.data.api

import com.svksricharan.animeapp.data.model.AnimeDetailResponse
import com.svksricharan.animeapp.data.model.TopAnimeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Only two endpoints needed — list and detail.
interface JikanApiService {

    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25     // Jikan default is 25, making it explicit
    ): TopAnimeResponse

    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") animeId: Int
    ): AnimeDetailResponse
}
