package com.svksricharan.animeapp.data.model

import com.svksricharan.animeapp.data.local.entity.AnimeEntity
import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.model.TrailerAction

// Mapping lives here so that API changes don't ripple into the UI.
// Three conversion paths:
//   1. DTO -> Entity   (cache to Room after API call)
//   2. DTO -> Domain   (network-first path, skip Room read)
//   3. Entity -> Domain (offline fallback path)

// ── DTO → Entity (for Room caching) ─────────────────────────────────

fun AnimeDto.toEntity(page: Int = 1): AnimeEntity {
    return AnimeEntity(
        malId = malId,
        title = titleEnglish ?: title,
        titleEnglish = titleEnglish,
        titleJapanese = titleJapanese,
        episodes = episodes,
        score = score,
        synopsis = synopsis,
        rating = rating,
        imageUrl = images?.jpg?.imageUrl,
        largeImageUrl = images?.jpg?.largeImageUrl,
        trailerEmbedUrl = trailer?.embedUrl,
        trailerYoutubeId = trailer?.youtubeId,
        trailerUrl = trailer?.url,
        genres = genres?.joinToString(", ") { it.name },
        type = type,
        status = status,
        airing = airing,
        duration = duration,
        rank = rank,
        popularity = popularity,
        season = season,
        year = year,
        page = page
    )
}

// ── DTO → Domain (network-first path) ───────────────────────────────

fun AnimeDto.toDomain(): Anime {
    return Anime(
        id = malId,
        title = titleEnglish ?: title,
        titleJapanese = titleJapanese,
        imageUrl = images?.jpg?.imageUrl,
        largeImageUrl = images?.jpg?.largeImageUrl,
        score = score,
        episodes = episodes,
        type = type,
        status = status,
        airing = airing,
        rank = rank,
        rating = rating,
        synopsis = synopsis,
        genres = genres?.map { it.name } ?: emptyList(),
        trailerAction = resolveTrailerAction(trailer)
    )
}

// ── Entity → Domain (cache fallback path) ───────────────────────────

fun AnimeEntity.toDomain(): Anime {
    return Anime(
        id = malId,
        title = titleEnglish ?: title,
        titleJapanese = titleJapanese,
        imageUrl = imageUrl,
        largeImageUrl = largeImageUrl,
        score = score,
        episodes = episodes,
        type = type,
        status = status,
        airing = airing,
        rank = rank,
        rating = rating,
        synopsis = synopsis,
        genres = genres?.split(", ") ?: emptyList(),
        trailerAction = resolveTrailerAction(trailerYoutubeId, trailerUrl, trailerEmbedUrl)
    )
}

// ── Trailer priority resolution ─────────────────────────────────────
// YouTube blocks embed_url in WebViews (Error 153), so we prefer youtube_id
// or direct url which can open in the YouTube app. embed_url is treated
// as "unavailable" since it won't play reliably on Android.

private fun resolveTrailerAction(trailer: Trailer?): TrailerAction {
    return when {
        !trailer?.youtubeId.isNullOrBlank() -> TrailerAction.InternalPlayer(trailer!!.youtubeId!!)
        !trailer?.url.isNullOrBlank() -> TrailerAction.ExternalLink(trailer!!.url!!)
        !trailer?.embedUrl.isNullOrBlank() -> TrailerAction.Unavailable(trailer!!.embedUrl!!)
        else -> TrailerAction.None
    }
}

private fun resolveTrailerAction(
    youtubeId: String?,
    url: String?,
    embedUrl: String?
): TrailerAction {
    return when {
        !youtubeId.isNullOrBlank() -> TrailerAction.InternalPlayer(youtubeId)
        !url.isNullOrBlank() -> TrailerAction.ExternalLink(url)
        !embedUrl.isNullOrBlank() -> TrailerAction.Unavailable(embedUrl)
        else -> TrailerAction.None
    }
}

// ── Paginated result wrapper ────────────────────────────────────────

data class PaginatedResult(
    val animeList: List<Anime>,
    val currentPage: Int,
    val hasNextPage: Boolean
)
