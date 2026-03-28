package com.svksricharan.animeapp.data.repository

import com.svksricharan.animeapp.data.api.JikanApiService
import com.svksricharan.animeapp.data.local.dao.AnimeDao
import com.svksricharan.animeapp.data.model.toDomain
import com.svksricharan.animeapp.data.model.toEntity
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.model.PaginatedResult
import com.svksricharan.animeapp.domain.network.NetworkMonitor
import com.svksricharan.animeapp.domain.repository.AnimeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepositoryImpl(
    private val apiService: JikanApiService,
    private val animeDao: AnimeDao,
    private val networkMonitor: NetworkMonitor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AnimeRepository {

    override fun getTopAnime(
        page: Int,
        forceRefresh: Boolean
    ): Flow<Result<PaginatedResult>> =
        flow {
            emit(fetchTopAnimeInternal(page, forceRefresh))
        }.flowOn(ioDispatcher)

    override fun getAnimeDetail(animeId: Int): Flow<Result<Anime>> =
        flow {
            emit(fetchAnimeDetailInternal(animeId))
        }.flowOn(ioDispatcher)

    private suspend fun fetchTopAnimeInternal(
        page: Int,
        forceRefresh: Boolean
    ): Result<PaginatedResult> {
        return try {
            if (networkMonitor.isCurrentlyConnected()) {
                val response = apiService.getTopAnime(page = page)
                val dtos = response.data
                val hasNextPage = response.pagination?.hasNextPage ?: false

                val entities = dtos.map { it.toEntity(page) }
                if (page == 1 && forceRefresh) {
                    animeDao.deleteAllAnime()
                }
                animeDao.insertAllAnime(entities)

                Result.success(
                    PaginatedResult(
                        animeList = dtos.map { it.toDomain() },
                        currentPage = page,
                        hasNextPage = hasNextPage
                    )
                )
            } else {
                loadCachedAnimeList(page)
            }
        } catch (e: Exception) {
            loadCachedAnimeList(page, fallbackError = e)
        }
    }

    private suspend fun loadCachedAnimeList(
        page: Int,
        fallbackError: Exception? = null
    ): Result<PaginatedResult> {
        val cached = animeDao.getAnimeUpToPage(page).map { it.toDomain() }
        val maxCachedPage = animeDao.getMaxCachedPage() ?: 0
        return if (cached.isNotEmpty()) {
            Result.success(
                PaginatedResult(
                    animeList = cached,
                    currentPage = maxCachedPage,
                    hasNextPage = false
                )
            )
        } else {
            Result.failure(
                fallbackError ?: Exception("No internet connection and no cached data available")
            )
        }
    }

    private suspend fun fetchAnimeDetailInternal(animeId: Int): Result<Anime> {
        return try {
            if (networkMonitor.isCurrentlyConnected()) {
                val response = apiService.getAnimeDetail(animeId)
                val dto = response.data
                val existingPage = animeDao.getAnimeById(animeId)?.page ?: 1
                animeDao.insertAnime(dto.toEntity(existingPage))
                Result.success(dto.toDomain())
            } else {
                loadCachedAnimeDetail(animeId)
            }
        } catch (e: Exception) {
            loadCachedAnimeDetail(animeId, fallbackError = e)
        }
    }

    private suspend fun loadCachedAnimeDetail(
        animeId: Int,
        fallbackError: Exception? = null
    ): Result<Anime> {
        val cached = animeDao.getAnimeById(animeId)
        return if (cached != null) {
            Result.success(cached.toDomain())
        } else {
            Result.failure(
                fallbackError ?: Exception("No internet connection and no cached data")
            )
        }
    }
}
