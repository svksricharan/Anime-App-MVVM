package com.svksricharan.animeapp.domain.repository

import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.model.PaginatedResult
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    fun getTopAnime(page: Int = 1, forceRefresh: Boolean = false): Flow<Result<PaginatedResult>>
    fun getAnimeDetail(animeId: Int): Flow<Result<Anime>>
}
