package com.example.ui.screens.details

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MovieRepository
import com.example.data.NoxPreferencesManager
import com.example.model.AVAILABLE_REGIONS
import com.example.model.Movie
import com.example.model.ProviderType
import com.example.model.Season
import com.example.model.WatchProvider
import com.example.ui.components.GenreChip
import com.example.ui.components.MovieCard
import com.example.ui.components.NoxErrorView
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    movieId: String,
    repository: MovieRepository,
    preferencesManager: NoxPreferencesManager,
    onBack: () -> Unit,
    onWatch: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colors = NoxTheme.colors
    val selectedRegionCode by preferencesManager.streamingRegion.collectAsState()
    val currentRegionName = AVAILABLE_REGIONS.find { it.code.equals(selectedRegionCode, ignoreCase = true) }?.name ?: "India"

    var movie by remember { mutableStateOf<Movie?>(null) }
    var watchProviders by remember { mutableStateOf<List<WatchProvider>>(emptyList()) }
    var relatedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(preferencesManager.isFavorite(movieId)) }
    var isDownloading by remember { mutableStateOf(false) }
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }
    var showSeasonMenu by remember { mutableStateOf(false) }

    val heartScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "heart_scale"
    )

    LaunchedEffect(movieId, selectedRegionCode) {
        isLoading = true
        val fetchedMovie = repository.getDetailsWithRegion(movieId, selectedRegionCode)
        movie = fetchedMovie
        if (fetchedMovie != null) {
            watchProviders = repository.getWatchProviders(fetchedMovie.id, selectedRegionCode)
            val primaryGenre = fetchedMovie.genres.firstOrNull() ?: fetchedMovie.genre.split(",").firstOrNull()?.trim() ?: ""
            relatedMovies = repository.getMoviesByGenre(primaryGenre).filter { it.id != fetchedMovie.id }
        }
        isFavorite = preferencesManager.isFavorite(movieId)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.accent)
        }
        return
    }

    val currentMovie = movie
    if (currentMovie == null) {
        NoxErrorView(
            message = "The requested media title could not be found.",
            onRetry = {
                movie = MovieRepository.mockMovies.find { it.id == movieId }
            }
        )
        return
    }

    val scrollState = rememberScrollState()
    val hasAuthorizedPlayableSource = (currentMovie.authorizedSource != null && currentMovie.mediaUrl.isNotBlank())
    val activeProviders = if (watchProviders.isNotEmpty()) watchProviders else currentMovie.whereToWatch

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp)
        ) {
            // Backdrop Header with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                AsyncImage(
                    model = currentMovie.backdropUrl.ifBlank { currentMovie.posterUrl },
                    contentDescription = currentMovie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Fading Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    colors.background.copy(alpha = 0.85f),
                                    colors.background
                                ),
                                startY = 0f
                            )
                        )
                )

                // Poster & Metadata Header in overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(160.dp)
                            .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
                            .background(colors.card)
                            .border(1.dp, colors.border, RoundedCornerShape(NoxDimensions.radiusMedium))
                    ) {
                        AsyncImage(
                            model = currentMovie.posterUrl,
                            contentDescription = currentMovie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        // Type Badge + Quality + Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(colors.accent, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (currentMovie.isSeries) "TV SERIES" else currentMovie.quality,
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format("%.1f", currentMovie.rating),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentMovie.title,
                            style = NoxTheme.typography.headlineMedium,
                            color = colors.text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${currentMovie.year} • ${currentMovie.duration}",
                            style = NoxTheme.typography.labelMedium,
                            color = colors.secondaryText
                        )
                    }
                }
            }

            // Body Content
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                // SECTION 20: HOME / DETAILS WATCH ACTION LOGIC
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasAuthorizedPlayableSource) {
                        // Case 1: NOX has authorized playback stream -> [ WATCH NOW ▶ ]
                        Button(
                            onClick = { onWatch(currentMovie.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            shape = RoundedCornerShape(NoxDimensions.radiusButton),
                            modifier = Modifier
                                .weight(1.8f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WATCH NOW",
                                color = Color.Black,
                                style = NoxTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Case 2: Discovery Title -> [ WHERE TO WATCH 🍿 ]
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(600)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            shape = RoundedCornerShape(NoxDimensions.radiusButton),
                            modifier = Modifier
                                .weight(1.8f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Tv, contentDescription = "Where to watch", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WHERE TO WATCH",
                                color = Color.Black,
                                style = NoxTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Download Button (enabled only for authorized media)
                    if (hasAuthorizedPlayableSource) {
                        IconButton(
                            onClick = {
                                isDownloading = true
                                repository.startDownload(currentMovie, currentMovie.quality)
                                Toast.makeText(context, "Added to downloads", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(colors.cardElevated, RoundedCornerShape(NoxDimensions.radiusButton))
                        ) {
                            Icon(
                                imageVector = if (isDownloading) Icons.Filled.Check else Icons.Outlined.Download,
                                contentDescription = "Download",
                                tint = if (isDownloading) colors.accent else colors.text
                            )
                        }
                    }

                    // Favorite / Bookmark Button
                    IconButton(
                        onClick = {
                            isFavorite = preferencesManager.toggleFavorite(currentMovie.id)
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(colors.cardElevated, RoundedCornerShape(NoxDimensions.radiusButton))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) colors.accent else colors.text,
                            modifier = Modifier.scale(heartScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Genres Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val genreList = if (currentMovie.genres.isNotEmpty()) currentMovie.genres else currentMovie.genre.split(",").map { it.trim() }
                    genreList.take(4).forEach { genreName ->
                        GenreChip(
                            label = genreName,
                            isSelected = false,
                            onClick = { }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Plot Overview
                Text(
                    text = "SYNOPSIS",
                    style = NoxTheme.typography.labelSmall,
                    color = colors.secondaryText,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentMovie.overview.ifBlank { currentMovie.description },
                    style = NoxTheme.typography.bodyMedium,
                    color = colors.text.copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                // TV Series Seasons & Episodes Selector (if available)
                if (currentMovie.isSeries && currentMovie.seasons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "EPISODES",
                        style = NoxTheme.typography.labelSmall,
                        color = colors.secondaryText,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val seasons = currentMovie.seasons
                    val selectedSeason = seasons.getOrElse(selectedSeasonIndex) { seasons.first() }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        selectedSeason.episodes.forEach { episode ->
                            Card(
                                onClick = { onWatch(currentMovie.id) },
                                colors = CardDefaults.cardColors(containerColor = colors.card),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp, 45.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.cardElevated)
                                    ) {
                                        AsyncImage(
                                            model = episode.thumbnail,
                                            contentDescription = episode.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "E${episode.episodeNumber} • ${episode.title}",
                                            style = NoxTheme.typography.bodyMedium,
                                            color = colors.text,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = episode.duration,
                                            style = NoxTheme.typography.labelSmall,
                                            color = colors.secondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SECTION 6, 7, 11: WHERE TO WATCH
                // ==========================================
                Spacer(modifier = Modifier.height(28.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WHERE TO WATCH",
                            style = NoxTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Available in $currentRegionName",
                            style = NoxTheme.typography.labelSmall,
                            color = colors.accent
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(colors.cardElevated, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${activeProviders.size} Providers",
                            color = colors.secondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (activeProviders.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.card),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.secondaryText,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No streaming providers currently listed for $currentRegionName.",
                                style = NoxTheme.typography.bodyMedium,
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Grouped / Categorized Watch Providers
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        activeProviders.forEach { provider ->
                            WatchProviderCard(
                                provider = provider,
                                regionName = currentRegionName,
                                onOpenProvider = { targetUrl ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Opening ${provider.name}...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }

                // ==========================================
                // SECTION 11: TRAILER
                // ==========================================
                currentMovie.trailerUrl?.let { trailerUrl ->
                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "OFFICIAL TRAILER",
                        style = NoxTheme.typography.titleMedium,
                        color = colors.text,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening trailer...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = colors.card),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = currentMovie.backdropUrl.ifBlank { currentMovie.posterUrl },
                                contentDescription = "Trailer",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f))
                            )
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.PlayCircleOutline,
                                    contentDescription = "Play Trailer",
                                    tint = colors.accent,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Watch Official Trailer",
                                    style = NoxTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // SECTION 11: SIMILAR TITLES
                // ==========================================
                if (relatedMovies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(28.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "SIMILAR TITLES",
                        style = NoxTheme.typography.titleMedium,
                        color = colors.text,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        items(relatedMovies) { related ->
                            MovieCard(
                                movie = related,
                                onClick = {
                                    movie = related
                                    watchProviders = related.whereToWatch
                                }
                            )
                        }
                    }
                }

                // ==========================================
                // SECTION 13 & 14: WATCHMODE ATTRIBUTION
                // ==========================================
                Spacer(modifier = Modifier.height(36.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.secondaryText.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Streaming availability data powered by Watchmode.",
                        style = NoxTheme.typography.labelSmall,
                        color = colors.secondaryText.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Floating Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 44.dp, start = 16.dp)
                .size(42.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
fun WatchProviderCard(
    provider: WatchProvider,
    regionName: String,
    onOpenProvider: (String) -> Unit
) {
    val colors = NoxTheme.colors

    val typeLabel = when (provider.type) {
        ProviderType.SUBSCRIPTION -> "Subscription"
        ProviderType.FREE -> "Free"
        ProviderType.RENT -> if (provider.price != null) "Rent (${provider.price})" else "Rent"
        ProviderType.BUY -> if (provider.price != null) "Buy (${provider.price})" else "Buy"
        ProviderType.TV -> "TV Network"
    }

    val typeBadgeColor = when (provider.type) {
        ProviderType.SUBSCRIPTION -> colors.accent
        ProviderType.FREE -> Color(0xFF10B981) // Green
        ProviderType.RENT, ProviderType.BUY -> Color(0xFFF59E0B) // Amber
        ProviderType.TV -> Color(0xFF8B5CF6) // Purple
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Provider Avatar / Logo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.cardElevated),
                contentAlignment = Alignment.Center
            ) {
                if (provider.logoUrl != null) {
                    AsyncImage(
                        model = provider.logoUrl,
                        contentDescription = provider.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Icon(
                        Icons.Default.Movie,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details: Name, Type Badge, Format
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    style = NoxTheme.typography.titleMedium,
                    color = colors.text,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(typeBadgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            color = typeBadgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    provider.format?.let { fmt ->
                        Text(
                            text = "• $fmt",
                            style = NoxTheme.typography.labelSmall,
                            color = colors.secondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Watch Action Button (Launches official provider URL/deeplink)
            Button(
                onClick = {
                    val targetUrl = provider.androidUrl ?: provider.webUrl ?: "https://www.google.com/search?q=${Uri.encode(provider.name)}"
                    onOpenProvider(targetUrl)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cardElevated,
                    contentColor = colors.accent
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when (provider.type) {
                        ProviderType.RENT -> "RENT"
                        ProviderType.BUY -> "BUY"
                        else -> "WATCH"
                    },
                    style = NoxTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
