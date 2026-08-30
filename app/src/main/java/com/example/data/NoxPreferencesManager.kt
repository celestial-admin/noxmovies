package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AccentColor
import com.example.model.NoxUser
import com.example.model.PlaybackQuality
import com.example.model.SubtitleLanguage
import com.example.model.ThemeMode
import com.example.model.UserTasteContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoxPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nox_preferences", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(getSavedUser())
    val currentUser: StateFlow<NoxUser> = _currentUser.asStateFlow()

    private val _syncLibraryEnabled = MutableStateFlow(prefs.getBoolean(KEY_SYNC_LIBRARY, true))
    val syncLibraryEnabled: StateFlow<Boolean> = _syncLibraryEnabled.asStateFlow()

    private val _streamingRegion = MutableStateFlow(prefs.getString(KEY_STREAMING_REGION, "IN") ?: "IN")
    val streamingRegion: StateFlow<String> = _streamingRegion.asStateFlow()

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentColor = MutableStateFlow(getSavedAccentColor())
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _reduceMotion = MutableStateFlow(prefs.getBoolean(KEY_REDUCE_MOTION, false))
    val reduceMotion: StateFlow<Boolean> = _reduceMotion.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _wifiOnlyDownloads = MutableStateFlow(prefs.getBoolean(KEY_WIFI_ONLY_DOWNLOADS, true))
    val wifiOnlyDownloads: StateFlow<Boolean> = _wifiOnlyDownloads.asStateFlow()

    private val _defaultQuality = MutableStateFlow(getSavedQuality())
    val defaultQuality: StateFlow<PlaybackQuality> = _defaultQuality.asStateFlow()

    private val _defaultSubtitle = MutableStateFlow(getSavedSubtitle())
    val defaultSubtitle: StateFlow<SubtitleLanguage> = _defaultSubtitle.asStateFlow()

    private val _defaultSpeed = MutableStateFlow(prefs.getFloat(KEY_DEFAULT_SPEED, 1.0f))
    val defaultSpeed: StateFlow<Float> = _defaultSpeed.asStateFlow()

    private val _autoDeleteWatched = MutableStateFlow(prefs.getBoolean(KEY_AUTO_DELETE_WATCHED, false))
    val autoDeleteWatched: StateFlow<Boolean> = _autoDeleteWatched.asStateFlow()

    private val _autoplay = MutableStateFlow(prefs.getBoolean(KEY_AUTOPLAY, true))
    val autoplay: StateFlow<Boolean> = _autoplay.asStateFlow()

    // Favorites persisted as StringSet
    private val _favoriteIds = MutableStateFlow(prefs.getStringSet(KEY_FAVORITES, setOf("1", "2")) ?: setOf("1", "2"))
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    // Search history persisted as list
    private val _recentSearches = MutableStateFlow(
        prefs.getString(KEY_SEARCH_HISTORY, "Dune,Oppenheimer,Cyberpunk,Batman")
            ?.split(",")
            ?.filter { it.isNotBlank() } ?: listOf("Dune", "Oppenheimer", "Cyberpunk", "Batman")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setAccentColor(accent: AccentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, accent.name).apply()
        _accentColor.value = accent
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _isOnboardingCompleted.value = completed
    }

    fun setReduceMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply()
        _reduceMotion.value = enabled
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
        _hapticFeedback.value = enabled
    }

    fun setWifiOnlyDownloads(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_ONLY_DOWNLOADS, enabled).apply()
        _wifiOnlyDownloads.value = enabled
    }

    fun setDefaultQuality(quality: PlaybackQuality) {
        prefs.edit().putString(KEY_DEFAULT_QUALITY, quality.name).apply()
        _defaultQuality.value = quality
    }

    fun setDefaultSubtitle(subtitle: SubtitleLanguage) {
        prefs.edit().putString(KEY_DEFAULT_SUBTITLE, subtitle.name).apply()
        _defaultSubtitle.value = subtitle
    }

    fun setDefaultSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_DEFAULT_SPEED, speed).apply()
        _defaultSpeed.value = speed
    }

    fun setAutoDeleteWatched(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_DELETE_WATCHED, enabled).apply()
        _autoDeleteWatched.value = enabled
    }

    fun setAutoplay(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTOPLAY, enabled).apply()
        _autoplay.value = enabled
    }

    fun toggleFavorite(movieId: String): Boolean {
        val current = _favoriteIds.value.toMutableSet()
        val isFav = if (current.contains(movieId)) {
            current.remove(movieId)
            false
        } else {
            current.add(movieId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        _favoriteIds.value = current
        return isFav
    }

    fun isFavorite(movieId: String): Boolean {
        return _favoriteIds.value.contains(movieId)
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        val trimmed = current.take(10)
        prefs.edit().putString(KEY_SEARCH_HISTORY, trimmed.joinToString(",")).apply()
        _recentSearches.value = trimmed
    }

    fun removeRecentSearch(query: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        prefs.edit().putString(KEY_SEARCH_HISTORY, current.joinToString(",")).apply()
        _recentSearches.value = current
    }

    fun clearRecentSearches() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
        _recentSearches.value = emptyList()
    }

    fun savePlaybackPosition(movieId: String, positionMs: Long) {
        prefs.edit().putLong("pos_$movieId", positionMs).apply()
    }

    fun getPlaybackPosition(movieId: String): Long {
        return prefs.getLong("pos_$movieId", 0L)
    }

    fun saveUser(user: NoxUser) {
        prefs.edit()
            .putString(KEY_USER_UID, user.uid)
            .putString(KEY_USER_NAME, user.displayName)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_PHOTO, user.photoUrl)
            .putBoolean(KEY_USER_IS_GUEST, user.isGuest)
            .putBoolean(KEY_SYNC_LIBRARY, user.syncLibraryEnabled)
            .apply()
        _currentUser.value = user
        _syncLibraryEnabled.value = user.syncLibraryEnabled
    }

    fun signOut() {
        prefs.edit()
            .remove(KEY_USER_UID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_PHOTO)
            .putBoolean(KEY_USER_IS_GUEST, true)
            .apply()
        _currentUser.value = NoxUser(
            uid = "guest_${System.currentTimeMillis()}",
            displayName = "Guest User",
            email = "guest@nox.local",
            photoUrl = null,
            isGuest = true,
            syncLibraryEnabled = false
        )
    }

    fun setSyncLibrary(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_LIBRARY, enabled).apply()
        _syncLibraryEnabled.value = enabled
        val current = _currentUser.value
        _currentUser.value = current.copy(syncLibraryEnabled = enabled)
    }

    fun setStreamingRegion(regionCode: String) {
        prefs.edit().putString(KEY_STREAMING_REGION, regionCode).apply()
        _streamingRegion.value = regionCode
    }

    fun getUserTasteContext(favoriteGenres: List<String> = emptyList(), watchedTitles: List<String> = emptyList()): UserTasteContext {
        val user = _currentUser.value
        return UserTasteContext(
            favoriteGenres = favoriteGenres.ifEmpty { listOf("Sci-Fi", "Action", "Thriller") },
            recentlyWatchedTitles = watchedTitles,
            isGuest = user.isGuest
        )
    }

    private fun getSavedUser(): NoxUser {
        val isGuest = prefs.getBoolean(KEY_USER_IS_GUEST, true)
        val uid = prefs.getString(KEY_USER_UID, null)
        val name = prefs.getString(KEY_USER_NAME, if (isGuest) "Guest User" else "Harshit") ?: "Guest User"
        val email = prefs.getString(KEY_USER_EMAIL, if (isGuest) "guest@nox.local" else "harshittraj25@gmail.com") ?: "guest@nox.local"
        val photo = prefs.getString(KEY_USER_PHOTO, null)
        val sync = prefs.getBoolean(KEY_SYNC_LIBRARY, true)

        return if (uid != null && !isGuest) {
            NoxUser(
                uid = uid,
                displayName = name,
                email = email,
                photoUrl = photo,
                isGuest = false,
                syncLibraryEnabled = sync
            )
        } else {
            NoxUser(
                uid = uid ?: "guest_user",
                displayName = "Guest User",
                email = "guest@nox.local",
                photoUrl = null,
                isGuest = true,
                syncLibraryEnabled = false
            )
        }
    }

    private fun getSavedThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }

    private fun getSavedAccentColor(): AccentColor {
        val name = prefs.getString(KEY_ACCENT_COLOR, AccentColor.NOX_LIME.name) ?: AccentColor.NOX_LIME.name
        return try {
            AccentColor.valueOf(name)
        } catch (e: Exception) {
            AccentColor.NOX_LIME
        }
    }

    private fun getSavedQuality(): PlaybackQuality {
        val name = prefs.getString(KEY_DEFAULT_QUALITY, PlaybackQuality.AUTO.name) ?: PlaybackQuality.AUTO.name
        return try {
            PlaybackQuality.valueOf(name)
        } catch (e: Exception) {
            PlaybackQuality.AUTO
        }
    }

    private fun getSavedSubtitle(): SubtitleLanguage {
        val name = prefs.getString(KEY_DEFAULT_SUBTITLE, SubtitleLanguage.OFF.name) ?: SubtitleLanguage.OFF.name
        return try {
            SubtitleLanguage.valueOf(name)
        } catch (e: Exception) {
            SubtitleLanguage.OFF
        }
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ACCENT_COLOR = "key_accent_color"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_REDUCE_MOTION = "key_reduce_motion"
        private const val KEY_HAPTIC_FEEDBACK = "key_haptic_feedback"
        private const val KEY_WIFI_ONLY_DOWNLOADS = "key_wifi_only_downloads"
        private const val KEY_DEFAULT_QUALITY = "key_default_quality"
        private const val KEY_DEFAULT_SUBTITLE = "key_default_subtitle"
        private const val KEY_DEFAULT_SPEED = "key_default_speed"
        private const val KEY_AUTO_DELETE_WATCHED = "key_auto_delete_watched"
        private const val KEY_AUTOPLAY = "key_autoplay"
        private const val KEY_FAVORITES = "key_favorites"
        private const val KEY_SEARCH_HISTORY = "key_search_history"
        private const val KEY_SYNC_LIBRARY = "key_sync_library"
        private const val KEY_STREAMING_REGION = "key_streaming_region"
        private const val KEY_USER_UID = "key_user_uid"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_PHOTO = "key_user_photo"
        private const val KEY_USER_IS_GUEST = "key_user_is_guest"
    }
}
