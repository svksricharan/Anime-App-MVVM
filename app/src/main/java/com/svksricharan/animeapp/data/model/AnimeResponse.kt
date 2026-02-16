package com.svksricharan.animeapp.data.model

import com.google.gson.annotations.SerializedName

// DTOs that map 1:1 to Jikan API JSON responses.
// These stay in the data layer — UI never touches them directly.
// See Mappers.kt for DTO -> Domain conversions.

data class TopAnimeResponse(
    @SerializedName("pagination") val pagination: Pagination?,
    @SerializedName("data") val data: List<AnimeDto>
)

data class AnimeDetailResponse(
    @SerializedName("data") val data: AnimeDto
)

data class Pagination(
    @SerializedName("last_visible_page") val lastVisiblePage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("items") val items: PaginationItems?
)

data class PaginationItems(
    @SerializedName("count") val count: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("per_page") val perPage: Int
)

data class AnimeDto(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("url") val url: String?,
    @SerializedName("images") val images: AnimeImages?,
    @SerializedName("trailer") val trailer: Trailer?,
    @SerializedName("title") val title: String,
    @SerializedName("title_english") val titleEnglish: String?,
    @SerializedName("title_japanese") val titleJapanese: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("episodes") val episodes: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("airing") val airing: Boolean?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("score") val score: Double?,
    @SerializedName("scored_by") val scoredBy: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("popularity") val popularity: Int?,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("season") val season: String?,
    @SerializedName("year") val year: Int?,
    @SerializedName("genres") val genres: List<Genre>?,
    @SerializedName("themes") val themes: List<Genre>?,
    @SerializedName("demographics") val demographics: List<Genre>?
)

data class AnimeImages(
    @SerializedName("jpg") val jpg: ImageUrls?,
    @SerializedName("webp") val webp: ImageUrls?
)

data class ImageUrls(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("small_image_url") val smallImageUrl: String?,
    @SerializedName("large_image_url") val largeImageUrl: String?
)

// Jikan gives us 3 different trailer fields with varying availability.
// We resolve which one to use in Mappers.kt (see resolveTrailerAction).
data class Trailer(
    @SerializedName("youtube_id") val youtubeId: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("embed_url") val embedUrl: String?,
    @SerializedName("images") val images: TrailerImages?
)

data class TrailerImages(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("small_image_url") val smallImageUrl: String?,
    @SerializedName("medium_image_url") val mediumImageUrl: String?,
    @SerializedName("large_image_url") val largeImageUrl: String?,
    @SerializedName("maximum_image_url") val maximumImageUrl: String?
)

data class Genre(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String?
)
