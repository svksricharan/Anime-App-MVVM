package com.svksricharan.animeapp.domain.usecase

import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow

class GetAnimeDetailUseCase(
    private val repository: AnimeRepository
) {
    operator fun invoke(animeId: Int): Flow<Result<Anime>> =
        repository.getAnimeDetail(animeId)
}
