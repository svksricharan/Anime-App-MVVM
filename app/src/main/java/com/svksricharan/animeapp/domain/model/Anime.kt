package com.svksricharan.animeapp.domain.model

// Domain model that the UI layer works with.
// Intentionally has no Gson/@SerializedName or Room/@Entity annotations
// so the UI doesn't depend on the API shape or DB schema.
data class Anime(
    val id: Int,
    val title: String,
    val titleJapanese: String?,
    val imageUrl: String?,
    val largeImageUrl: String?,
    val score: Double?,
    val episodes: Int?,
    val type: String?,
    val status: String?,
    val airing: Boolean?,
    val rank: Int?,
    val rating: String?,
    val synopsis: String?,
    val genres: List<String>,
    val trailerAction: TrailerAction
)

// Sealed class so the UI can pattern-match exhaustively.
// The resolution logic (which tier to use) lives in Mappers.kt,
// so the UI just does when(trailerAction) and handles each case.
//
// Priority: youtube_id → url → embed_url (unusable on Android) → none
sealed class TrailerAction {
    data class InternalPlayer(val youtubeId: String) : TrailerAction()
    data class ExternalLink(val url: String) : TrailerAction()
    data class Unavailable(val embedUrl: String) : TrailerAction()
    object None : TrailerAction()
}
