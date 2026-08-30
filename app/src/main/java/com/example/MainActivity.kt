package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.MovieRepository
import com.example.data.NoxPreferencesManager
import com.example.data.auth.AuthRepository
import com.example.data.gemini.GeminiRepository
import com.example.data.watchmode.WatchmodeApiService
import com.example.data.watchmode.WatchmodeCache
import com.example.ui.NoxApp
import com.example.ui.player.PlayerManager
import com.example.ui.theme.NoxTheme

class MainActivity : ComponentActivity() {

    private lateinit var playerManager: PlayerManager
    private lateinit var preferencesManager: NoxPreferencesManager
    private lateinit var watchmodeApi: WatchmodeApiService
    private lateinit var watchmodeCache: WatchmodeCache
    private lateinit var repository: MovieRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var geminiRepository: GeminiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = NoxPreferencesManager(applicationContext)
        watchmodeCache = WatchmodeCache(applicationContext)
        watchmodeApi = WatchmodeApiService()
        repository = MovieRepository(watchmodeApi = watchmodeApi, cache = watchmodeCache)
        authRepository = AuthRepository(applicationContext, preferencesManager)
        geminiRepository = GeminiRepository(repository)
        playerManager = PlayerManager(applicationContext, repository, preferencesManager)

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState()
            val accentColor by preferencesManager.accentColor.collectAsState()

            NoxTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NoxTheme.colors.background)
                ) {
                    NoxApp(
                        preferencesManager = preferencesManager,
                        repository = repository,
                        authRepository = authRepository,
                        geminiRepository = geminiRepository,
                        playerManager = playerManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::playerManager.isInitialized) {
            playerManager.release()
        }
    }
}
