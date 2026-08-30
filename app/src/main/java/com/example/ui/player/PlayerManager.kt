package com.example.ui.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.MovieRepository
import com.example.data.NoxPreferencesManager
import com.example.model.Movie
import com.example.model.PlaybackQuality
import com.example.model.SubtitleLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerManager(
    private val context: Context,
    private val repository: MovieRepository,
    private val preferencesManager: NoxPreferencesManager
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startProgressUpdates()
                    } else {
                        stopProgressUpdates()
                        saveCurrentPosition()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isBuffering.value = (playbackState == Player.STATE_BUFFERING)
                    if (playbackState == Player.STATE_READY) {
                        _durationMs.value = duration.coerceAtLeast(0L)
                    } else if (playbackState == Player.STATE_ENDED) {
                        playNextInQueue()
                    }
                }
            })
        }
    }

    private val _currentMovie = MutableStateFlow<Movie?>(null)
    val currentMovie: StateFlow<Movie?> = _currentMovie.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isMiniPlayerVisible = MutableStateFlow(false)
    val isMiniPlayerVisible: StateFlow<Boolean> = _isMiniPlayerVisible.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _selectedQuality = MutableStateFlow(PlaybackQuality.AUTO)
    val selectedQuality: StateFlow<PlaybackQuality> = _selectedQuality.asStateFlow()

    private val _selectedSubtitle = MutableStateFlow(SubtitleLanguage.OFF)
    val selectedSubtitle: StateFlow<SubtitleLanguage> = _selectedSubtitle.asStateFlow()

    fun playMovie(movie: Movie, startPositionMs: Long? = null) {
        _currentMovie.value = movie
        _isMiniPlayerVisible.value = false

        val savedPosition = startPositionMs ?: preferencesManager.getPlaybackPosition(movie.id)

        val mediaItem = MediaItem.fromUri(movie.mediaUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        if (savedPosition > 0) {
            exoPlayer.seekTo(savedPosition)
        }
        exoPlayer.playWhenReady = true
        _isPlaying.value = true

        startProgressUpdates()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentPositionMs.value = positionMs
        saveCurrentPosition()
    }

    fun seekForward(offsetMs: Long = 10000L) {
        val newPos = (exoPlayer.currentPosition + offsetMs).coerceAtMost(exoPlayer.duration)
        seekTo(newPos)
    }

    fun seekBackward(offsetMs: Long = 10000L) {
        val newPos = (exoPlayer.currentPosition - offsetMs).coerceAtLeast(0L)
        seekTo(newPos)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer.playbackParameters = PlaybackParameters(speed)
    }

    fun setQuality(quality: PlaybackQuality) {
        _selectedQuality.value = quality
    }

    fun setSubtitle(subtitle: SubtitleLanguage) {
        _selectedSubtitle.value = subtitle
    }

    fun showMiniPlayer() {
        if (_currentMovie.value != null) {
            _isMiniPlayerVisible.value = true
        }
    }

    fun hideMiniPlayer() {
        _isMiniPlayerVisible.value = false
    }

    fun dismissPlayer() {
        saveCurrentPosition()
        exoPlayer.stop()
        _currentMovie.value = null
        _isMiniPlayerVisible.value = false
        _isPlaying.value = false
        stopProgressUpdates()
    }

    fun playNextInQueue() {
        val queue = repository.watchQueue.value
        val current = _currentMovie.value
        if (current != null && queue.isNotEmpty()) {
            val currentIndex = queue.indexOfFirst { it.id == current.id }
            if (currentIndex != -1 && currentIndex + 1 < queue.size) {
                playMovie(queue[currentIndex + 1])
                return
            }
        }
        dismissPlayer()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    _currentPositionMs.value = pos
                    _durationMs.value = dur
                    
                    _currentMovie.value?.let { movie ->
                        repository.recordWatchHistory(movie.id, pos, dur)
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun saveCurrentPosition() {
        _currentMovie.value?.let { movie ->
            val pos = exoPlayer.currentPosition
            preferencesManager.savePlaybackPosition(movie.id, pos)
            repository.recordWatchHistory(movie.id, pos, exoPlayer.duration)
        }
    }

    fun release() {
        stopProgressUpdates()
        saveCurrentPosition()
        exoPlayer.release()
    }
}
