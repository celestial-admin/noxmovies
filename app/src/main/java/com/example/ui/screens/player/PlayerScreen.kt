package com.example.ui.screens.player

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.data.MovieRepository
import com.example.model.Movie
import com.example.model.PlaybackQuality
import com.example.model.SubtitleLanguage
import com.example.ui.player.PlayerManager
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme
import kotlinx.coroutines.delay

enum class PlayerSheetType {
    NONE,
    SPEED,
    QUALITY,
    SUBTITLES,
    QUEUE
}

@kotlin.OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    movieId: String,
    playerManager: PlayerManager,
    repository: MovieRepository,
    onBack: () -> Unit
) {
    val colors = NoxTheme.colors

    var activeSheet by remember { mutableStateOf(PlayerSheetType.NONE) }
    var areControlsVisible by remember { mutableStateOf(true) }

    val currentMovie by playerManager.currentMovie.collectAsState()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val isBuffering by playerManager.isBuffering.collectAsState()
    val currentPositionMs by playerManager.currentPositionMs.collectAsState()
    val durationMs by playerManager.durationMs.collectAsState()
    val currentSpeed by playerManager.playbackSpeed.collectAsState()
    val currentQuality by playerManager.selectedQuality.collectAsState()
    val currentSubtitle by playerManager.selectedSubtitle.collectAsState()
    val queue by repository.watchQueue.collectAsState()

    LaunchedEffect(movieId) {
        if (currentMovie?.id != movieId) {
            val movieToPlay = repository.getMovie(movieId)
            movieToPlay?.let { playerManager.playMovie(it) }
        }
    }

    // Auto-hide controls after 4 seconds of inactivity when playing
    LaunchedEffect(areControlsVisible, isPlaying) {
        if (areControlsVisible && isPlaying && activeSheet == PlayerSheetType.NONE) {
            delay(4000)
            areControlsVisible = false
        }
    }

    fun formatTime(ms: Long): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                areControlsVisible = !areControlsVisible
            }
    ) {
        // ExoPlayer Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.exoPlayer
                    useController = false // Custom Compose Controls
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.accent)
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Bar: Back button + Title + Quality badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentMovie?.title ?: "Playing Media",
                                color = Color.White,
                                style = NoxTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${currentMovie?.year ?: ""} • ${currentQuality.label}",
                                color = colors.secondaryText,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Queue Button
                    IconButton(onClick = { activeSheet = PlayerSheetType.QUEUE }) {
                        Icon(
                            Icons.Default.QueueMusic,
                            contentDescription = "Watch Queue",
                            tint = colors.accent
                        )
                    }
                }

                // Center Transport Controls (10s back, Play/Pause, 10s fwd)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { playerManager.seekBackward(10000L) },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Replay10,
                            contentDescription = "10s Backward",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = { playerManager.togglePlayPause() },
                        modifier = Modifier
                            .size(72.dp)
                            .background(colors.accent, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    IconButton(
                        onClick = { playerManager.seekForward(10000L) },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Forward10,
                            contentDescription = "10s Forward",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Controls: Time, Seek Bar, Subtitle, Quality, Speed
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Time Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatTime(durationMs),
                            color = colors.secondaryText,
                            fontSize = 12.sp
                        )
                    }

                    // Seek Slider
                    Slider(
                        value = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f,
                        onValueChange = { fraction ->
                            playerManager.seekTo((fraction * durationMs).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = colors.cardElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Secondary Tools: Subtitles, Quality, Speed, Audio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { activeSheet = PlayerSheetType.SUBTITLES }) {
                            Icon(
                                Icons.Default.Subtitles,
                                contentDescription = null,
                                tint = if (currentSubtitle != SubtitleLanguage.OFF) colors.accent else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Subtitles",
                                color = if (currentSubtitle != SubtitleLanguage.OFF) colors.accent else Color.White,
                                fontSize = 12.sp
                            )
                        }

                        TextButton(onClick = { activeSheet = PlayerSheetType.QUALITY }) {
                            Icon(
                                Icons.Default.Hd,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentQuality.label,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        TextButton(onClick = { activeSheet = PlayerSheetType.SPEED }) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (currentSpeed != 1.0f) colors.accent else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentSpeed}x",
                                color = if (currentSpeed != 1.0f) colors.accent else Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets for Player Customization
    if (activeSheet != PlayerSheetType.NONE) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = PlayerSheetType.NONE },
            containerColor = colors.cardElevated,
            contentColor = colors.text
        ) {
            when (activeSheet) {
                PlayerSheetType.SPEED -> {
                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Text("Playback Speed", style = NoxTheme.typography.titleLarge, color = colors.text)
                        Spacer(modifier = Modifier.height(16.dp))
                        speeds.forEach { speed ->
                            val isSelected = (currentSpeed == speed)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.card else Color.Transparent)
                                    .clickable {
                                        playerManager.setSpeed(speed)
                                        activeSheet = PlayerSheetType.NONE
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${speed}x", color = if (isSelected) colors.accent else colors.text, fontWeight = FontWeight.SemiBold)
                                if (isSelected) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.accent)
                                }
                            }
                        }
                    }
                }

                PlayerSheetType.QUALITY -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Text("Streaming Quality", style = NoxTheme.typography.titleLarge, color = colors.text)
                        Spacer(modifier = Modifier.height(16.dp))
                        PlaybackQuality.values().forEach { quality ->
                            val isSelected = (currentQuality == quality)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.card else Color.Transparent)
                                    .clickable {
                                        playerManager.setQuality(quality)
                                        activeSheet = PlayerSheetType.NONE
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(quality.label, color = if (isSelected) colors.accent else colors.text, fontWeight = FontWeight.SemiBold)
                                if (isSelected) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.accent)
                                }
                            }
                        }
                    }
                }

                PlayerSheetType.SUBTITLES -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Text("Subtitles", style = NoxTheme.typography.titleLarge, color = colors.text)
                        Spacer(modifier = Modifier.height(16.dp))
                        SubtitleLanguage.values().forEach { sub ->
                            val isSelected = (currentSubtitle == sub)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.card else Color.Transparent)
                                    .clickable {
                                        playerManager.setSubtitle(sub)
                                        activeSheet = PlayerSheetType.NONE
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sub.label, color = if (isSelected) colors.accent else colors.text, fontWeight = FontWeight.SemiBold)
                                if (isSelected) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.accent)
                                }
                            }
                        }
                    }
                }

                PlayerSheetType.QUEUE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text("Watch Queue", style = NoxTheme.typography.titleLarge, color = colors.text)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("NOW PLAYING", color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(currentMovie?.title ?: "Nothing", color = colors.text, style = NoxTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("UP NEXT", color = colors.secondaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                            itemsIndexed(queue) { index, movie ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.card)
                                        .clickable {
                                            playerManager.playMovie(movie)
                                            activeSheet = PlayerSheetType.NONE
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(movie.title, color = colors.text, style = NoxTheme.typography.titleMedium, maxLines = 1)
                                        Text("${movie.year} • ${movie.duration}", color = colors.secondaryText, fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { repository.removeFromQueue(movie.id) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = colors.mutedText)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
