package com.svksricharan.animeapp.ui.animelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.svksricharan.animeapp.data.repository.AnimeRepository
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.utils.NetworkHelper
import com.svksricharan.animeapp.utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimeListViewModel(
    private val repository: AnimeRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Anime>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Anime>>> = _uiState.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // Separate loading state for pagination so the main screen doesn't flicker
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Tracks if the last page load failed — shows retry UI at bottom of list
    private val _paginationError = MutableStateFlow(false)
    val paginationError: StateFlow<Boolean> = _paginationError.asStateFlow()

    private var currentPage = 1
    private var hasNextPage = true
    private var paginationJob: Job? = null   // separate from fetchJob so we can cancel them independently
    private var fetchJob: Job? = null

    // We accumulate items across pages here — emitted as a snapshot via toList()
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

            repository.getTopAnime(page = 1, forceRefresh = forceRefresh).collect { result ->
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
        // Guard: don't load if we're already loading or there's nothing left
        if (!hasNextPage || _isLoadingMore.value) return

        _paginationError.value = false

        paginationJob = viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1  // local var — only update currentPage on success

            repository.getTopAnime(page = nextPage, forceRefresh = false).collect { result ->
                result.fold(
                    onSuccess = { paginated ->
                        // Filter dupes — API sometimes returns overlapping items across pages
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

    // Auto-retry when connectivity comes back and we're stuck on an error screen
    private fun observeNetworkState() {
        viewModelScope.launch {
            networkHelper.observeNetworkState().collect { isConnected ->
                _isOffline.value = !isConnected
                if (isConnected && _uiState.value is UiState.Error) {
                    fetchTopAnime(forceRefresh = true)
                }
            }
        }
    }

    class Factory(
        private val repository: AnimeRepository,
        private val networkHelper: NetworkHelper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnimeListViewModel::class.java)) {
                return AnimeListViewModel(repository, networkHelper) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
