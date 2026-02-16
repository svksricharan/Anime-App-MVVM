package com.svksricharan.animeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.svksricharan.animeapp.data.local.entity.AnimeEntity

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime ORDER BY page ASC, rank ASC")
    suspend fun getAllAnime(): List<AnimeEntity>

    // Returns all cached anime up to a given page — this is how we serve
    // the accumulated list from cache when offline.
    @Query("SELECT * FROM anime WHERE page <= :upToPage ORDER BY page ASC, rank ASC")
    suspend fun getAnimeUpToPage(upToPage: Int): List<AnimeEntity>

    // Needed to figure out how many pages we've already cached
    @Query("SELECT MAX(page) FROM anime")
    suspend fun getMaxCachedPage(): Int?

    @Query("SELECT * FROM anime WHERE malId = :malId")
    suspend fun getAnimeById(malId: Int): AnimeEntity?

    // REPLACE strategy so re-fetching the same page updates the cache instead of duplicating
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnime(anime: List<AnimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntity)

    @Query("DELETE FROM anime")
    suspend fun deleteAllAnime()
}
