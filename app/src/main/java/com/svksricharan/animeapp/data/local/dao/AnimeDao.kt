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

    @Query("SELECT * FROM anime WHERE page <= :upToPage ORDER BY page ASC, rank ASC")
    suspend fun getAnimeUpToPage(upToPage: Int): List<AnimeEntity>

    @Query("SELECT MAX(page) FROM anime")
    suspend fun getMaxCachedPage(): Int?

    @Query("SELECT * FROM anime WHERE malId = :malId")
    suspend fun getAnimeById(malId: Int): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnime(anime: List<AnimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntity)

    @Query("DELETE FROM anime")
    suspend fun deleteAllAnime()
}
