package com.example.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.MovieRepository
import com.example.data.NoxPreferencesManager
import com.example.data.auth.AuthRepository
import com.example.data.gemini.GeminiRepository
import com.example.ui.player.PlayerManager
import com.example.ui.screens.MainScreen
import com.example.ui.screens.auth.GoogleSignInScreen
import com.example.ui.screens.details.MovieDetailsScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.player.PlayerScreen
import com.example.ui.screens.splash.SplashScreen

@Composable
fun NoxApp(
    preferencesManager: NoxPreferencesManager,
    repository: MovieRepository,
    authRepository: AuthRepository,
    geminiRepository: GeminiRepository,
    playerManager: PlayerManager
) {
    val navController = rememberNavController()
    val isOnboardingCompleted by preferencesManager.isOnboardingCompleted.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable("splash") {
            SplashScreen(onFinish = {
                if (!isOnboardingCompleted) {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            })
        }

        composable("onboarding") {
            OnboardingScreen(onFinish = {
                preferencesManager.setOnboardingCompleted(true)
                navController.navigate("main") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }

        composable("signin") {
            GoogleSignInScreen(
                authRepository = authRepository,
                onSignInSuccess = {
                    navController.popBackStack()
                },
                onContinueAsGuest = {
                    navController.popBackStack()
                }
            )
        }

        composable("main") {
            MainScreen(
                repository = repository,
                preferencesManager = preferencesManager,
                authRepository = authRepository,
                geminiRepository = geminiRepository,
                playerManager = playerManager,
                onNavigateToDetails = { movieId ->
                    navController.navigate("details/$movieId")
                },
                onNavigateToPlayer = { movieId ->
                    navController.navigate("player/$movieId")
                }
            )
        }

        composable(
            "details/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(350)
                )
            }
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            MovieDetailsScreen(
                movieId = movieId,
                repository = repository,
                preferencesManager = preferencesManager,
                onBack = { navController.popBackStack() },
                onWatch = { id -> navController.navigate("player/$id") }
            )
        }

        composable(
            "player/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType }),
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            PlayerScreen(
                movieId = movieId,
                playerManager = playerManager,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
