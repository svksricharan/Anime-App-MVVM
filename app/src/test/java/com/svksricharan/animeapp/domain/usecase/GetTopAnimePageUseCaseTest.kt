package com.svksricharan.animeapp.domain.usecase

import com.svksricharan.animeapp.domain.model.PaginatedResult
import com.svksricharan.animeapp.domain.repository.AnimeRepository
import com.svksricharan.animeapp.testing.TestAnimeFactory
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class GetTopAnimePageUseCaseTest {

    private val repository: AnimeRepository = mock()
    private val useCase = GetTopAnimePageUseCase(repository)

    @Test
    fun invoke_delegatesToRepository() = runTest {
        val page = 2
        val forceRefresh = true
        val expected = Result.success(
            PaginatedResult(
                animeList = listOf(TestAnimeFactory.anime()),
                currentPage = page,
                hasNextPage = false
            )
        )
        `when`(repository.getTopAnime(eq(page), eq(forceRefresh))).thenReturn(flowOf(expected))

        val emitted = mutableListOf<Result<PaginatedResult>>()
        useCase(page, forceRefresh).collect { emitted += it }

        verify(repository).getTopAnime(page, forceRefresh)
        assertEquals(listOf(expected), emitted)
    }
}
