package com.svksricharan.animeapp.ui.animelist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.model.PaginatedResult
import com.svksricharan.animeapp.domain.usecase.GetTopAnimePageUseCase
import com.svksricharan.animeapp.domain.usecase.ObserveNetworkConnectivityUseCase
import com.svksricharan.animeapp.testing.MainDispatcherRule
import com.svksricharan.animeapp.testing.TestAnimeFactory
import com.svksricharan.animeapp.utils.UiState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AnimeListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTopAnimePageUseCase: GetTopAnimePageUseCase = mock()
    private val observeNetworkConnectivityUseCase: ObserveNetworkConnectivityUseCase = mock()

    @Test
    fun init_success_emitsSuccessWithAnimeList() = runTest {
        val anime = TestAnimeFactory.anime(id = 1, title = "One")
        stubNetworkOnline()
        `when`(getTopAnimePageUseCase.invoke(anyInt(), anyBoolean())).thenReturn(
            flowOf(
                Result.success(
                    PaginatedResult(
                        animeList = listOf(anime),
                        currentPage = 1,
                        hasNextPage = false
                    )
                )
            )
        )

        val viewModel = AnimeListViewModel(
            getTopAnimePageUseCase,
            observeNetworkConnectivityUseCase
        )

        assertEquals(UiState.Success(listOf(anime)), viewModel.uiState.value)
        assertFalse(viewModel.paginationError.value)
    }

    @Test
    fun init_failure_emitsErrorWithMessage() = runTest {
        stubNetworkOnline()
        `when`(getTopAnimePageUseCase.invoke(anyInt(), anyBoolean())).thenReturn(
            flowOf(Result.failure(Exception("network down")))
        )

        val viewModel = AnimeListViewModel(
            getTopAnimePageUseCase,
            observeNetworkConnectivityUseCase
        )

        assertEquals(
            UiState.Error("network down"),
            viewModel.uiState.value
        )
    }

    @Test
    fun init_failure_withoutMessage_usesFallbackMessage() = runTest {
        stubNetworkOnline()
        `when`(getTopAnimePageUseCase.invoke(anyInt(), anyBoolean())).thenReturn(
            flowOf(Result.failure(Exception()))
        )

        val viewModel = AnimeListViewModel(
            getTopAnimePageUseCase,
            observeNetworkConnectivityUseCase
        )

        assertEquals(
            UiState.Error("An unknown error occurred"),
            viewModel.uiState.value
        )
    }

    @Test
    fun loadNextPage_appendsNewItems() = runTest {
        stubNetworkOnline()
        val first = TestAnimeFactory.anime(id = 1, title = "First")
        val second = TestAnimeFactory.anime(id = 2, title = "Second")
        `when`(getTopAnimePageUseCase.invoke(1, false)).thenReturn(
            flowOf(
                Result.success(
                    PaginatedResult(
                        animeList = listOf(first),
                        currentPage = 1,
                        hasNextPage = true
                    )
                )
            )
        )
        `when`(getTopAnimePageUseCase.invoke(2, false)).thenReturn(
            flowOf(
                Result.success(
                    PaginatedResult(
                        animeList = listOf(second),
                        currentPage = 2,
                        hasNextPage = false
                    )
                )
            )
        )

        val viewModel = AnimeListViewModel(
            getTopAnimePageUseCase,
            observeNetworkConnectivityUseCase
        )
        assertEquals(UiState.Success(listOf(first)), viewModel.uiState.value)

        viewModel.loadNextPage()

        assertEquals(UiState.Success(listOf(first, second)), viewModel.uiState.value)
        assertFalse(viewModel.paginationError.value)
        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun loadNextPage_onFailure_setsPaginationError() = runTest {
        stubNetworkOnline()
        val first = TestAnimeFactory.anime(id = 1)
        `when`(getTopAnimePageUseCase.invoke(1, false)).thenReturn(
            flowOf(
                Result.success(
                    PaginatedResult(
                        animeList = listOf(first),
                        currentPage = 1,
                        hasNextPage = true
                    )
                )
            )
        )
        `when`(getTopAnimePageUseCase.invoke(2, false)).thenReturn(
            flowOf(Result.failure(Exception("page 2")))
        )

        val viewModel = AnimeListViewModel(
            getTopAnimePageUseCase,
            observeNetworkConnectivityUseCase
        )
        viewModel.loadNextPage()

        assertTrue(viewModel.paginationError.value)
        assertEquals(UiState.Success(listOf(first)), viewModel.uiState.value)
    }

    @Test
    fun factory_createsAnimeListViewModel() {
        stubNetworkOnline()
        `when`(getTopAnimePageUseCase.invoke(anyInt(), anyBoolean())).thenReturn(
            flowOf(
                Result.success(
                    PaginatedResult(
                        animeList = emptyList(),
                        currentPage = 1,
                        hasNextPage = false
                    )
                )
            )
        )

        val factory = AnimeListViewModel.Factory(
            getTopAnimePageUseCase,
            observeNetworkConnectivityUseCase
        )
        val vm = factory.create(AnimeListViewModel::class.java)
        assertEquals(UiState.Success(emptyList<Anime>()), vm.uiState.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun factory_throwsForUnknownModelClass() {
        val factory = AnimeListViewModel.Factory(
            getTopAnimePageUseCase,
            observeNetworkConnectivityUseCase
        )
        factory.create(DummyViewModel::class.java)
    }

    private fun stubNetworkOnline() {
        `when`(observeNetworkConnectivityUseCase.invoke()).thenReturn(flowOf(true))
    }

    private class DummyViewModel : ViewModel()
}
