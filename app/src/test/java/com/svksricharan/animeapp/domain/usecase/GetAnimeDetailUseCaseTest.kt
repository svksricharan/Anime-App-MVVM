package com.svksricharan.animeapp.domain.usecase

import com.svksricharan.animeapp.domain.model.Anime
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

class GetAnimeDetailUseCaseTest {

    private val repository: AnimeRepository = mock()
    private val useCase = GetAnimeDetailUseCase(repository)

    @Test
    fun invoke_delegatesToRepository() = runTest {
        val animeId = 42
        val expected = Result.success(TestAnimeFactory.anime(id = animeId))
        `when`(repository.getAnimeDetail(eq(animeId))).thenReturn(flowOf(expected))

        val emitted = mutableListOf<Result<Anime>>()
        useCase(animeId).collect { emitted += it }

        verify(repository).getAnimeDetail(animeId)
        assertEquals(listOf(expected), emitted)
    }
}
