package com.example.data

import com.example.data.watchmode.WatchmodeApiService
import com.example.data.watchmode.WatchmodeCache
import com.example.data.watchmode.WatchmodeResult
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieRepository(
    val watchmodeApi: WatchmodeApiService? = null,
    val cache: WatchmodeCache? = null
) : MediaProvider {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Reactive Watch History
    private val _history = MutableStateFlow<List<WatchHistoryItem>>(initialHistory)
    val history: StateFlow<List<WatchHistoryItem>> = _history.asStateFlow()

    // Reactive Downloads
    private val _downloads = MutableStateFlow<List<DownloadItem>>(initialDownloads)
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    // Reactive Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(initialNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Watch Queue
    private val _watchQueue = MutableStateFlow<List<Movie>>(mockMovies.take(3))
    val watchQueue: StateFlow<List<Movie>> = _watchQueue.asStateFlow()

    override suspend fun search(query: String): List<Movie> {
        return searchWithRegion(query, "IN")
    }

    suspend fun searchWithRegion(query: String, region: String = "IN"): List<Movie> {
        if (query.isBlank()) return emptyList()

        // 1. Check local memory/30-day cache
        cache?.getSearchResults(query, region)?.let {
            return it
        }

        // 2. Query Watchmode API if configured
        if (watchmodeApi != null && watchmodeApi.isConfigured) {
            when (val apiResult = watchmodeApi.searchTitles(query, "movie,tv", region)) {
                is WatchmodeResult.Success -> {
                    if (apiResult.data.isNotEmpty()) {
                        cache?.putSearchResults(query, region, apiResult.data)
                        return apiResult.data
                    }
                }
                is WatchmodeResult.Error -> {
                    // Log or handle gracefully, fallback to local discovery catalog
                }
            }
        }

        // 3. Fallback to rich discovery catalog
        val fallback = mockMovies.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.genre.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.genres.any { g -> g.contains(query, ignoreCase = true) }
        }
        cache?.putSearchResults(query, region, fallback)
        return fallback
    }

    override suspend fun getDetails(id: String): Movie? {
        return getDetailsWithRegion(id, "IN")
    }

    suspend fun getDetailsWithRegion(id: String, region: String = "IN"): Movie? {
        // Check cache
        cache?.getMovieDetails(id, region)?.let { return it }

        // Find local movie first (to check if it has authorized demo playback)
        val localMovie = mockMovies.find { it.id == id || it.watchmodeId == id }

        // If Watchmode ID is available and API configured, enrich with live Watchmode availability
        val watchmodeIdLong = id.toLongOrNull() ?: localMovie?.watchmodeId?.toLongOrNull()
        if (watchmodeApi != null && watchmodeApi.isConfigured && watchmodeIdLong != null) {
            when (val apiResult = watchmodeApi.getTitleDetails(watchmodeIdLong, region)) {
                is WatchmodeResult.Success -> {
                    val remote = apiResult.data
                    val merged = remote.copy(
                        // Preserve authorized playback source if available in local catalog
                        mediaUrl = localMovie?.mediaUrl ?: "",
                        authorizedSource = localMovie?.authorizedSource,
                        seasons = localMovie?.seasons ?: emptyList()
                    )
                    cache?.putMovieDetails(id, region, merged)
                    return merged
                }
                is WatchmodeResult.Error -> {
                    // Fall back to local
                }
            }
        }

        if (localMovie != null) {
            cache?.putMovieDetails(id, region, localMovie)
            return localMovie
        }

        return null
    }

    override suspend fun getSources(id: String): List<MediaSource> {
        val movie = getDetails(id) ?: return emptyList()
        if (movie.mediaUrl.isBlank()) return emptyList()
        return listOf(
            MediaSource(id = "s1", quality = "1080p", url = movie.mediaUrl, type = "video/mp4"),
            MediaSource(id = "s2", quality = "720p", url = movie.mediaUrl, type = "video/mp4"),
            MediaSource(id = "s3", quality = "480p", url = movie.mediaUrl, type = "video/mp4"),
            MediaSource(id = "s4", quality = "360p", url = movie.mediaUrl, type = "video/mp4")
        )
    }

    suspend fun getWatchProviders(movieId: String, region: String = "IN"): List<WatchProvider> {
        cache?.getWatchProviders(movieId, region)?.let { return it }

        val movie = getDetailsWithRegion(movieId, region)
        if (movie != null && movie.whereToWatch.isNotEmpty()) {
            cache?.putWatchProviders(movieId, region, movie.whereToWatch)
            return movie.whereToWatch
        }

        val watchmodeIdLong = movieId.toLongOrNull() ?: movie?.watchmodeId?.toLongOrNull()
        if (watchmodeApi != null && watchmodeApi.isConfigured && watchmodeIdLong != null) {
            when (val res = watchmodeApi.getTitleSources(watchmodeIdLong, region)) {
                is WatchmodeResult.Success -> {
                    cache?.putWatchProviders(movieId, region, res.data)
                    return res.data
                }
                is WatchmodeResult.Error -> {}
            }
        }

        return emptyList()
    }

    suspend fun getTrending(region: String = "IN"): List<Movie> {
        delay(100)
        return mockMovies
    }

    suspend fun getRecommended(preferredGenres: List<String> = listOf("Sci-Fi", "Action")): List<Movie> {
        delay(100)
        return mockMovies.filter { movie ->
            preferredGenres.any { genre -> movie.genre.contains(genre, ignoreCase = true) || movie.genres.any { g -> g.contains(genre, ignoreCase = true) } }
        }.ifEmpty { mockMovies.take(5) }
    }

    suspend fun getNewReleases(region: String = "IN"): List<Movie> {
        delay(100)
        return mockMovies.filter { it.year == "2026" || it.year == "2025" || it.year == "2024" }
    }

    suspend fun getPopular(region: String = "IN"): List<Movie> {
        delay(100)
        return mockMovies.sortedByDescending { it.rating }
    }

    suspend fun getRecentlyAdded(): List<Movie> {
        delay(100)
        return mockMovies.reversed()
    }

    suspend fun getMovie(id: String): Movie? {
        return getDetails(id)
    }

    fun getAllMovies(): List<Movie> = mockMovies

    suspend fun getMoviesByGenre(genre: String): List<Movie> {
        delay(100)
        if (genre.equals("All", ignoreCase = true)) return mockMovies
        return mockMovies.filter { it.genre.contains(genre, ignoreCase = true) || it.genres.any { g -> g.contains(genre, ignoreCase = true) } }
    }

    // Filter & Search Engine
    suspend fun filterMovies(
        query: String,
        category: String, // All, Movies, Series, Anime
        genre: String,
        year: String,
        minRating: Double,
        sortBy: String, // Relevance, Newest, Rating, A-Z
        providerFilter: String = "All",
        region: String = "IN"
    ): List<Movie> {
        var list = if (query.isNotBlank()) {
            searchWithRegion(query, region)
        } else {
            mockMovies
        }

        // Category Filter
        list = when (category) {
            "Movies" -> list.filter { !it.isSeries && !it.genre.contains("Anime", ignoreCase = true) }
            "Series", "TV Shows" -> list.filter { it.isSeries }
            "Anime" -> list.filter { it.genre.contains("Anime", ignoreCase = true) }
            else -> list
        }

        // Genre Filter
        if (genre.isNotBlank() && genre != "All") {
            list = list.filter { it.genre.contains(genre, ignoreCase = true) || it.genres.any { g -> g.contains(genre, ignoreCase = true) } }
        }

        // Year Filter
        if (year.isNotBlank() && year != "All") {
            list = list.filter { it.year == year }
        }

        // Rating Filter
        if (minRating > 0) {
            list = list.filter { it.rating >= minRating }
        }

        // Provider Filter (Watchmode streaming availability)
        if (providerFilter.isNotBlank() && !providerFilter.equals("All", ignoreCase = true)) {
            list = list.filter { movie ->
                movie.whereToWatch.any { p -> p.name.contains(providerFilter, ignoreCase = true) }
            }
        }

        // Sorting
        list = when (sortBy) {
            "Newest" -> list.sortedByDescending { it.year }
            "Rating" -> list.sortedByDescending { it.rating }
            "A-Z" -> list.sortedBy { it.title }
            else -> list
        }

        return list
    }

    // Watch History Management
    fun recordWatchHistory(movieId: String, positionMs: Long, durationMs: Long) {
        val movie = mockMovies.find { it.id == movieId } ?: return
        val current = _history.value.toMutableList()
        current.removeAll { it.movieId == movieId }
        current.add(
            0,
            WatchHistoryItem(
                movieId = movieId,
                title = movie.title,
                posterUrl = movie.posterUrl,
                positionMs = positionMs,
                durationMs = if (durationMs > 0) durationMs else 120 * 60 * 1000L,
                lastWatchedTimestamp = System.currentTimeMillis(),
                subtitleInfo = if (movie.isSeries) "S1 • E1" else "${movie.year} • ${movie.duration}",
                genre = movie.genres.firstOrNull() ?: movie.genre.split(",").firstOrNull()?.trim() ?: "Movie"
            )
        )
        _history.value = current
    }

    fun removeHistoryItem(movieId: String) {
        val current = _history.value.toMutableList()
        current.removeAll { it.movieId == movieId }
        _history.value = current
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    // Queue Management
    fun addToQueue(movie: Movie) {
        val current = _watchQueue.value.toMutableList()
        if (!current.any { it.id == movie.id }) {
            current.add(movie)
            _watchQueue.value = current
        }
    }

    fun removeFromQueue(movieId: String) {
        val current = _watchQueue.value.toMutableList()
        current.removeAll { it.id == movieId }
        _watchQueue.value = current
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val current = _watchQueue.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _watchQueue.value = current
        }
    }

    // Download Management
    fun startDownload(movie: Movie, quality: String = "1080p") {
        val current = _downloads.value.toMutableList()
        if (current.any { it.movieId == movie.id }) return

        val totalBytes = 2_147_483_648L // ~2.1 GB
        val newDownload = DownloadItem(
            id = "dl_${movie.id}",
            movieId = movie.id,
            title = movie.title,
            quality = quality,
            posterUrl = movie.posterUrl,
            localPath = "/data/user/0/com.example/files/${movie.id}.mp4",
            totalBytes = totalBytes,
            downloadedBytes = 214_748_364L, // Starts at 10%
            speed = "3.2 MB/s",
            isPaused = false,
            isCompleted = false
        )
        current.add(0, newDownload)
        _downloads.value = current

        simulateDownloadProgress(newDownload.id)
    }

    fun togglePauseDownload(downloadId: String) {
        val current = _downloads.value.toMutableList()
        val index = current.indexOfFirst { it.id == downloadId }
        if (index != -1) {
            val item = current[index]
            val updated = item.copy(isPaused = !item.isPaused)
            current[index] = updated
            _downloads.value = current
            if (!updated.isPaused && !updated.isCompleted) {
                simulateDownloadProgress(downloadId)
            }
        }
    }

    fun cancelDownload(downloadId: String) {
        val current = _downloads.value.toMutableList()
        current.removeAll { it.id == downloadId }
        _downloads.value = current
    }

    fun deleteCompletedDownload(downloadId: String) {
        cancelDownload(downloadId)
    }

    fun clearAllDownloads() {
        _downloads.value = emptyList()
    }

    private fun simulateDownloadProgress(downloadId: String) {
        coroutineScope.launch {
            for (step in 1..10) {
                delay(1200)
                val current = _downloads.value.toMutableList()
                val index = current.indexOfFirst { it.id == downloadId }
                if (index == -1) break
                val item = current[index]
                if (item.isPaused || item.isCompleted) break

                val increment = item.totalBytes / 10
                val newDownloaded = (item.downloadedBytes + increment).coerceAtMost(item.totalBytes)
                val completed = newDownloaded >= item.totalBytes
                current[index] = item.copy(
                    downloadedBytes = newDownloaded,
                    isCompleted = completed,
                    speed = if (completed) "Completed" else "${(2.0 + Math.random() * 1.5).toString().take(3)} MB/s"
                )
                _downloads.value = current
                if (completed) {
                    addNotification(
                        title = "Download Complete",
                        description = "${item.title} (${item.quality}) is ready for offline playback.",
                        category = "Downloads"
                    )
                    break
                }
            }
        }
    }

    // Notifications Management
    fun addNotification(title: String, description: String, category: String) {
        val current = _notifications.value.toMutableList()
        current.add(
            0,
            NotificationItem(
                id = "notif_${System.currentTimeMillis()}",
                title = title,
                description = description,
                category = category,
                timestamp = "Just now",
                isRead = false
            )
        )
        _notifications.value = current
    }

    fun markNotificationAsRead(id: String) {
        val current = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _notifications.value = current
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    companion object {
        val initialHistory = listOf(
            WatchHistoryItem(
                movieId = "1",
                title = "The Silent Echo",
                posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400&q=80",
                positionMs = 45 * 60 * 1000L,
                durationMs = 105 * 60 * 1000L,
                lastWatchedTimestamp = System.currentTimeMillis() - 1000 * 60 * 30, // 30 mins ago
                subtitleInfo = "45m / 1h 45m remaining",
                genre = "Thriller"
            ),
            WatchHistoryItem(
                movieId = "6",
                title = "Chronicles of Solitude",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80",
                positionMs = 28 * 60 * 1000L,
                durationMs = 50 * 60 * 1000L,
                lastWatchedTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 26, // Yesterday
                subtitleInfo = "S1 • E2: Rehearsal",
                genre = "Drama"
            ),
            WatchHistoryItem(
                movieId = "2",
                title = "Neon Horizon",
                posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&q=80",
                positionMs = 90 * 60 * 1000L,
                durationMs = 126 * 60 * 1000L,
                lastWatchedTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 80, // Earlier
                subtitleInfo = "1h 30m / 2h 06m",
                genre = "Sci-Fi"
            )
        )

        val initialDownloads = listOf(
            DownloadItem(
                id = "dl_1",
                movieId = "2",
                title = "Neon Horizon",
                quality = "1080p",
                posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&q=80",
                localPath = "/storage/emulated/0/NOX/neon_horizon_1080p.mp4",
                totalBytes = 2_254_857_830L,
                downloadedBytes = 1_352_914_698L,
                speed = "2.4 MB/s",
                isPaused = false,
                isCompleted = false
            ),
            DownloadItem(
                id = "dl_2",
                movieId = "5",
                title = "The Last Stand",
                quality = "4K UHD",
                posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&q=80",
                localPath = "/storage/emulated/0/NOX/the_last_stand_4k.mp4",
                totalBytes = 3_435_973_836L,
                downloadedBytes = 3_435_973_836L,
                speed = "Done",
                isPaused = false,
                isCompleted = true
            )
        )

        val initialNotifications = listOf(
            NotificationItem(
                id = "notif_1",
                title = "New 4K Release: Neon Horizon",
                description = "Experience the thrilling futuristic cyberpunk mystery now streaming in Ultra HD with Dolby Atmos audio.",
                category = "New Releases",
                timestamp = "2 hours ago",
                isRead = false
            ),
            NotificationItem(
                id = "notif_2",
                title = "Download Complete: The Last Stand",
                description = "The Last Stand (4K UHD) is ready for offline playback anytime.",
                category = "Downloads",
                timestamp = "Yesterday",
                isRead = true
            ),
            NotificationItem(
                id = "notif_3",
                title = "NOX System Update v1.0",
                description = "Enjoy enhanced Media3 player performance, Watchmode streaming discovery, and custom accent themes.",
                category = "System",
                timestamp = "2 days ago",
                isRead = true
            )
        )

        // Safe Public Domain / Open Demo Media URLs
        const val DEMO_VIDEO_1 = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        const val DEMO_VIDEO_2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        const val DEMO_VIDEO_3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        const val DEMO_VIDEO_4 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"

        val mockMovies = listOf(
            // 1. Interstellar (Classic requested title in section 4, 11, 26)
            Movie(
                id = "3173903",
                watchmodeId = "3173903",
                imdbId = "tt0816692",
                tmdbId = "157336",
                title = "Interstellar",
                year = "2014",
                runtime = 169,
                duration = "2h 49m",
                posterUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200&q=80",
                overview = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the limitations on human space travel and conquer the vast distances involved in an interstellar voyage.",
                description = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the limitations on human space travel and conquer the vast distances involved in an interstellar voyage.",
                rating = 8.7,
                genres = listOf("Sci-Fi", "Adventure", "Drama"),
                genre = "Sci-Fi, Adventure",
                type = "movie",
                quality = "4K UHD",
                mediaUrl = "", // No direct video stream - watches through authorized streaming providers
                trailerUrl = "https://www.youtube.com/watch?v=zSWdZVtXT7E",
                cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"),
                tags = listOf("4K UHD", "IMAX", "Dolby Atmos"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "netflix_in",
                        name = "Netflix",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.netflix.com/title/70305903",
                        androidUrl = "https://www.netflix.com/title/70305903",
                        format = "4K UHD",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "prime_in",
                        name = "Amazon Prime Video",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.primevideo.com/detail/Interstellar",
                        androidUrl = "https://www.primevideo.com/detail/Interstellar",
                        format = "4K UHD",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "youtube_in",
                        name = "YouTube Movies",
                        type = ProviderType.RENT,
                        price = "₹120",
                        webUrl = "https://www.youtube.com/results?search_query=Interstellar+movie",
                        androidUrl = "https://www.youtube.com/results?search_query=Interstellar+movie",
                        format = "HD",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "appletv_in",
                        name = "Apple TV",
                        type = ProviderType.BUY,
                        price = "₹490",
                        webUrl = "https://tv.apple.com/movie/interstellar/umc.cmc.209633519828",
                        androidUrl = "https://tv.apple.com/movie/interstellar/umc.cmc.209633519828",
                        format = "4K HDR",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "jiocinema_in",
                        name = "JioCinema",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.jiocinema.com/movies/interstellar",
                        androidUrl = "https://www.jiocinema.com/movies/interstellar",
                        format = "HD",
                        region = "IN"
                    )
                )
            ),

            // 2. Dune: Part Two
            Movie(
                id = "3194821",
                watchmodeId = "3194821",
                imdbId = "tt15239678",
                tmdbId = "693134",
                title = "Dune: Part Two",
                year = "2024",
                runtime = 166,
                duration = "2h 46m",
                posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&q=80",
                overview = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the universe.",
                description = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
                rating = 8.6,
                genres = listOf("Sci-Fi", "Adventure", "Action"),
                genre = "Sci-Fi, Adventure",
                type = "movie",
                quality = "4K UHD",
                mediaUrl = "",
                trailerUrl = "https://www.youtube.com/watch?v=Way9Dexny3w",
                cast = listOf("Timothée Chalamet", "Zendaya", "Rebecca Ferguson"),
                tags = listOf("4K UHD", "Dolby Vision", "IMAX Enhanced"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "jiocinema_in",
                        name = "JioCinema Premium",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.jiocinema.com/movies/dune-part-two",
                        androidUrl = "https://www.jiocinema.com/movies/dune-part-two",
                        format = "4K",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "prime_in",
                        name = "Amazon Prime Video",
                        type = ProviderType.RENT,
                        price = "₹149",
                        webUrl = "https://www.primevideo.com",
                        androidUrl = "https://www.primevideo.com",
                        format = "4K UHD",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "appletv_in",
                        name = "Apple TV",
                        type = ProviderType.BUY,
                        price = "₹690",
                        webUrl = "https://tv.apple.com",
                        androidUrl = "https://tv.apple.com",
                        format = "4K HDR",
                        region = "IN"
                    )
                )
            ),

            // 3. The Silent Echo (Authorized NOX Demo Media)
            Movie(
                id = "1",
                watchmodeId = "1001",
                title = "The Silent Echo",
                year = "2026",
                runtime = 105,
                duration = "1h 45m",
                posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200&q=80",
                overview = "A gripping tale of mystery and suspense in a secluded mountain town where audio recordings uncover decades of hidden town history.",
                description = "A gripping tale of mystery and suspense in a secluded mountain town where audio recordings uncover decades of hidden town history.",
                rating = 8.4,
                genres = listOf("Thriller", "Mystery"),
                genre = "Thriller, Mystery",
                type = "movie",
                quality = "4K UHD",
                mediaUrl = DEMO_VIDEO_1, // Authorized NOX Player media
                cast = listOf("Elena Rostova", "Marcus Vance", "Sora Takahashi"),
                tags = listOf("4K UHD", "HDR10", "Spatial Audio"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "nox_direct",
                        name = "NOX Master Stream",
                        type = ProviderType.FREE,
                        webUrl = "https://nox.local/stream/1",
                        format = "4K HDR",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "youtube_free",
                        name = "YouTube",
                        type = ProviderType.FREE,
                        webUrl = "https://www.youtube.com",
                        format = "1080p",
                        region = "IN"
                    )
                )
            ),

            // 4. Neon Horizon (Authorized NOX Demo Media)
            Movie(
                id = "2",
                watchmodeId = "1002",
                title = "Neon Horizon",
                year = "2026",
                runtime = 126,
                duration = "2h 06m",
                posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&q=80",
                overview = "In a sprawling neon metropolis beneath orbital solar mirrors, an operative untangles a corporate cybernet syndicate.",
                description = "In a sprawling neon metropolis beneath orbital solar mirrors, an operative untangles a corporate cybernet syndicate.",
                rating = 9.1,
                genres = listOf("Sci-Fi", "Action", "Cyberpunk"),
                genre = "Sci-Fi, Action",
                type = "movie",
                quality = "4K UHD",
                mediaUrl = DEMO_VIDEO_2, // Authorized NOX Player media
                cast = listOf("Akira Tanaka", "Sarah Connor", "Liam Chen"),
                tags = listOf("4K UHD", "Dolby Vision", "IMAX Enhanced"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "nox_direct_2",
                        name = "NOX Master Stream",
                        type = ProviderType.FREE,
                        webUrl = "https://nox.local/stream/2",
                        format = "4K UHD",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "prime_in_2",
                        name = "Amazon Prime Video",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.primevideo.com",
                        format = "4K",
                        region = "IN"
                    )
                )
            ),

            // 5. Oppenheimer
            Movie(
                id = "3182901",
                watchmodeId = "3182901",
                imdbId = "tt15398776",
                tmdbId = "872585",
                title = "Oppenheimer",
                year = "2023",
                runtime = 180,
                duration = "3h 00m",
                posterUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=1200&q=80",
                overview = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.",
                description = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.",
                rating = 8.9,
                genres = listOf("Biography", "Drama", "History"),
                genre = "Biography, Drama",
                type = "movie",
                quality = "4K UHD",
                mediaUrl = "",
                trailerUrl = "https://www.youtube.com/watch?v=uYPbbksJxIg",
                cast = listOf("Cillian Murphy", "Emily Blunt", "Robert Downey Jr."),
                tags = listOf("4K UHD", "IMAX 70mm", "Dolby Atmos"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "jiocinema_in_3",
                        name = "JioCinema Premium",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.jiocinema.com/movies/oppenheimer",
                        androidUrl = "https://www.jiocinema.com/movies/oppenheimer",
                        format = "4K",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "prime_rent",
                        name = "Amazon Prime Video",
                        type = ProviderType.RENT,
                        price = "₹120",
                        webUrl = "https://www.primevideo.com",
                        format = "4K UHD",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "zee5_in",
                        name = "ZEE5",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.zee5.com",
                        format = "HD",
                        region = "IN"
                    )
                )
            ),

            // 6. Stranger Things (TV Series)
            Movie(
                id = "3154890",
                watchmodeId = "3154890",
                imdbId = "tt4574334",
                tmdbId = "66732",
                title = "Stranger Things",
                year = "2024",
                runtime = 55,
                duration = "4 Seasons",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=80",
                overview = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
                description = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments and terrifying supernatural forces.",
                rating = 8.7,
                genres = listOf("Sci-Fi", "Horror", "Drama"),
                genre = "Sci-Fi, Horror",
                type = "tv",
                isSeries = true,
                quality = "4K UHD",
                mediaUrl = "",
                trailerUrl = "https://www.youtube.com/watch?v=b9EkMc79ZSU",
                cast = listOf("Millie Bobby Brown", "Finn Wolfhard", "Winona Ryder"),
                tags = listOf("4K UHD", "HDR10", "Spatial Audio"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "netflix_st",
                        name = "Netflix",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.netflix.com/title/80057281",
                        androidUrl = "https://www.netflix.com/title/80057281",
                        format = "4K Dolby Vision",
                        region = "IN"
                    )
                ),
                seasons = listOf(
                    Season(
                        seasonNumber = 1,
                        title = "Season 1",
                        episodes = listOf(
                            Episode(1, "Chapter One: The Vanishing of Will Byers", "48m", 48, DEMO_VIDEO_1, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80", 0.65f),
                            Episode(2, "Chapter Two: The Weirdo on Maple Street", "55m", 55, DEMO_VIDEO_2, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400&q=80", 0.0f)
                        )
                    )
                )
            ),

            // 7. Cyberpunk: Edgerunners (TV Series)
            Movie(
                id = "3167120",
                watchmodeId = "3167120",
                imdbId = "tt12590266",
                tmdbId = "105248",
                title = "Cyberpunk: Edgerunners",
                year = "2023",
                runtime = 24,
                duration = "10 Episodes",
                posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200&q=80",
                overview = "A street kid trying to survive in a technology and body modification-obsessed city of the future. Having everything to lose, he chooses to stay alive by becoming an edgerunner.",
                description = "A street kid trying to survive in Night City becomes an edgerunner mercenary.",
                rating = 8.3,
                genres = listOf("Animation", "Action", "Sci-Fi"),
                genre = "Anime, Sci-Fi",
                type = "tv",
                isSeries = true,
                quality = "4K UHD",
                mediaUrl = "",
                trailerUrl = "https://www.youtube.com/watch?v=JtqIas3bYhg",
                cast = listOf("Aoi Yuuki", "KENN", "Hiroki Touchi"),
                tags = listOf("Anime", "4K", "Studio Trigger"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "netflix_cp",
                        name = "Netflix",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.netflix.com/title/81054853",
                        androidUrl = "https://www.netflix.com/title/81054853",
                        format = "4K",
                        region = "IN"
                    )
                )
            ),

            // 8. The Dark Knight
            Movie(
                id = "3145672",
                watchmodeId = "3145672",
                imdbId = "tt0468569",
                tmdbId = "155",
                title = "The Dark Knight",
                year = "2008",
                runtime = 152,
                duration = "2h 32m",
                posterUrl = "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=1200&q=80",
                overview = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                description = "When the menace known as the Joker wreaks havoc on Gotham, Batman must accept one of his greatest tests.",
                rating = 9.0,
                genres = listOf("Action", "Crime", "Drama"),
                genre = "Action, Crime",
                type = "movie",
                quality = "4K UHD",
                mediaUrl = "",
                trailerUrl = "https://www.youtube.com/watch?v=EXeTwQWrcwY",
                cast = listOf("Christian Bale", "Heath Ledger", "Aaron Eckhart"),
                tags = listOf("4K UHD", "IMAX", "Dolby Atmos"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "jiocinema_dk",
                        name = "JioCinema Premium",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.jiocinema.com",
                        format = "4K",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "netflix_dk",
                        name = "Netflix",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.netflix.com",
                        format = "4K",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "prime_dk",
                        name = "Amazon Prime Video",
                        type = ProviderType.RENT,
                        price = "₹99",
                        webUrl = "https://www.primevideo.com",
                        format = "HD",
                        region = "IN"
                    )
                )
            ),

            // 9. Tears of Steel (Authorized Demo Media)
            Movie(
                id = "4",
                watchmodeId = "1004",
                title = "Tears of Steel",
                year = "2025",
                runtime = 98,
                duration = "1h 38m",
                posterUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&q=80",
                overview = "In a dystopian future set in Amsterdam, a group of scientists and warriors stage a desperate attempt to rescue the earth from destructive robots.",
                description = "Scientists and warriors stage a desperate attempt to rescue Earth from destructive machines.",
                rating = 7.9,
                genres = listOf("Sci-Fi", "VFX", "Action"),
                genre = "Sci-Fi, VFX",
                type = "movie",
                quality = "4K UHD",
                mediaUrl = DEMO_VIDEO_3, // Authorized NOX media
                cast = listOf("Derek de Lint", "Sergio Hasselbaink", "Vanja Rukavina"),
                tags = listOf("4K UHD", "VFX", "Open Source"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "nox_direct_3",
                        name = "NOX Master Stream",
                        type = ProviderType.FREE,
                        webUrl = "https://nox.local/stream/4",
                        format = "4K UHD",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "youtube_tos",
                        name = "YouTube",
                        type = ProviderType.FREE,
                        webUrl = "https://www.youtube.com",
                        format = "4K",
                        region = "IN"
                    )
                )
            ),

            // 10. Sintel (Authorized Demo Media)
            Movie(
                id = "5",
                watchmodeId = "1005",
                title = "The Last Stand: Sintel",
                year = "2024",
                runtime = 110,
                duration = "1h 50m",
                posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=80",
                overview = "A lonely young woman attacks perilous winter lands to recover a captive baby dragon, testing her resolve against ancient beasts.",
                description = "A lonely young woman attacks perilous winter lands to recover a captive baby dragon.",
                rating = 8.1,
                genres = listOf("Fantasy", "Animation", "Adventure"),
                genre = "Fantasy, Animation",
                type = "movie",
                quality = "4K UHD",
                mediaUrl = DEMO_VIDEO_4, // Authorized NOX media
                cast = listOf("Halina Reijn", "Thom Hoffman"),
                tags = listOf("4K UHD", "HDR10", "Dolby Atmos"),
                whereToWatch = listOf(
                    WatchProvider(
                        providerId = "nox_direct_5",
                        name = "NOX Master Stream",
                        type = ProviderType.FREE,
                        webUrl = "https://nox.local/stream/5",
                        format = "4K UHD",
                        region = "IN"
                    ),
                    WatchProvider(
                        providerId = "hotstar_sintel",
                        name = "Disney+ Hotstar",
                        type = ProviderType.SUBSCRIPTION,
                        webUrl = "https://www.hotstar.com",
                        format = "4K",
                        region = "IN"
                    )
                )
            )
        )
    }
}
