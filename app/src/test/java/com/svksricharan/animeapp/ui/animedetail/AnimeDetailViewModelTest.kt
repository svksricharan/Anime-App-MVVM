package com.svksricharan.animeapp.ui.animedetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import com.svksricharan.animeapp.domain.usecase.GetAnimeDetailUseCase
import com.svksricharan.animeapp.testing.MainDispatcherRule
import com.svksricharan.animeapp.testing.TestAnimeFactory
import com.svksricharan.animeapp.utils.UiState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AnimeDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getAnimeDetailUseCase: GetAnimeDetailUseCase = mock()

    @Test
    fun init_emitsSuccessWhenUseCaseSucceeds() = runTest {
        val anime = TestAnimeFactory.anime(id = 7, title = "Detail Title")
        `when`(getAnimeDetailUseCase.invoke(anyInt())).thenReturn(
            flowOf(Result.success(anime))
        )

        val viewModel = AnimeDetailViewModel(getAnimeDetailUseCase, animeId = 7)

        assertEquals(UiState.Success(anime), viewModel.animeState.value)
    }

    @Test
    fun init_emitsErrorWhenUseCaseFails() = runTest {
        `when`(getAnimeDetailUseCase.invoke(anyInt())).thenReturn(
            flowOf(Result.failure(Exception("boom")))
        )

        val viewModel = AnimeDetailViewModel(getAnimeDetailUseCase, animeId = 1)

        val state = viewModel.animeState.value
        assertTrue(state is UiState.Error)
        assertEquals("boom", (state as UiState.Error).message)
    }

    @Test
    fun factory_createsAnimeDetailViewModel() {
        val anime = TestAnimeFactory.anime(id = 99)
        `when`(getAnimeDetailUseCase.invoke(anyInt())).thenReturn(
            flowOf(Result.success(anime))
        )

        val factory = AnimeDetailViewModel.Factory(getAnimeDetailUseCase, animeId = 99)
        val vm = factory.create(AnimeDetailViewModel::class.java)

        val state = vm.animeState.value
        assertTrue(state is UiState.Success)
        assertEquals(99, (state as UiState.Success).data.id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun factory_throwsForUnknownModelClass() {
        val factory = AnimeDetailViewModel.Factory(getAnimeDetailUseCase, animeId = 1)
        factory.create(DummyViewModel::class.java)
    }

    private class DummyViewModel : ViewModel()
}
