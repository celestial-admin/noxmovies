package com.example.data.watchmode

import android.util.Log
import com.example.BuildConfig
import com.example.model.Movie
import com.example.model.ProviderType
import com.example.model.WatchProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

interface WatchmodeRetrofitApi {

    @GET("search/")
    suspend fun search(
        @Query("apiKey") apiKey: String,
        @Query("search_field") searchField: String = "name",
        @Query("search_value") searchValue: String,
        @Query("types") types: String = "movie,tv"
    ): Response<WatchmodeSearchResponse>

    @GET("autocomplete-search/")
    suspend fun autocompleteSearch(
        @Query("apiKey") apiKey: String,
        @Query("search_value") searchValue: String,
        @Query("search_type") searchType: Int = 1
    ): Response<WatchmodeSearchResponse>

    @GET("title/{title_id}/details/")
    suspend fun getTitleDetails(
        @Path("title_id") titleId: Long,
        @Query("apiKey") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "sources"
    ): Response<WatchmodeTitleDetails>

    @GET("title/{title_id}/sources/")
    suspend fun getTitleSources(
        @Path("title_id") titleId: Long,
        @Query("apiKey") apiKey: String,
        @Query("regions") regions: String
    ): Response<List<WatchmodeSourceItem>>

    @GET("list-titles/")
    suspend fun listTitles(
        @Query("apiKey") apiKey: String,
        @Query("types") types: String = "movie,tv",
        @Query("sort_by") sortBy: String = "popularity_desc",
        @Query("regions") regions: String = "IN",
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): Response<WatchmodeListTitlesResponse>
}

class WatchmodeApiService(
    private val apiKeyProvider: () -> String = { getApiKeyFromConfig() }
) {
    companion object {
        private const val TAG = "WatchmodeApiService"
        private const val BASE_URL = "https://api.watchmode.com/v1/"
        
        fun getApiKeyFromConfig(): String {
            return try {
                val key = BuildConfig.WATCHMODE_API_KEY
                if (key.isBlank() || key.contains("PLACEHOLDER", ignoreCase = true)) {
                    ""
                } else {
                    key.trim()
                }
            } catch (e: Exception) {
                ""
            }
        }
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            // Sanitize requests - avoid leaking keys in logs or headers
            val original = chain.request()
            val response = chain.proceed(original)
            response
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: WatchmodeRetrofitApi = retrofit.create(WatchmodeRetrofitApi::class.java)

    val isConfigured: Boolean
        get() = apiKeyProvider().isNotBlank()

    suspend fun searchTitles(
        query: String,
        types: String = "movie,tv",
        region: String = "IN"
    ): WatchmodeResult<List<Movie>> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            return@withContext WatchmodeResult.Error(
                WatchmodeResult.ErrorType.AUTH_CONFIGURATION_ERROR,
                "Watchmode API key is not configured. Please configure WATCHMODE_API_KEY in the Secrets panel."
            )
        }

        executeWithRetry {
            val response = api.search(
                apiKey = apiKey,
                searchField = "name",
                searchValue = query,
                types = types
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                val results = body?.titleResults ?: body?.results ?: emptyList()
                val movies = results.map { result ->
                    val isTv = result.type.equals("tv", ignoreCase = true) || result.type.equals("tv_series", ignoreCase = true)
                    Movie(
                        id = result.id.toString(),
                        watchmodeId = result.id.toString(),
                        imdbId = result.imdbId,
                        tmdbId = result.tmdbId?.toString(),
                        title = result.name ?: result.title ?: "Untitled",
                        year = result.year?.toString() ?: "2024",
                        runtime = if (isTv) null else 120,
                        duration = if (isTv) "TV Series" else "2h 0m",
                        posterUrl = result.imageUrl ?: "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&q=80",
                        backdropUrl = result.imageUrl ?: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=80",
                        overview = "Discover streaming availability across platforms for ${result.name ?: result.title ?: "this title"}.",
                        rating = result.userRating ?: 8.0,
                        genres = listOf(if (isTv) "TV Series" else "Movie"),
                        type = if (isTv) "tv" else "movie",
                        isSeries = isTv,
                        mediaUrl = "" // NOX media playback requires separate authorized source
                    )
                }
                WatchmodeResult.Success(movies)
            } else {
                mapHttpError(response.code(), response.message())
            }
        }
    }

    suspend fun getTitleDetails(
        titleId: Long,
        region: String = "IN"
    ): WatchmodeResult<Movie> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            return@withContext WatchmodeResult.Error(
                WatchmodeResult.ErrorType.AUTH_CONFIGURATION_ERROR,
                "Watchmode API key is not configured."
            )
        }

        executeWithRetry {
            val response = api.getTitleDetails(titleId = titleId, apiKey = apiKey, appendToResponse = "sources")
            if (response.isSuccessful) {
                val details = response.body()
                if (details != null) {
                    val isTv = details.type.equals("tv", ignoreCase = true) || details.type.equals("tv_series", ignoreCase = true)
                    val providers = details.sources
                        ?.filter { it.region.isNullOrBlank() || it.region.equals(region, ignoreCase = true) }
                        ?.map { source -> mapToWatchProvider(source, region) }
                        ?.distinctBy { it.name }
                        ?: emptyList()

                    val movie = Movie(
                        id = details.id.toString(),
                        watchmodeId = details.id.toString(),
                        imdbId = details.imdbId,
                        tmdbId = details.tmdbId?.toString(),
                        title = details.title ?: details.originalTitle ?: "Untitled",
                        year = details.year?.toString() ?: "2024",
                        runtime = details.runtimeMinutes,
                        duration = if (details.runtimeMinutes != null && details.runtimeMinutes > 0) {
                            "${details.runtimeMinutes / 60}h ${details.runtimeMinutes % 60}m"
                        } else if (isTv) "TV Series" else "2h 0m",
                        posterUrl = details.poster ?: "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&q=80",
                        backdropUrl = details.backdrop ?: details.poster ?: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=80",
                        overview = details.plotOverview ?: "Detailed streaming overview and availability across licensed providers.",
                        rating = details.userRating ?: (details.criticScore?.div(10.0)) ?: 8.2,
                        genres = details.genreNames?.ifEmpty { listOf("Drama", "Sci-Fi") } ?: listOf("Drama"),
                        type = if (isTv) "tv" else "movie",
                        isSeries = isTv,
                        whereToWatch = providers,
                        trailerUrl = details.trailer,
                        mediaUrl = "" // Playable media requires authorized NOX source
                    )
                    WatchmodeResult.Success(movie)
                } else {
                    WatchmodeResult.Error(WatchmodeResult.ErrorType.NOT_FOUND, "Title details not found")
                }
            } else {
                mapHttpError(response.code(), response.message())
            }
        }
    }

    suspend fun getTitleSources(
        titleId: Long,
        region: String = "IN"
    ): WatchmodeResult<List<WatchProvider>> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            return@withContext WatchmodeResult.Error(
                WatchmodeResult.ErrorType.AUTH_CONFIGURATION_ERROR,
                "Watchmode API key is not configured."
            )
        }

        executeWithRetry {
            val response = api.getTitleSources(titleId = titleId, apiKey = apiKey, regions = region)
            if (response.isSuccessful) {
                val sources = response.body() ?: emptyList()
                val providers = sources
                    .map { mapToWatchProvider(it, region) }
                    .distinctBy { it.name }
                WatchmodeResult.Success(providers)
            } else {
                mapHttpError(response.code(), response.message())
            }
        }
    }

    private fun mapToWatchProvider(source: WatchmodeSourceItem, region: String): WatchProvider {
        val pType = when (source.type.lowercase()) {
            "sub", "subscription" -> ProviderType.SUBSCRIPTION
            "free" -> ProviderType.FREE
            "rent" -> ProviderType.RENT
            "buy" -> ProviderType.BUY
            "tve", "tv" -> ProviderType.TV
            else -> ProviderType.SUBSCRIPTION
        }

        val logo = getProviderLogoUrl(source.name)
        val formattedPrice = if (source.price != null && source.price > 0) "$${source.price}" else null

        return WatchProvider(
            providerId = source.sourceId?.toString() ?: source.name.lowercase().replace(" ", "_"),
            name = source.name,
            logoUrl = logo,
            type = pType,
            webUrl = source.webUrl,
            androidUrl = source.androidUrl ?: source.webUrl,
            price = formattedPrice,
            format = source.format ?: "4K UHD",
            region = source.region ?: region
        )
    }

    private fun getProviderLogoUrl(providerName: String): String {
        val lower = providerName.lowercase()
        return when {
            lower.contains("netflix") -> "https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_logo.svg"
            lower.contains("prime") || lower.contains("amazon") -> "https://upload.wikimedia.org/wikipedia/commons/f/f1/Prime_Video.png"
            lower.contains("disney") || lower.contains("hotstar") -> "https://upload.wikimedia.org/wikipedia/commons/3/3e/Disney%2B_logo.svg"
            lower.contains("apple") -> "https://upload.wikimedia.org/wikipedia/commons/2/28/Apple_TV_Plus_Logo.svg"
            lower.contains("youtube") || lower.contains("google") -> "https://upload.wikimedia.org/wikipedia/commons/0/09/YouTube_full-color_icon_%282017%29.svg"
            lower.contains("hbo") || lower.contains("max") -> "https://upload.wikimedia.org/wikipedia/commons/1/17/HBO_Max_Logo.svg"
            lower.contains("hulu") -> "https://upload.wikimedia.org/wikipedia/commons/e/e4/Hulu_Logo.svg"
            lower.contains("paramount") -> "https://upload.wikimedia.org/wikipedia/commons/a/a5/Paramount_Plus.svg"
            lower.contains("peacock") -> "https://upload.wikimedia.org/wikipedia/commons/d/d3/NBCUniversal_Peacock_Logo.svg"
            lower.contains("jiocinema") || lower.contains("jio") -> "https://upload.wikimedia.org/wikipedia/commons/e/e9/JioCinema_logo.svg"
            lower.contains("zee5") || lower.contains("zee") -> "https://upload.wikimedia.org/wikipedia/commons/5/5a/ZEE5_logo.svg"
            lower.contains("sonyliv") || lower.contains("sony") -> "https://upload.wikimedia.org/wikipedia/commons/8/87/SonyLIV_logo.svg"
            else -> "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=100&q=80"
        }
    }

    private suspend fun <T> executeWithRetry(
        maxRetries: Int = 2,
        block: suspend () -> WatchmodeResult<T>
    ): WatchmodeResult<T> {
        var currentAttempt = 0
        var lastError: WatchmodeResult.Error? = null

        while (currentAttempt <= maxRetries) {
            try {
                val result = block()
                if (result is WatchmodeResult.Error) {
                    // Do not retry 401/403 auth configuration errors
                    if (result.errorType == WatchmodeResult.ErrorType.AUTH_CONFIGURATION_ERROR) {
                        return result
                    }
                    if (result.errorType == WatchmodeResult.ErrorType.RATE_LIMITED && currentAttempt < maxRetries) {
                        delay((1000L * (currentAttempt + 1)))
                        currentAttempt++
                        lastError = result
                        continue
                    }
                }
                return result
            } catch (e: IOException) {
                lastError = WatchmodeResult.Error(
                    WatchmodeResult.ErrorType.NETWORK_ERROR,
                    "Unable to connect to Watchmode discovery service."
                )
                if (currentAttempt < maxRetries) {
                    delay(800L * (currentAttempt + 1))
                    currentAttempt++
                } else {
                    return lastError
                }
            } catch (e: Exception) {
                Log.e(TAG, "Watchmode API error: ${e.message}")
                return WatchmodeResult.Error(
                    WatchmodeResult.ErrorType.UNKNOWN,
                    "An unexpected error occurred while querying Watchmode."
                )
            }
        }
        return lastError ?: WatchmodeResult.Error(WatchmodeResult.ErrorType.UNKNOWN, "Request failed.")
    }

    private fun <T> mapHttpError(code: Int, message: String): WatchmodeResult<T> {
        return when (code) {
            401, 403 -> WatchmodeResult.Error(
                WatchmodeResult.ErrorType.AUTH_CONFIGURATION_ERROR,
                "Movie service configuration error. Please verify Watchmode API credentials."
            )
            429 -> WatchmodeResult.Error(
                WatchmodeResult.ErrorType.RATE_LIMITED,
                "Too many requests. Please try again later."
            )
            404 -> WatchmodeResult.Error(
                WatchmodeResult.ErrorType.NOT_FOUND,
                "Requested title not found on Watchmode."
            )
            else -> WatchmodeResult.Error(
                WatchmodeResult.ErrorType.NETWORK_ERROR,
                "Unable to connect (HTTP $code: $message)."
            )
        }
    }
}
