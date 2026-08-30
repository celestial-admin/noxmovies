package com.example.data.gemini

import com.example.data.MovieRepository
import com.example.model.Movie
import com.example.model.NoxAiRecommendationResult
import com.example.model.SearchIntentResult
import com.example.model.UserTasteContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

class GeminiRepository(
    private val movieRepository: MovieRepository
) : GeminiService {

    override suspend fun parseSearchIntent(
        userPrompt: String,
        userContext: UserTasteContext?
    ): Result<SearchIntentResult> = withContext(Dispatchers.Default) {
        try {
            val queryLower = userPrompt.lowercase(Locale.ROOT)
            val extractedGenres = mutableListOf<String>()
            val extractedKeywords = mutableListOf<String>()
            var extractedMood = ""
            var extractedCategory: String? = null
            var yearFrom: Int? = null
            var yearTo: Int? = null

            // Detect category
            when {
                queryLower.contains("anime") || queryLower.contains("animated") -> {
                    extractedCategory = "Anime"
                    extractedGenres.add("Anime")
                }
                queryLower.contains("series") || queryLower.contains("show") || queryLower.contains("season") || queryLower.contains("tv") -> {
                    extractedCategory = "Series"
                }
                queryLower.contains("movie") || queryLower.contains("film") || queryLower.contains("cinema") -> {
                    extractedCategory = "Movies"
                }
            }

            // Detect genres
            if (queryLower.contains("sci-fi") || queryLower.contains("scifi") || queryLower.contains("science fiction") || queryLower.contains("space") || queryLower.contains("interstellar") || queryLower.contains("alien")) {
                extractedGenres.add("Sci-Fi")
            }
            if (queryLower.contains("thriller") || queryLower.contains("suspense") || queryLower.contains("psychological")) {
                extractedGenres.add("Thriller")
            }
            if (queryLower.contains("action") || queryLower.contains("fight") || queryLower.contains("combat") || queryLower.contains("superhero") || queryLower.contains("batman")) {
                extractedGenres.add("Action")
            }
            if (queryLower.contains("drama") || queryLower.contains("emotional") || queryLower.contains("intense") || queryLower.contains("oppenheimer")) {
                extractedGenres.add("Drama")
            }
            if (queryLower.contains("crime") || queryLower.contains("detective") || queryLower.contains("gangster") || queryLower.contains("mafia") || queryLower.contains("peaky") || queryLower.contains("godfather")) {
                extractedGenres.add("Crime")
            }
            if (queryLower.contains("adventure") || queryLower.contains("journey") || queryLower.contains("quest")) {
                extractedGenres.add("Adventure")
            }
            if (queryLower.contains("fantasy") || queryLower.contains("magic") || queryLower.contains("mythology")) {
                extractedGenres.add("Fantasy")
            }
            if (queryLower.contains("cyberpunk") || queryLower.contains("neon") || queryLower.contains("future") || queryLower.contains("blade runner")) {
                extractedGenres.add("Cyberpunk")
                extractedGenres.add("Sci-Fi")
            }
            if (queryLower.contains("mystery") || queryLower.contains("puzzle") || queryLower.contains("whodunit")) {
                extractedGenres.add("Mystery")
            }

            // Detect mood
            extractedMood = when {
                queryLower.contains("mind-bending") || queryLower.contains("mind bending") || queryLower.contains("inception") || queryLower.contains("matrix") -> "Mind-Bending & Cerebral"
                queryLower.contains("thought-provoking") || queryLower.contains("thought provoking") || queryLower.contains("deep") -> "Thought-Provoking"
                queryLower.contains("dark") || queryLower.contains("gritty") || queryLower.contains("noir") -> "Dark & Atmospheric"
                queryLower.contains("fast") || queryLower.contains("adrenaline") || queryLower.contains("hype") -> "High-Octane"
                queryLower.contains("mystery") || queryLower.contains("psychological") -> "Tense & Mysterious"
                queryLower.contains("tonight") || queryLower.contains("recommend") || queryLower.contains("popular") -> "Critically Acclaimed"
                else -> "Immersive Cinema"
            }

            // Extract Year bounds
            val yearMatch = Regex("""\b(19\d\d|20\d\d)\b""").findAll(userPrompt).map { it.value.toInt() }.toList()
            if (yearMatch.size >= 2) {
                yearFrom = yearMatch.minOrNull()
                yearTo = yearMatch.maxOrNull()
            } else if (yearMatch.size == 1) {
                yearFrom = yearMatch.first()
            }

            // Personalized fallback if query is open-ended ("What should I watch tonight?")
            if (extractedGenres.isEmpty() && userContext != null && userContext.favoriteGenres.isNotEmpty()) {
                extractedGenres.addAll(userContext.favoriteGenres.take(2))
            }

            val explanation = buildString {
                append("Discovered matches for ")
                if (extractedMood.isNotEmpty()) append("$extractedMood ")
                if (extractedGenres.isNotEmpty()) append(extractedGenres.joinToString(", ")) else append("top-rated cinema")
                if (extractedCategory != null) append(" ($extractedCategory)")
                if (yearFrom != null) append(" from $yearFrom")
            }

            Result.success(
                SearchIntentResult(
                    genres = extractedGenres.distinct(),
                    mood = extractedMood,
                    keywords = extractedKeywords,
                    yearFrom = yearFrom,
                    yearTo = yearTo,
                    category = extractedCategory,
                    explanation = explanation
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("AI features are temporarily unavailable."))
        }
    }

    override suspend fun generateRecommendations(
        userPrompt: String,
        catalog: List<Movie>,
        userContext: UserTasteContext?
    ): Result<NoxAiRecommendationResult> = withContext(Dispatchers.Default) {
        try {
            delay(350) // Smooth conversational pacing

            val intentResult = parseSearchIntent(userPrompt, userContext).getOrNull()
                ?: SearchIntentResult(genres = listOf("Sci-Fi", "Action"), mood = "Top Rated")

            // Safe validation against actual NOX Movie Catalog
            val matchedMovies = catalog.filter { movie ->
                val matchesGenre = intentResult.genres.isEmpty() || intentResult.genres.any { g ->
                    movie.genre.contains(g, ignoreCase = true) || movie.tags.any { t -> t.contains(g, ignoreCase = true) }
                }

                val matchesCategory = when (intentResult.category) {
                    "Anime" -> movie.genre.contains("Anime", ignoreCase = true) || movie.title.contains("Attack", ignoreCase = true)
                    "Series" -> movie.isSeries
                    "Movies" -> !movie.isSeries
                    else -> true
                }

                val matchesYear = (intentResult.yearFrom == null || (movie.year.toIntOrNull() ?: 2024) >= intentResult.yearFrom)

                matchesGenre && matchesCategory && matchesYear
            }.ifEmpty {
                // Fallback to top-rated catalog items
                catalog.sortedByDescending { it.rating }.take(5)
            }

            val aiResponse = when {
                userPrompt.contains("interstellar", ignoreCase = true) ->
                    "Based on Interstellar's grand cosmic scale and philosophical depth, here are curated cinematic masterworks featuring epic worldbuilding and stunning visuals."
                userPrompt.contains("psychological", ignoreCase = true) || userPrompt.contains("thriller", ignoreCase = true) ->
                    "Here are gripping thrillers packed with psychological tension, intricate plots, and unexpected revelations."
                userPrompt.contains("tonight", ignoreCase = true) ->
                    "For tonight's feature presentation, here are premier, high-rated titles matching your cinematic preferences."
                else ->
                    "Here are handpicked selections tailored to '${userPrompt.take(40)}', strictly validated against the NOX library."
            }

            Result.success(
                NoxAiRecommendationResult(
                    aiResponseText = aiResponse,
                    recommendedMovies = matchedMovies.take(6),
                    searchCriteria = intentResult,
                    isAvailable = true
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("AI features are temporarily unavailable."))
        }
    }
}
