package com.example.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieRepository
import com.example.data.NoxPreferencesManager
import com.example.model.Movie
import com.example.ui.components.MovieCard
import com.example.ui.components.NoxEmptyState
import com.example.ui.theme.NoxTheme

@Composable
fun FavoritesScreen(
    repository: MovieRepository,
    preferencesManager: NoxPreferencesManager,
    onNavigateToDetails: (String) -> Unit,
    onExplore: () -> Unit
) {
    val colors = NoxTheme.colors
    val favoriteIds by preferencesManager.favoriteIds.collectAsState()

    val favoriteMovies = remember(favoriteIds) {
        MovieRepository.mockMovies.filter { favoriteIds.contains(it.id) }
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
                Text(
                    text = "Favorites",
                    style = NoxTheme.typography.headlineLarge,
                    color = colors.text
                )
                if (favoriteMovies.isNotEmpty()) {
                    Text(
                        text = "${favoriteMovies.size} Saved",
                        color = colors.secondaryText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        if (favoriteMovies.isEmpty()) {
            NoxEmptyState(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Nothing saved yet.",
                subtitle = "Add movies and series to your favorites to quickly access them here anytime.",
                actionLabel = "Explore NOX",
                onActionClick = onExplore,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
                items(favoriteMovies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onNavigateToDetails(movie.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
