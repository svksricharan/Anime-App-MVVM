package com.svksricharan.animeapp.domain.model

data class PaginatedResult(
    val animeList: List<Anime>,
    val currentPage: Int,
    val hasNextPage: Boolean
)
