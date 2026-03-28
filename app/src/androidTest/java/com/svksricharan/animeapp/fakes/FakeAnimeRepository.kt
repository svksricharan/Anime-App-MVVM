package com.svksricharan.animeapp.fakes

import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.model.PaginatedResult
import com.svksricharan.animeapp.domain.repository.AnimeRepository
import com.svksricharan.animeapp.support.TestAnimeFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAnimeRepository(
    private val firstPageAnime: List<Anime>,
    private val secondPageAnime: List<Anime> = emptyList(),
    private val detailFallback: (Int) -> Anime = { id -> TestAnimeFactory.anime(id = id) }
) : AnimeRepository {

    override fun getTopAnime(page: Int, forceRefresh: Boolean): Flow<Result<PaginatedResult>> = flow {
        when (page) {
            1 -> emit(
                Result.success(
                    PaginatedResult(
                        animeList = firstPageAnime,
                        currentPage = 1,
                        hasNextPage = secondPageAnime.isNotEmpty()
                    )
                )
            )
            2 -> emit(
                Result.success(
                    PaginatedResult(
                        animeList = secondPageAnime,
                        currentPage = 2,
                        hasNextPage = false
                    )
                )
            )
            else -> emit(Result.failure(IllegalStateException("Unstubbed page: $page")))
        }
    }

    override fun getAnimeDetail(animeId: Int): Flow<Result<Anime>> = flow {
        emit(Result.success(detailFallback(animeId)))
    }
}
