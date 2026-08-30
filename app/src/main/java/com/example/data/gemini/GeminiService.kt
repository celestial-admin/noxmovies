package com.example.data.gemini

import com.example.model.Movie
import com.example.model.NoxAiRecommendationResult
import com.example.model.SearchIntentResult
import com.example.model.UserTasteContext

/**
 * Interface defining secure natural language query parsing and AI discovery.
 * The Android client does NOT store or access raw private API keys directly.
 */
interface GeminiService {
    suspend fun parseSearchIntent(
        userPrompt: String,
        userContext: UserTasteContext?
    ): Result<SearchIntentResult>

    suspend fun generateRecommendations(
        userPrompt: String,
        catalog: List<Movie>,
        userContext: UserTasteContext?
    ): Result<NoxAiRecommendationResult>
}
