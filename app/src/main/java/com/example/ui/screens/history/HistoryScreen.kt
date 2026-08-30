package com.example.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MovieRepository
import com.example.model.WatchHistoryItem
import com.example.ui.components.NoxEmptyState
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme

@Composable
fun HistoryScreen(
    repository: MovieRepository,
    onBack: () -> Unit,
    onPlayMovie: (String) -> Unit,
    onExplore: () -> Unit
) {
    val colors = NoxTheme.colors
    val historyList by repository.history.collectAsState()

    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L

    val todayItems = historyList.filter { (now - it.lastWatchedTimestamp) < oneDayMs }
    val yesterdayItems = historyList.filter { (now - it.lastWatchedTimestamp) in oneDayMs..(2 * oneDayMs) }
    val earlierItems = historyList.filter { (now - it.lastWatchedTimestamp) > 2 * oneDayMs }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.text
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Watch History",
                        style = NoxTheme.typography.titleLarge,
                        color = colors.text
                    )
                }

                if (historyList.isNotEmpty()) {
                    TextButton(onClick = { repository.clearHistory() }) {
                        Text(
                            text = "Clear All",
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (historyList.isEmpty()) {
            NoxEmptyState(
                icon = Icons.Default.History,
                title = "No watch history",
                subtitle = "Titles you watch will automatically be saved here so you can resume anytime.",
                actionLabel = "Start Exploring",
                onActionClick = onExplore,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (todayItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Today",
                            style = NoxTheme.typography.titleMedium,
                            color = colors.accent
                        )
                    }
                    items(todayItems, key = { it.movieId }) { item ->
                        HistoryRowItem(
                            item = item,
                            onClick = { onPlayMovie(item.movieId) },
                            onDelete = { repository.removeHistoryItem(item.movieId) }
                        )
                    }
                }

                if (yesterdayItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Yesterday",
                            style = NoxTheme.typography.titleMedium,
                            color = colors.accent
                        )
                    }
                    items(yesterdayItems, key = { it.movieId }) { item ->
                        HistoryRowItem(
                            item = item,
                            onClick = { onPlayMovie(item.movieId) },
                            onDelete = { repository.removeHistoryItem(item.movieId) }
                        )
                    }
                }

                if (earlierItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Earlier",
                            style = NoxTheme.typography.titleMedium,
                            color = colors.accent
                        )
                    }
                    items(earlierItems, key = { it.movieId }) { item ->
                        HistoryRowItem(
                            item = item,
                            onClick = { onPlayMovie(item.movieId) },
                            onDelete = { repository.removeHistoryItem(item.movieId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRowItem(
    item: WatchHistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = NoxTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
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

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = NoxTheme.typography.titleMedium,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitleInfo ?: item.genre,
                color = colors.secondaryText,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${(item.progress * 100).toInt()}% watched",
                color = colors.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Delete from history", tint = colors.mutedText)
        }
    }
}
