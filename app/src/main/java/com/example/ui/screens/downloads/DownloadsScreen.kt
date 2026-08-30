package com.example.ui.screens.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.model.DownloadItem
import com.example.ui.components.NoxEmptyState
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    repository: MovieRepository,
    onPlayMovie: (String) -> Unit,
    onExplore: () -> Unit
) {
    val colors = NoxTheme.colors
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    val downloads by repository.downloads.collectAsState()
    val downloadingItems = downloads.filter { !it.isCompleted }
    val completedItems = downloads.filter { it.isCompleted }

    val tabs = listOf("Downloading (${downloadingItems.size})", "Completed (${completedItems.size})")

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Downloads",
                        style = NoxTheme.typography.headlineLarge,
                        color = colors.text
                    )

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = colors.text)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(colors.cardElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear All Downloads", color = colors.text) },
                                onClick = {
                                    repository.clearAllDownloads()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Material 3 Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = colors.card,
                    contentColor = colors.accent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = colors.accent
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = (selectedTabIndex == index),
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTabIndex == index) colors.accent else colors.secondaryText,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
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
            if (selectedTabIndex == 0) {
                // Downloading Tab
                if (downloadingItems.isEmpty()) {
                    NoxEmptyState(
                        icon = Icons.Default.Download,
                        title = "No active downloads",
                        subtitle = "Select any authorized movie or series to download and enjoy offline.",
                        actionLabel = "Browse Media",
                        onActionClick = onExplore,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(downloadingItems, key = { it.id }) { item ->
                            DownloadingCard(
                                item = item,
                                onTogglePause = { repository.togglePauseDownload(item.id) },
                                onCancel = { repository.cancelDownload(item.id) }
                            )
                        }
                    }
                }
            } else {
                // Completed Tab
                if (completedItems.isEmpty()) {
                    NoxEmptyState(
                        icon = Icons.Outlined.DownloadDone,
                        title = "No completed downloads",
                        subtitle = "Downloaded media ready for offline playback will appear here.",
                        actionLabel = "Discover Movies",
                        onActionClick = onExplore,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(completedItems, key = { it.id }) { item ->
                            CompletedCard(
                                item = item,
                                onPlay = { onPlayMovie(item.movieId) },
                                onDelete = { repository.deleteCompletedDownload(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadingCard(
    item: DownloadItem,
    onTogglePause: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = NoxTheme.colors

    Card(
        shape = RoundedCornerShape(NoxDimensions.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardElevated)
                ) {
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
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
                        text = "${item.quality} • ${item.speed}",
                        color = colors.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.formattedDownloadedSize} / ${item.formattedTotalSize} • ${(item.progress * 100).toInt()}%",
                        color = colors.secondaryText,
                        fontSize = 11.sp
                    )
                }

                // Pause / Resume & Cancel
                IconButton(onClick = onTogglePause) {
                    Icon(
                        imageVector = if (item.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (item.isPaused) "Resume" else "Pause",
                        tint = colors.accent
                    )
                }

                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cancel",
                        tint = colors.mutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = colors.accent,
                trackColor = colors.cardElevated
            )
        }
    }
}

@Composable
fun CompletedCard(
    item: DownloadItem,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = NoxTheme.colors

    Card(
        shape = RoundedCornerShape(NoxDimensions.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = colors.card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.cardElevated)
            ) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
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
                    text = "${item.quality} • ${item.formattedTotalSize}",
                    color = colors.secondaryText,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PLAY", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.mutedText)
            }
        }
    }
}
