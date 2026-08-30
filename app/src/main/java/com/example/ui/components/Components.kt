package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Movie
import com.example.model.WatchHistoryItem
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme
import kotlinx.coroutines.delay

@Composable
fun HeroCarousel(
    movies: List<Movie>,
    onWatchClick: (String) -> Unit,
    onAddClick: (Movie) -> Unit,
    onDetailsClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return

    var currentIndex by remember { mutableStateOf(0) }

    // Auto-advance hero carousel every 6 seconds
    LaunchedEffect(movies.size) {
        if (movies.size > 1) {
            while (true) {
                delay(6000)
                currentIndex = (currentIndex + 1) % movies.size
            }
        }
    }

    val currentMovie = movies[currentIndex]
    val colors = NoxTheme.colors
    val hasAuthorizedPlayableSource = (currentMovie.authorizedSource != null && currentMovie.mediaUrl.isNotBlank())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f) // ~ 4:5 aspect ratio
            .clip(RoundedCornerShape(NoxDimensions.radiusHero))
            .background(colors.card)
    ) {
        AsyncImage(
            model = currentMovie.posterUrl,
            contentDescription = currentMovie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay blending to background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            colors.background.copy(alpha = 0.85f),
                            colors.background
                        ),
                        startY = 0f
                    )
                )
        )

        // Watermark
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "CINEMA",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = colors.cardElevated.copy(alpha = 0.35f),
                modifier = Modifier.rotate(-12f)
            )
        }

        // Hero Indicators
        if (movies.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                movies.take(5).forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentIndex) 8.dp else 6.dp)
                            .background(
                                color = if (index == currentIndex) colors.accent else colors.mutedText.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Badges Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Text(
                    text = "${currentMovie.year} • ${currentMovie.genres.firstOrNull() ?: currentMovie.genre.split(",").firstOrNull()?.trim() ?: ""} • ${currentMovie.duration}",
                    color = colors.secondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentMovie.title.uppercase(),
                style = NoxTheme.typography.headlineLarge,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentMovie.overview.ifBlank { currentMovie.description },
                style = NoxTheme.typography.bodyMedium,
                color = colors.secondaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (hasAuthorizedPlayableSource) {
                            onWatchClick(currentMovie.id)
                        } else {
                            (onDetailsClick ?: onWatchClick)(currentMovie.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(NoxDimensions.radiusButton),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        if (hasAuthorizedPlayableSource) Icons.Default.PlayArrow else Icons.Default.Tv,
                        contentDescription = "Watch",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasAuthorizedPlayableSource) "WATCH NOW" else "WHERE TO WATCH",
                        color = Color.Black,
                        style = NoxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { onAddClick(currentMovie) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.cardElevated, RoundedCornerShape(NoxDimensions.radiusButton))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add to Queue", tint = colors.text)
                }
            }
        }
    }
}

// Backward compatibility alias for single movie
@Composable
fun HeroBanner(
    movie: Movie,
    onWatchClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    HeroCarousel(
        movies = listOf(movie),
        onWatchClick = { onWatchClick() },
        onAddClick = { onFavoriteClick() }
    )
}

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Column(
        modifier = modifier
            .width(NoxDimensions.posterWidth)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(NoxDimensions.posterHeight)
                .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
                .background(colors.card)
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Rating Badge (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${movie.rating}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quality Badge (Bottom Left)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = movie.quality,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = movie.title,
            style = NoxTheme.typography.titleMedium,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "${movie.year} • ${movie.genre.split(",").firstOrNull()?.trim() ?: ""}",
            fontSize = 11.sp,
            color = colors.mutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ContinueWatchingCard(
    item: WatchHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Row(
        modifier = modifier
            .width(NoxDimensions.continueWatchingWidth)
            .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.cardElevated)
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Play Icon overlay
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(item.progress)
                        .background(colors.accent)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = colors.text,
                style = NoxTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitleInfo ?: item.genre,
                color = colors.secondaryText,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.remainingMinutes}m remaining",
                color = colors.mutedText,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun GenreChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) colors.accent else colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else colors.secondaryText,
            style = NoxTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String = "SEE ALL",
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            style = NoxTheme.typography.titleLarge,
            color = colors.text
        )
        Text(
            text = actionLabel.uppercase(),
            style = NoxTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = colors.accent,
            modifier = Modifier.clickable(onClick = onActionClick)
        )
    }
}

@Composable
fun MiniPlayerView(
    movie: Movie,
    isPlaying: Boolean,
    progress: Float,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
            .background(colors.cardElevated)
            .clickable(onClick = onExpand)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.card)
            ) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    color = colors.text,
                    style = NoxTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${movie.year} • ${movie.quality}",
                    color = colors.secondaryText,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = colors.accent
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close MiniPlayer",
                    tint = colors.mutedText
                )
            }
        }

        // Mini player accent progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(colors.card)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(colors.accent)
            )
        }
    }
}

@Composable
fun NoxEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String = "Explore NOX",
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(colors.cardElevated, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            style = NoxTheme.typography.titleLarge,
            color = colors.text,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = NoxTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
            shape = RoundedCornerShape(NoxDimensions.radiusButton)
        ) {
            Text(
                text = actionLabel,
                color = Color.Black,
                style = NoxTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NoxErrorView(
    message: String = "Something went wrong. Please try again.",
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error Loading Media",
            style = NoxTheme.typography.titleLarge,
            color = colors.text
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = NoxTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
            shape = RoundedCornerShape(NoxDimensions.radiusButton)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "RETRY", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val colors = NoxTheme.colors
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            colors.card,
            colors.cardElevated,
            colors.card
        ),
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier.background(brush)
    )
}

@Composable
fun MovieCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(NoxDimensions.posterWidth)) {
        ShimmerPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(NoxDimensions.posterHeight)
                .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerPlaceholder(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        ShimmerPlaceholder(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}
