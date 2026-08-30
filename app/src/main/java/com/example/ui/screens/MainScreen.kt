package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieRepository
import com.example.data.NoxPreferencesManager
import com.example.data.auth.AuthRepository
import com.example.data.gemini.GeminiRepository
import com.example.ui.components.MiniPlayerView
import com.example.ui.player.PlayerManager
import com.example.ui.screens.about.AboutScreen
import com.example.ui.screens.auth.GoogleSignInScreen
import com.example.ui.screens.downloads.DownloadsScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.notifications.NotificationCenterScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.NoxTheme

enum class MainNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("Search", Icons.Filled.Search, Icons.Outlined.Search),
    DOWNLOADS("Downloads", Icons.Filled.Download, Icons.Outlined.Download),
    FAVORITES("Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

enum class SubScreen {
    NONE,
    SETTINGS,
    HISTORY,
    NOTIFICATIONS,
    ABOUT,
    SIGN_IN
}

@Composable
fun MainScreen(
    repository: MovieRepository,
    preferencesManager: NoxPreferencesManager,
    authRepository: AuthRepository,
    geminiRepository: GeminiRepository,
    playerManager: PlayerManager,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
    val colors = NoxTheme.colors
    var currentTab by remember { mutableStateOf(MainNavTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }

    val currentPlayingMovie by playerManager.currentMovie.collectAsState()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val currentPositionMs by playerManager.currentPositionMs.collectAsState()
    val durationMs by playerManager.durationMs.collectAsState()

    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            if (currentSubScreen != SubScreen.SIGN_IN) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                ) {
                    // Mini Player (Anchored immediately above bottom bar when active)
                    AnimatedVisibility(
                        visible = (currentPlayingMovie != null),
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        currentPlayingMovie?.let { movie ->
                            MiniPlayerView(
                                movie = movie,
                                isPlaying = isPlaying,
                                progress = progress,
                                onExpand = { onNavigateToPlayer(movie.id) },
                                onPlayPause = { playerManager.togglePlayPause() },
                                onDismiss = { playerManager.dismissPlayer() }
                            )
                        }
                    }

                    // Material 3 Navigation Bar
                    NavigationBar(
                        containerColor = colors.surface,
                        contentColor = colors.text,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(68.dp)
                    ) {
                        MainNavTab.values().forEach { tab ->
                            val isSelected = (currentTab == tab && currentSubScreen == SubScreen.NONE)
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentTab = tab
                                    currentSubScreen = SubScreen.NONE
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) colors.accent else colors.secondaryText
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) colors.accent else colors.secondaryText,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                    selectedIconColor = colors.accent,
                                    unselectedIconColor = colors.secondaryText,
                                    selectedTextColor = colors.accent,
                                    unselectedTextColor = colors.secondaryText
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentSubScreen) {
                SubScreen.SIGN_IN -> {
                    GoogleSignInScreen(
                        authRepository = authRepository,
                        onSignInSuccess = { currentSubScreen = SubScreen.NONE },
                        onContinueAsGuest = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.SETTINGS -> {
                    SettingsScreen(
                        preferencesManager = preferencesManager,
                        repository = repository,
                        authRepository = authRepository,
                        onNavigateToSignIn = { currentSubScreen = SubScreen.SIGN_IN },
                        onBack = { currentSubScreen = SubScreen.NONE },
                        onNavigateToAbout = { currentSubScreen = SubScreen.ABOUT }
                    )
                }
                SubScreen.HISTORY -> {
                    HistoryScreen(
                        repository = repository,
                        onBack = { currentSubScreen = SubScreen.NONE },
                        onPlayMovie = { onNavigateToPlayer(it) },
                        onExplore = {
                            currentSubScreen = SubScreen.NONE
                            currentTab = MainNavTab.HOME
                        }
                    )
                }
                SubScreen.NOTIFICATIONS -> {
                    NotificationCenterScreen(
                        repository = repository,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.ABOUT -> {
                    AboutScreen(onBack = { currentSubScreen = SubScreen.NONE })
                }
                SubScreen.NONE -> {
                    when (currentTab) {
                        MainNavTab.HOME -> {
                            HomeScreen(
                                repository = repository,
                                onNavigateToDetails = onNavigateToDetails,
                                onNavigateToPlayer = onNavigateToPlayer,
                                onNavigateToSearch = { currentTab = MainNavTab.SEARCH },
                                onNavigateToNotifications = { currentSubScreen = SubScreen.NOTIFICATIONS },
                                onNavigateToProfile = { currentTab = MainNavTab.PROFILE },
                                onNavigateToHistory = { currentSubScreen = SubScreen.HISTORY }
                            )
                        }
                        MainNavTab.SEARCH -> {
                            SearchScreen(
                                repository = repository,
                                preferencesManager = preferencesManager,
                                geminiRepository = geminiRepository,
                                onNavigateToDetails = onNavigateToDetails
                            )
                        }
                        MainNavTab.DOWNLOADS -> {
                            DownloadsScreen(
                                repository = repository,
                                onPlayMovie = onNavigateToPlayer,
                                onExplore = { currentTab = MainNavTab.HOME }
                            )
                        }
                        MainNavTab.FAVORITES -> {
                            FavoritesScreen(
                                repository = repository,
                                preferencesManager = preferencesManager,
                                onNavigateToDetails = onNavigateToDetails,
                                onExplore = { currentTab = MainNavTab.HOME }
                            )
                        }
                        MainNavTab.PROFILE -> {
                            ProfileScreen(
                                authRepository = authRepository,
                                preferencesManager = preferencesManager,
                                onNavigateToSignIn = { currentSubScreen = SubScreen.SIGN_IN },
                                onNavigateToSettings = { currentSubScreen = SubScreen.SETTINGS },
                                onNavigateToFavorites = { currentTab = MainNavTab.FAVORITES },
                                onNavigateToHistory = { currentSubScreen = SubScreen.HISTORY },
                                onNavigateToDownloads = { currentTab = MainNavTab.DOWNLOADS },
                                onNavigateToAbout = { currentSubScreen = SubScreen.ABOUT },
                                onNavigateToNotifications = { currentSubScreen = SubScreen.NOTIFICATIONS }
                            )
                        }
                    }
                }
            }
        }
    }
}
