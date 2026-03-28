package com.svksricharan.animeapp.support

import com.svksricharan.animeapp.domain.model.Anime
import com.svksricharan.animeapp.domain.model.TrailerAction

object TestAnimeFactory {
    fun anime(
        id: Int = 1,
        title: String = "Instrumented Anime"
    ): Anime = Anime(
        id = id,
        title = title,
        titleJapanese = null,
        imageUrl = null,
        largeImageUrl = null,
        score = 8.5,
        episodes = 12,
        type = "TV",
        status = "Finished Airing",
        airing = false,
        rank = id,
        rating = "PG-13",
        synopsis = "Synopsis",
        genres = listOf("Action"),
        trailerAction = TrailerAction.None
    )
}
