package com.example.model

data class SearchIntentResult(
    val genres: List<String> = emptyList(),
    val mood: String = "",
    val keywords: List<String> = emptyList(),
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
    val category: String? = null, // "Movies", "Series", "Anime", or null
    val explanation: String = "",
    val matchedMovieIds: List<String> = emptyList()
)

data class NoxAiRecommendationResult(
    val aiResponseText: String,
    val recommendedMovies: List<Movie>,
    val searchCriteria: SearchIntentResult? = null,
    val isAvailable: Boolean = true
)
