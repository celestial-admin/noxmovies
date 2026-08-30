package com.example.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieRepository
import com.example.data.NoxPreferencesManager
import com.example.data.gemini.GeminiRepository
import com.example.model.AVAILABLE_REGIONS
import com.example.model.Movie
import com.example.model.NoxAiRecommendationResult
import com.example.ui.components.GenreChip
import com.example.ui.components.MovieCard
import com.example.ui.components.MovieCardSkeleton
import com.example.ui.components.NoxEmptyState
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SearchMode {
    CATALOG,
    ASK_NOX
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    repository: MovieRepository,
    preferencesManager: NoxPreferencesManager,
    geminiRepository: GeminiRepository,
    onNavigateToDetails: (String) -> Unit
) {
    val colors = NoxTheme.colors
    val scope = rememberCoroutineScope()
    val streamingRegion by preferencesManager.streamingRegion.collectAsState()
    val regionName = AVAILABLE_REGIONS.find { it.code.equals(streamingRegion, ignoreCase = true) }?.name ?: "India"

    var searchMode by remember { mutableStateOf(SearchMode.CATALOG) }

    // Catalog search state
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedGenre by remember { mutableStateOf("All") }
    var selectedProvider by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Relevance") }
    var minRating by remember { mutableDoubleStateOf(0.0) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    // AI Search / Ask NOX state
    var aiQuery by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }
    var aiResult by remember { mutableStateOf<NoxAiRecommendationResult?>(null) }
    var aiError by remember { mutableStateOf<String?>(null) }

    val recentSearches by preferencesManager.recentSearches.collectAsState()
    val currentUser by preferencesManager.currentUser.collectAsState()

    val categories = listOf("All", "Movies", "Series", "Anime")
    val sortOptions = listOf("Relevance", "Newest", "Rating", "A-Z")
    val providers = listOf("All", "Netflix", "Prime Video", "Disney+ Hotstar", "Apple TV", "YouTube", "JioCinema", "ZEE5")
    val genres = listOf("All", "Action", "Sci-Fi", "Drama", "Thriller", "Adventure", "Crime", "Comedy", "Fantasy", "Animation")

    val aiQuickPrompts = listOf(
        "Find me a sci-fi movie similar to Interstellar",
        "What should I watch tonight?",
        "5 psychological thrillers with strong mystery elements",
        "Mind-bending anime or cyberpunk series",
        "High-stakes crime dramas like Peaky Blinders"
    )

    // Debounced Catalog Search Trigger
    LaunchedEffect(query, selectedCategory, selectedGenre, selectedProvider, selectedSort, minRating, streamingRegion, searchMode) {
        if (searchMode == SearchMode.CATALOG) {
            isSearching = true
            searchError = null
            // Debounce user keystrokes
            if (query.isNotBlank()) {
                delay(350)
            }
            try {
                results = repository.filterMovies(
                    query = query,
                    category = selectedCategory,
                    genre = selectedGenre,
                    year = "All",
                    minRating = minRating,
                    sortBy = selectedSort,
                    providerFilter = selectedProvider,
                    region = streamingRegion
                )
            } catch (e: Exception) {
                searchError = "Unable to complete search. Please check your connection."
            } finally {
                isSearching = false
            }
        }
    }

    fun executeAiSearch(prompt: String) {
        if (prompt.isBlank()) return
        scope.launch {
            isAiLoading = true
            aiError = null
            preferencesManager.addRecentSearch(prompt)
            val allMovies = repository.getAllMovies()
            val tasteContext = preferencesManager.getUserTasteContext(
                favoriteGenres = listOf("Sci-Fi", "Thriller", "Action"),
                watchedTitles = listOf("Dune: Part Two", "Oppenheimer")
            )
            val response = geminiRepository.generateRecommendations(
                userPrompt = prompt,
                catalog = allMovies,
                userContext = tasteContext
            )
            isAiLoading = false
            response.fold(
                onSuccess = { aiResult = it },
                onFailure = { aiError = "AI features are temporarily unavailable." }
            )
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Mode Toggle Tabs (Catalog vs Ask NOX)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.card)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Catalog Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (searchMode == SearchMode.CATALOG) colors.cardElevated else Color.Transparent)
                            .clickable { searchMode = SearchMode.CATALOG }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = if (searchMode == SearchMode.CATALOG) colors.accent else colors.secondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Discovery Search",
                                color = if (searchMode == SearchMode.CATALOG) colors.text else colors.secondaryText,
                                fontSize = 12.sp,
                                fontWeight = if (searchMode == SearchMode.CATALOG) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    // Ask NOX AI Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (searchMode == SearchMode.ASK_NOX) colors.accent else Color.Transparent)
                            .clickable { searchMode = SearchMode.ASK_NOX }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = if (searchMode == SearchMode.ASK_NOX) Color.Black else colors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ask NOX (AI)",
                                color = if (searchMode == SearchMode.ASK_NOX) Color.Black else colors.text,
                                fontSize = 12.sp,
                                fontWeight = if (searchMode == SearchMode.ASK_NOX) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (searchMode == SearchMode.CATALOG) {
                    // Standard Search Input Field
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search movies, series, or streaming titles...",
                                color = colors.mutedText,
                                style = NoxTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = if (query.isNotBlank()) colors.accent else colors.mutedText
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = colors.secondaryText
                                        )
                                    }
                                }
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filters",
                                        tint = if (selectedGenre != "All" || selectedProvider != "All" || minRating > 0 || selectedSort != "Relevance") colors.accent else colors.secondaryText
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = colors.accent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = colors.card,
                            unfocusedContainerColor = colors.card,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            cursorColor = colors.accent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Filter Chips (Categories & Streaming Providers)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            GenreChip(
                                label = cat,
                                isSelected = (selectedCategory == cat),
                                onClick = { selectedCategory = cat }
                            )
                        }
                        item {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(28.dp)
                                    .padding(horizontal = 4.dp),
                                color = colors.border
                            )
                        }
                        items(providers.take(5)) { prov ->
                            GenreChip(
                                label = if (prov == "All") "All Services" else prov,
                                isSelected = (selectedProvider == prov),
                                onClick = { selectedProvider = prov }
                            )
                        }
                    }
                } else {
                    // ASK NOX AI Input Field
                    OutlinedTextField(
                        value = aiQuery,
                        onValueChange = { aiQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Ask in natural language (e.g. sci-fi similar to Interstellar)...",
                                color = colors.mutedText,
                                style = NoxTheme.typography.bodyMedium,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = colors.accent
                            )
                        },
                        trailingIcon = {
                            if (aiQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { executeAiSearch(aiQuery) },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(colors.accent)
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "Search",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = colors.accent,
                            unfocusedIndicatorColor = colors.cardElevated,
                            focusedContainerColor = colors.card,
                            unfocusedContainerColor = colors.card,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            cursorColor = colors.accent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = false,
                        maxLines = 2
                    )
                }
            }
        }
    ) { paddingValues ->
        if (searchMode == SearchMode.CATALOG) {
            // Standard Catalog Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Recent Searches
                if (query.isEmpty() && recentSearches.isNotEmpty() && selectedCategory == "All" && selectedGenre == "All" && selectedProvider == "All") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = NoxTheme.typography.titleMedium,
                                color = colors.secondaryText
                            )
                            Text(
                                text = "Clear",
                                color = colors.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { preferencesManager.clearRecentSearches() }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recentSearches) { searchItem ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(colors.card)
                                        .clickable {
                                            query = searchItem
                                            preferencesManager.addRecentSearch(searchItem)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = colors.mutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = searchItem,
                                        color = colors.text,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = colors.mutedText,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { preferencesManager.removeRecentSearch(searchItem) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Results count, streaming region badge, and active sort label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${results.size} Titles Found • $regionName",
                        color = colors.secondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Sort: $selectedSort",
                        color = colors.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showFilterSheet = true }
                    )
                }

                // Grid Content or Error states
                if (isSearching) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
                    ) {
                        items(6) {
                            MovieCardSkeleton(modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else if (searchError != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = searchError ?: "Search Error",
                            style = NoxTheme.typography.titleMedium,
                            color = colors.text,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { query = query },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                        ) {
                            Text("Retry Search", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (results.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
                    ) {
                        items(results) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = {
                                    if (query.isNotBlank()) {
                                        preferencesManager.addRecentSearch(query)
                                    }
                                    onNavigateToDetails(movie.id)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    NoxEmptyState(
                        icon = Icons.Default.Search,
                        title = "Nothing Found",
                        subtitle = "No movies or TV shows match your search and filter criteria. Try adjusting your query or streaming region.",
                        actionLabel = "Reset Filters",
                        onActionClick = {
                            query = ""
                            selectedCategory = "All"
                            selectedGenre = "All"
                            selectedProvider = "All"
                            selectedSort = "Relevance"
                            minRating = 0.0
                        },
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            }
        } else {
            // ASK NOX (AI SEARCH) LAYOUT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
            ) {
                // Quick Inspiration Prompt Chips
                Text(
                    text = "Try asking NOX:",
                    style = NoxTheme.typography.titleSmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(aiQuickPrompts) { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.card)
                                .border(1.dp, colors.cardElevated, RoundedCornerShape(16.dp))
                                .clickable {
                                    aiQuery = prompt
                                    executeAiSearch(prompt)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                color = colors.text,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Response / Search Results
                if (isAiLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.card)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = colors.accent, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Analyzing cinematic intent with NOX AI...",
                            color = colors.text,
                            style = NoxTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Matching structured criteria against verified library",
                            color = colors.secondaryText,
                            style = NoxTheme.typography.labelSmall
                        )
                    }
                } else if (aiError != null) {
                    Surface(
                        color = Color(0x22EF4444),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFF6B6B))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = aiError ?: "AI features are temporarily unavailable.",
                                color = Color(0xFFFF6B6B),
                                style = NoxTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (aiResult != null) {
                    val result = aiResult!!

                    Column(modifier = Modifier.fillMaxSize()) {
                        // AI Commentary Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.cardElevated)
                                .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(colors.accent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "NOX AI Curator",
                                    color = colors.accent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (!currentUser.isGuest) {
                                    Text(
                                        text = "Personalized for ${currentUser.displayName.split(" ").first()}",
                                        color = colors.mutedText,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = result.aiResponseText,
                                color = colors.text,
                                style = NoxTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            result.searchCriteria?.let { criteria ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (criteria.mood.isNotEmpty()) {
                                        CriteriaBadge(label = criteria.mood)
                                    }
                                    criteria.genres.forEach { g ->
                                        CriteriaBadge(label = g)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Validated NOX Catalog Matches (${result.recommendedMovies.size})",
                            style = NoxTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(result.recommendedMovies) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = { onNavigateToDetails(movie.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    // Initial Ask NOX State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.card)
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.cardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Conversational Movie Discovery",
                            style = NoxTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Describe any mood, theme, complex query, or favorite filmmaker. NOX AI interprets your intent and retrieves verified, high-quality streams from the library.",
                            style = NoxTheme.typography.bodySmall,
                            color = colors.secondaryText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet for Catalog
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = colors.cardElevated,
            contentColor = colors.text
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Filters & Streaming Options",
                    style = NoxTheme.typography.titleLarge,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sort
                Text(
                    text = "Sort By",
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sortOptions) { sort ->
                        GenreChip(
                            label = sort,
                            isSelected = (selectedSort == sort),
                            onClick = { selectedSort = sort }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Streaming Provider Filter
                Text(
                    text = "Streaming Provider ($regionName)",
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(providers) { prov ->
                        GenreChip(
                            label = prov,
                            isSelected = (selectedProvider == prov),
                            onClick = { selectedProvider = prov }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Genre
                Text(
                    text = "Genre",
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(genres) { genre ->
                        GenreChip(
                            label = genre,
                            isSelected = (selectedGenre == genre),
                            onClick = { selectedGenre = genre }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Min Rating
                Text(
                    text = "Minimum Rating: ${if (minRating == 0.0) "Any" else "★ $minRating+"}",
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = minRating.toFloat(),
                    onValueChange = { minRating = (it * 10).toInt() / 10.0 },
                    valueRange = 0f..9f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.accent,
                        activeTrackColor = colors.accent,
                        inactiveTrackColor = colors.card
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(NoxDimensions.radiusButton),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Apply Filters",
                        color = Color.Black,
                        style = NoxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CriteriaBadge(label: String) {
    val colors = NoxTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.card)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = colors.accent,
            fontWeight = FontWeight.SemiBold
        )
    }
}
