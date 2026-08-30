package com.example.model

enum class ProviderType {
    SUBSCRIPTION,
    FREE,
    RENT,
    BUY,
    TV
}

data class WatchProvider(
    val providerId: String,
    val name: String,
    val logoUrl: String? = null,
    val type: ProviderType = ProviderType.SUBSCRIPTION,
    val webUrl: String? = null,
    val androidUrl: String? = null,
    val price: String? = null,
    val format: String? = null,
    val region: String = "IN"
)

data class AuthorizedMediaSource(
    val url: String,
    val quality: String? = "1080p",
    val mimeType: String? = "video/mp4"
)

data class Region(
    val code: String,
    val name: String,
    val flag: String = ""
)

val AVAILABLE_REGIONS = listOf(
    Region("IN", "India", "🇮🇳"),
    Region("US", "United States", "🇺🇸"),
    Region("GB", "United Kingdom", "🇬🇧"),
    Region("CA", "Canada", "🇨🇦"),
    Region("AU", "Australia", "🇦🇺"),
    Region("DE", "Germany", "🇩🇪"),
    Region("FR", "France", "🇫🇷"),
    Region("JP", "Japan", "🇯🇵"),
    Region("BR", "Brazil", "🇧🇷")
)

data class Movie(
    val id: String,
    val watchmodeId: String? = null,
    val imdbId: String? = null,
    val tmdbId: String? = null,
    val title: String,
    val year: String = "2024",
    val runtime: Int? = null,
    val duration: String = if (runtime != null && runtime > 0) "${runtime / 60}h ${runtime % 60}m" else "2h 0m",
    val posterUrl: String,
    val backdropUrl: String = "",
    val overview: String = "",
    val description: String = overview,
    val rating: Double = 8.0,
    val genres: List<String> = emptyList(),
    val genre: String = if (genres.isNotEmpty()) genres.first() else "Cinema",
    val type: String = "movie", // "movie", "tv", "tv_series"
    val quality: String = "4K UHD",
    val mediaUrl: String = "",
    val isSeries: Boolean = (type == "tv" || type == "tv_series"),
    val authorizedSource: AuthorizedMediaSource? = if (mediaUrl.isNotBlank()) AuthorizedMediaSource(mediaUrl, quality, "video/mp4") else null,
    val whereToWatch: List<WatchProvider> = emptyList(),
    val trailerUrl: String? = null,
    val seasons: List<Season> = emptyList(),
    val cast: List<String> = listOf("Lead Actor", "Supporting Star", "Director Visionary"),
    val tags: List<String> = listOf("4K UHD", "HDR10", "Dolby Atmos")
)

data class Season(
    val seasonNumber: Int,
    val title: String,
    val episodes: List<Episode>
)

data class Episode(
    val episodeNumber: Int,
    val title: String,
    val duration: String,
    val durationMinutes: Int,
    val videoUrl: String,
    val thumbnail: String,
    val progressPercent: Float = 0f
)

data class MediaSource(
    val id: String,
    val quality: String, // 1080p, 720p, 480p, 360p
    val url: String,
    val type: String
)

data class DownloadItem(
    val id: String,
    val movieId: String,
    val title: String,
    val quality: String,
    val posterUrl: String,
    val localPath: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speed: String = "2.4 MB/s",
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false
) {
    val progress: Float
        get() = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes.toFloat() else 0f
        
    val formattedTotalSize: String
        get() = String.format("%.1f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))
        
    val formattedDownloadedSize: String
        get() = String.format("%.1f GB", downloadedBytes / (1024.0 * 1024.0 * 1024.0))
}

data class WatchHistoryItem(
    val movieId: String,
    val title: String,
    val posterUrl: String,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatchedTimestamp: Long = System.currentTimeMillis(),
    val subtitleInfo: String? = null,
    val genre: String = "Sci-Fi"
) {
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
        
    val remainingMinutes: Int
        get() = if (durationMs > positionMs) (((durationMs - positionMs) / 1000) / 60).toInt() else 0
}

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "New Releases", "Downloads", "System"
    val timestamp: String,
    val isRead: Boolean = false
)

enum class ThemeMode {
    AMOLED,
    DARK,
    LIGHT,
    DYNAMIC,
    SYSTEM
}

enum class AccentColor(val displayName: String, val hexCode: Long) {
    NOX_LIME("NOX Lime", 0xFFE9FF55),
    ELECTRIC_BLUE("Electric Blue", 0xFF3B82F6),
    VIOLET("Violet", 0xFF8B5CF6),
    PINK("Pink", 0xFFEC4899),
    RED("Red", 0xFFEF4444),
    ORANGE("Orange", 0xFFF97316),
    CYAN("Cyan", 0xFF06B6D4)
}

enum class PlaybackQuality(val label: String) {
    AUTO("Auto"),
    P1080("1080p"),
    P720("720p"),
    P480("480p"),
    P360("360p")
}

enum class SubtitleLanguage(val label: String) {
    OFF("Off"),
    ENGLISH("English"),
    HINDI("Hindi"),
    SPANISH("Spanish"),
    FRENCH("French"),
    JAPANESE("Japanese")
}
