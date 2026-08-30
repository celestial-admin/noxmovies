package com.example.data.watchmode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WatchmodeSearchResponse(
    @Json(name = "title_results") val titleResults: List<WatchmodeSearchResult>? = emptyList(),
    @Json(name = "results") val results: List<WatchmodeSearchResult>? = null
)

@JsonClass(generateAdapter = true)
data class WatchmodeSearchResult(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "type") val type: String? = "movie",
    @Json(name = "year") val year: Int? = null,
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "tmdb_id") val tmdbId: Long? = null,
    @Json(name = "tmdb_type") val tmdbType: String? = null,
    @Json(name = "has_details") val hasDetails: Boolean? = true,
    @Json(name = "result_type") val resultType: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "user_rating") val userRating: Double? = null
)

@JsonClass(generateAdapter = true)
data class WatchmodeTitleDetails(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String? = null,
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "plot_overview") val plotOverview: String? = null,
    @Json(name = "type") val type: String? = "movie",
    @Json(name = "runtime_minutes") val runtimeMinutes: Int? = null,
    @Json(name = "year") val year: Int? = null,
    @Json(name = "end_year") val endYear: Int? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "tmdb_id") val tmdbId: Long? = null,
    @Json(name = "tmdb_type") val tmdbType: String? = null,
    @Json(name = "genres") val genres: List<Int>? = emptyList(),
    @Json(name = "genre_names") val genreNames: List<String>? = emptyList(),
    @Json(name = "user_rating") val userRating: Double? = null,
    @Json(name = "critic_score") val criticScore: Int? = null,
    @Json(name = "us_rating") val usRating: String? = null,
    @Json(name = "poster") val poster: String? = null,
    @Json(name = "backdrop") val backdrop: String? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "trailer") val trailer: String? = null,
    @Json(name = "trailer_thumbnail") val trailerThumbnail: String? = null,
    @Json(name = "similar_titles") val similarTitles: List<Long>? = emptyList(),
    @Json(name = "sources") val sources: List<WatchmodeSourceItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class WatchmodeSourceItem(
    @Json(name = "source_id") val sourceId: Long? = null,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String, // sub, free, rent, buy, tve
    @Json(name = "region") val region: String? = null,
    @Json(name = "ios_url") val iosUrl: String? = null,
    @Json(name = "android_url") val androidUrl: String? = null,
    @Json(name = "web_url") val webUrl: String? = null,
    @Json(name = "format") val format: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "seasons") val seasons: Int? = null,
    @Json(name = "episodes") val episodes: Int? = null
)

@JsonClass(generateAdapter = true)
data class WatchmodeListTitlesResponse(
    @Json(name = "titles") val titles: List<WatchmodeSearchResult>? = emptyList(),
    @Json(name = "page") val page: Int? = 1,
    @Json(name = "total_results") val totalResults: Int? = 0,
    @Json(name = "total_pages") val totalPages: Int? = 1
)

sealed class WatchmodeResult<out T> {
    data class Success<out T>(val data: T) : WatchmodeResult<T>()
    data class Error(val errorType: ErrorType, val message: String) : WatchmodeResult<Nothing>()
    
    enum class ErrorType {
        NETWORK_ERROR,
        RATE_LIMITED,
        AUTH_CONFIGURATION_ERROR,
        NOT_FOUND,
        UNKNOWN
    }
}
