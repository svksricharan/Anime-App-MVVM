package com.svksricharan.animeapp.domain.model

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

sealed class TrailerAction {
    data class InternalPlayer(val youtubeId: String) : TrailerAction()
    data class ExternalLink(val url: String) : TrailerAction()
    data class Unavailable(val embedUrl: String) : TrailerAction()
    object None : TrailerAction()
}
