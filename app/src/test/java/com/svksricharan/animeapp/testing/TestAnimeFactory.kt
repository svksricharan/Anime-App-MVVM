package com.svksricharan.animeapp.testing

import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.model.TrailerAction

object TestAnimeFactory {
    fun anime(
        id: Int = 1,
        title: String = "Test Anime"
    ): Anime = Anime(
        id = id,
        title = title,
        titleJapanese = null,
        imageUrl = null,
        largeImageUrl = null,
        score = 9.0,
        episodes = 24,
        type = "TV",
        status = "Finished Airing",
        airing = false,
        rank = id,
        rating = "PG-13",
        synopsis = "Test synopsis",
        genres = listOf("Action"),
        trailerAction = TrailerAction.None
    )
}
