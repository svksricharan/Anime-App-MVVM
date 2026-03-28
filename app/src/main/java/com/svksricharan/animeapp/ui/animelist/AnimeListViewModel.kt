package com.svksricharan.animeapp.ui.animelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.usecase.GetTopAnimePageUseCase
import com.svksricharan.animeapp.domain.usecase.ObserveNetworkConnectivityUseCase
import com.svksricharan.animeapp.utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimeListViewModel(
    private val getTopAnimePageUseCase: GetTopAnimePageUseCase,
    private val observeNetworkConnectivityUseCase: ObserveNetworkConnectivityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Anime>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Anime>>> = _uiState.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _paginationError = MutableStateFlow(false)
    val paginationError: StateFlow<Boolean> = _paginationError.asStateFlow()

    private var currentPage = 1
    private var hasNextPage = true
    private var fetchJob: Job? = null
    private var paginationJob: Job? = null

    private val allAnime = mutableListOf<Anime>()

    init {
        fetchTopAnime(forceRefresh = false)
        observeNetworkState()
    }

    fun fetchTopAnime(forceRefresh: Boolean = false) {
        fetchJob?.cancel()
        paginationJob?.cancel()
        currentPage = 1
        hasNextPage = true
        allAnime.clear()
        _paginationError.value = false

        fetchJob = viewModelScope.launch {
            _uiState.value = UiState.Loading

            getTopAnimePageUseCase(page = 1, forceRefresh = forceRefresh).collect { result ->
                result.fold(
                    onSuccess = { paginated ->
                        allAnime.addAll(paginated.animeList)
                        currentPage = paginated.currentPage
                        hasNextPage = paginated.hasNextPage
                        _uiState.value = UiState.Success(allAnime.toList())
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error(
                            error.message ?: "An unknown error occurred"
                        )
                    }
                )
            }
        }
    }

    fun loadNextPage() {
        if (!hasNextPage || _isLoadingMore.value) return

        _paginationError.value = false

        paginationJob = viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1

            getTopAnimePageUseCase(page = nextPage, forceRefresh = false).collect { result ->
                result.fold(
                    onSuccess = { paginated ->
                        val newItems = paginated.animeList.filter { anime ->
                            allAnime.none { it.id == anime.id }
                        }
                        allAnime.addAll(newItems)
                        currentPage = paginated.currentPage
                        hasNextPage = paginated.hasNextPage
                        _uiState.value = UiState.Success(allAnime.toList())
                    },
                    onFailure = {
                        _paginationError.value = true
                    }
                )
                _isLoadingMore.value = false
            }
        }
    }

    fun retryNextPage() {
        _paginationError.value = false
        loadNextPage()
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            observeNetworkConnectivityUseCase().collect { isConnected ->
                _isOffline.value = !isConnected
                if (isConnected && _uiState.value is UiState.Error) {
                    fetchTopAnime(forceRefresh = true)
                }
            }
        }
    }

    class Factory(
        private val getTopAnimePageUseCase: GetTopAnimePageUseCase,
        private val observeNetworkConnectivityUseCase: ObserveNetworkConnectivityUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnimeListViewModel::class.java)) {
                return AnimeListViewModel(
                    getTopAnimePageUseCase,
                    observeNetworkConnectivityUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
