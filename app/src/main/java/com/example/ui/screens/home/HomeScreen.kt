package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieRepository
import com.example.model.Movie
import com.example.model.WatchHistoryItem
import com.example.ui.components.*
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme

@Composable
fun HomeScreen(
    repository: MovieRepository,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val colors = NoxTheme.colors

    var trendingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var recommendedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var newReleases by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var recentlyAdded by remember { mutableStateOf<List<Movie>>(emptyList()) }
    val historyItems by repository.history.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val unreadNotificationsCount = notifications.count { !it.isRead }

    var selectedGenre by remember { mutableStateOf("All") }
    var genreMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val genres = listOf(
        "All", "Action", "Sci-Fi", "Drama", "Thriller", "Adventure",
        "Comedy", "Crime", "Fantasy", "Horror", "Mystery", "Anime"
    )

    LaunchedEffect(Unit) {
        isLoading = true
        trendingMovies = repository.getTrending()
        recommendedMovies = repository.getRecommended()
        newReleases = repository.getNewReleases()
        popularMovies = repository.getPopular()
        recentlyAdded = repository.getRecentlyAdded()
        genreMovies = repository.getMoviesByGenre(selectedGenre)
        isLoading = false
    }

    LaunchedEffect(selectedGenre) {
        genreMovies = repository.getMoviesByGenre(selectedGenre)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // NOX Logo
                Text(
                    text = "NOX",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = colors.text
                )

                // Actions: Notification + Profile Avatar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Button with badge
                    Box {
                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier
                                .size(40.dp)
                                .background(colors.card, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = colors.text,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(10.dp)
                                    .background(colors.accent, CircleShape)
                            )
                        }
                    }

                    // Profile Avatar
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.card, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = colors.text,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .aspectRatio(0.85f)
                            .clip(RoundedCornerShape(NoxDimensions.radiusHero))
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            MovieCardSkeleton()
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Search Bar Trigger
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.card)
                            .clickable(onClick = onNavigateToSearch)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.mutedText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Search movies, series, people...",
                                color = colors.secondaryText,
                                style = NoxTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Hero Banner Carousel
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    HeroCarousel(
                        movies = trendingMovies.take(5),
                        onWatchClick = { onNavigateToPlayer(it) },
                        onDetailsClick = { onNavigateToDetails(it) },
                        onAddClick = { repository.addToQueue(it) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // Trending Now Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = "Trending Now",
                        actionLabel = "SEE ALL",
                        onActionClick = onNavigateToSearch
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(trendingMovies) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onNavigateToDetails(movie.id) }
                            )
                        }
                    }
                }

                // Continue Watching Section
                if (historyItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionHeader(
                            title = "Continue Watching",
                            actionLabel = "HISTORY",
                            onActionClick = onNavigateToHistory
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(historyItems) { historyItem ->
                                ContinueWatchingCard(
                                    item = historyItem,
                                    onClick = { onNavigateToPlayer(historyItem.movieId) }
                                )
                            }
                        }
                    }
                }

                // Recommended for You Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = "Recommended for you",
                        actionLabel = "MORE",
                        onActionClick = onNavigateToSearch
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recommendedMovies) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onNavigateToDetails(movie.id) }
                            )
                        }
                    }
                }

                // Genres Interactive Chips Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Browse Genres",
                        style = NoxTheme.typography.titleLarge,
                        color = colors.text,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(genres) { genre ->
                            GenreChip(
                                label = genre,
                                isSelected = (selectedGenre == genre),
                                onClick = { selectedGenre = genre }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(genreMovies) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onNavigateToDetails(movie.id) }
                            )
                        }
                    }
                }

                // New Releases Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = "New Releases",
                        actionLabel = "EXPLORE",
                        onActionClick = onNavigateToSearch
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(newReleases) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onNavigateToDetails(movie.id) }
                            )
                        }
                    }
                }

                // Popular Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = "Popular on NOX",
                        actionLabel = "ALL",
                        onActionClick = onNavigateToSearch
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(popularMovies) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onNavigateToDetails(movie.id) }
                            )
                        }
                    }
                }

                // Recently Added Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader(
                        title = "Recently Added",
                        actionLabel = "ALL",
                        onActionClick = onNavigateToSearch
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recentlyAdded) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onNavigateToDetails(movie.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
