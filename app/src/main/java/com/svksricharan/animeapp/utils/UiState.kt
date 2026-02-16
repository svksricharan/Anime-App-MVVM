package com.svksricharan.animeapp.utils

// Generic UI state wrapper — every screen uses this pattern.
// Keeps the ViewModel -> UI contract consistent across the app.
sealed interface UiState<out T> {
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
    object Loading : UiState<Nothing>
}
