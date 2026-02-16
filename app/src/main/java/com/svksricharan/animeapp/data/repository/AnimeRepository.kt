package com.svksricharan.animeapp.data.repository

import com.svksricharan.animeapp.data.api.JikanApiService
import com.svksricharan.animeapp.data.local.dao.AnimeDao
import com.svksricharan.animeapp.data.model.PaginatedResult
import com.svksricharan.animeapp.data.model.toDomain
import com.svksricharan.animeapp.data.model.toEntity
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.utils.NetworkHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Single source of truth for anime data.
// Strategy: network-first, fall back to Room cache if offline or on error.
// Returns domain models (Anime) — DTOs and entities don't leak past this layer.
class AnimeRepository(
    private val apiService: JikanApiService,
    private val animeDao: AnimeDao,
    private val networkHelper: NetworkHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // Mutexes prevent race conditions when multiple coroutines try to
    // read/write the cache at the same time (e.g. rapid pagination)
    private val topAnimeMutex = Mutex()
    private val detailMutex = Mutex()

    // Flow wrapper so the ViewModel can collect reactively.
    // The actual work happens inside the mutex to avoid concurrent cache writes.
    fun getTopAnime(
        page: Int = 1,
        forceRefresh: Boolean = false
    ): Flow<Result<PaginatedResult>> =
        flow {
            val result = topAnimeMutex.withLock {
                fetchTopAnimeInternal(page, forceRefresh)
            }
            emit(result)
        }.flowOn(ioDispatcher)

    fun getAnimeDetail(animeId: Int): Flow<Result<Anime>> =
        flow {
            val result = detailMutex.withLock {
                fetchAnimeDetailInternal(animeId)
            }
            emit(result)
        }.flowOn(ioDispatcher)

    // ── Top Anime (paginated) ────────────────────────────────────────

    private suspend fun fetchTopAnimeInternal(
        page: Int,
        forceRefresh: Boolean
    ): Result<PaginatedResult> {
        return try {
            if (networkHelper.isNetworkAvailable()) {
                val response = apiService.getTopAnime(page = page)
                val dtos = response.data
                val hasNextPage = response.pagination?.hasNextPage ?: false

                // Cache the results in Room with their page number
                val entities = dtos.map { it.toEntity(page) }
                if (page == 1 && forceRefresh) {
                    animeDao.deleteAllAnime()  // wipe stale cache on manual refresh
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

    // ── Detail ───────────────────────────────────────────────────────

    private suspend fun fetchAnimeDetailInternal(animeId: Int): Result<Anime> {
        return try {
            if (networkHelper.isNetworkAvailable()) {
                val response = apiService.getAnimeDetail(animeId)
                val dto = response.data
                // Preserve the page number from the list — detail API doesn't know about pages
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
