package com.svksricharan.animeapp.ui.animedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.svksricharan.animeapp.data.repository.AnimeRepository
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.utils.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Fetches detail for a single anime. The animeId is passed via Factory
// so each detail screen gets its own ViewModel instance with the right ID.
class AnimeDetailViewModel(
    private val repository: AnimeRepository,
    private val animeId: Int
) : ViewModel() {

    private val _animeState = MutableStateFlow<UiState<Anime>>(UiState.Loading)
    val animeState: StateFlow<UiState<Anime>> = _animeState.asStateFlow()

    private var detailJob: Job? = null

    init {
        fetchAnimeDetail()
    }

    fun fetchAnimeDetail() {
        detailJob?.cancel()  // cancel any in-flight request before starting a new one
        detailJob = viewModelScope.launch {
            _animeState.value = UiState.Loading
            repository.getAnimeDetail(animeId).collect { result ->
                result.fold(
                    onSuccess = { anime ->
                        _animeState.value = UiState.Success(anime)
                    },
                    onFailure = { error ->
                        _animeState.value = UiState.Error(
                            error.message ?: "Failed to load anime details"
                        )
                    }
                )
            }
        }
    }

    class Factory(
        private val repository: AnimeRepository,
        private val animeId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnimeDetailViewModel::class.java)) {
                return AnimeDetailViewModel(repository, animeId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
