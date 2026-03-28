package com.svksricharan.animeapp.domain.usecase

import com.svksricharan.animeapp.domain.model.PaginatedResult
import com.svksricharan.animeapp.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow

class GetTopAnimePageUseCase(
    private val repository: AnimeRepository
) {
    operator fun invoke(page: Int, forceRefresh: Boolean): Flow<Result<PaginatedResult>> =
        repository.getTopAnime(page, forceRefresh)
}
