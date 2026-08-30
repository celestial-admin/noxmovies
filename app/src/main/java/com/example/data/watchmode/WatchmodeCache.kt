package com.example.data.watchmode

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Movie
import com.example.model.WatchProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Local cache for Watchmode metadata and streaming availability.
 * Implements strict compliance with Watchmode's 30-day data retention limits.
 */
class WatchmodeCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("watchmode_cache_store", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    
    // In-memory fast caches
    private val searchCache = ConcurrentHashMap<String, CachedData<List<Movie>>>()
    private val detailsCache = ConcurrentHashMap<String, CachedData<Movie>>()
    private val sourcesCache = ConcurrentHashMap<String, CachedData<List<WatchProvider>>>()

    companion object {
        // Strict 30-day max retention limit per Watchmode Developer terms (in ms)
        private const val MAX_RETENTION_MS = 30L * 24 * 60 * 60 * 1000L
        // Standard fresh TTL: 6 hours for availability, 24 hours for metadata
        private const val DEFAULT_TTL_MS = 24L * 60 * 60 * 1000L
    }

    data class CachedData<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val isExpired: Boolean
            get() = (System.currentTimeMillis() - timestamp) > MAX_RETENTION_MS
    }

    init {
        purgeExpiredData()
    }

    fun getSearchResults(query: String, region: String): List<Movie>? {
        val key = "search_${region}_${query.trim().lowercase()}"
        val inMem = searchCache[key]
        if (inMem != null && !inMem.isExpired) {
            return inMem.data
        }
        return null
    }

    fun putSearchResults(query: String, region: String, results: List<Movie>) {
        val key = "search_${region}_${query.trim().lowercase()}"
        searchCache[key] = CachedData(results)
    }

    fun getMovieDetails(titleId: String, region: String): Movie? {
        val key = "details_${region}_$titleId"
        val inMem = detailsCache[key]
        if (inMem != null && !inMem.isExpired) {
            return inMem.data
        }
        return null
    }

    fun putMovieDetails(titleId: String, region: String, movie: Movie) {
        val key = "details_${region}_$titleId"
        detailsCache[key] = CachedData(movie)
    }

    fun getWatchProviders(titleId: String, region: String): List<WatchProvider>? {
        val key = "sources_${region}_$titleId"
        val inMem = sourcesCache[key]
        if (inMem != null && !inMem.isExpired) {
            return inMem.data
        }
        return null
    }

    fun putWatchProviders(titleId: String, region: String, providers: List<WatchProvider>) {
        val key = "sources_${region}_$titleId"
        sourcesCache[key] = CachedData(providers)
    }

    fun clearAll() {
        searchCache.clear()
        detailsCache.clear()
        sourcesCache.clear()
        prefs.edit().clear().apply()
    }

    private fun purgeExpiredData() {
        val now = System.currentTimeMillis()
        searchCache.entries.removeIf { (now - it.value.timestamp) > MAX_RETENTION_MS }
        detailsCache.entries.removeIf { (now - it.value.timestamp) > MAX_RETENTION_MS }
        sourcesCache.entries.removeIf { (now - it.value.timestamp) > MAX_RETENTION_MS }
    }
}
