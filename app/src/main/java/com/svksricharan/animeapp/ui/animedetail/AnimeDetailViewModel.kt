package com.svksricharan.animeapp.ui.animedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.usecase.GetAnimeDetailUseCase
import com.svksricharan.animeapp.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimeDetailViewModel(
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase,
    private val animeId: Int
) : ViewModel() {

    private val _animeState = MutableStateFlow<UiState<Anime>>(UiState.Loading)
    val animeState: StateFlow<UiState<Anime>> = _animeState.asStateFlow()

    init {
        fetchAnimeDetail()
    }

    fun fetchAnimeDetail() {
        viewModelScope.launch {
            _animeState.value = UiState.Loading
            getAnimeDetailUseCase(animeId).collect { result ->
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
        private val getAnimeDetailUseCase: GetAnimeDetailUseCase,
        private val animeId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnimeDetailViewModel::class.java)) {
                return AnimeDetailViewModel(getAnimeDetailUseCase, animeId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
