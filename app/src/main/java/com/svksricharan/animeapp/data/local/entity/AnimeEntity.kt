package com.svksricharan.animeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Flat table — genres stored as comma-separated string to avoid a junction table.
// For a bigger app I'd normalize this, but for ~25 items/page it's fine.
@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey val malId: Int,
    val title: String,
    val titleEnglish: String?,
    val titleJapanese: String?,
    val episodes: Int?,
    val score: Double?,
    val synopsis: String?,
    val rating: String?,
    val imageUrl: String?,
    val largeImageUrl: String?,
    val trailerEmbedUrl: String?,
    val trailerYoutubeId: String?,
    val trailerUrl: String?,
    val genres: String?,
    val type: String?,
    val status: String?,
    val airing: Boolean?,
    val duration: String?,
    val rank: Int?,
    val popularity: Int?,
    val season: String?,
    val year: Int?,
    val page: Int = 1,             // which API page this came from — used for paginated cache queries
    val lastUpdated: Long = System.currentTimeMillis()
)
