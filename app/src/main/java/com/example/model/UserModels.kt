package com.example.model

data class NoxUser(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val isGuest: Boolean = false,
    val syncLibraryEnabled: Boolean = true
)

data class UserTasteContext(
    val favoriteGenres: List<String> = emptyList(),
    val recentlyWatchedTitles: List<String> = emptyList(),
    val isGuest: Boolean = false
)
