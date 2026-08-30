package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.AccentColor

// Default Fallbacks
val NoxBrandAccent = Color(0xFFE9FF55)

// AMOLED Theme Colors
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF090909)
val AmoledCard = Color(0xFF111111)
val AmoledCardElevated = Color(0xFF181818)
val AmoledText = Color(0xFFFFFFFF)
val AmoledSecondaryText = Color(0xFFA0A0A0)
val AmoledMutedText = Color(0xFF555555)

// Dark Theme Colors
val DarkBackground = Color(0xFF080808)
val DarkSurface = Color(0xFF101010)
val DarkCard = Color(0xFF161616)
val DarkCardElevated = Color(0xFF202020)
val DarkText = Color(0xFFFFFFFF)
val DarkSecondaryText = Color(0xFFA6A6A6)
val DarkMutedText = Color(0xFF666666)

// Light Theme Colors
val LightBackground = Color(0xFFF7F7F7)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFEEEEEE)
val LightCardElevated = Color(0xFFE2E2E2)
val LightText = Color(0xFF101010)
val LightSecondaryText = Color(0xFF666666)
val LightMutedText = Color(0xFF9E9E9E)
val LightDefaultAccent = Color(0xFF667A00)

// Backward compatibility references
val NoxPrimaryBackground = DarkBackground
val NoxSecondaryBackground = DarkSurface
val NoxCardBackground = DarkCard
val NoxElevatedCard = DarkCardElevated
val NoxPrimaryText = DarkText
val NoxSecondaryText = DarkSecondaryText
val NoxMutedText = DarkMutedText

data class NoxColorPalette(
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardElevated: Color,
    val text: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val accent: Color,
    val isDark: Boolean = true,
    val border: Color = if (isDark) Color(0xFF242424) else Color(0xFFE2E2E2)
)

val LocalNoxColors = staticCompositionLocalOf {
    NoxColorPalette(
        background = DarkBackground,
        surface = DarkSurface,
        card = DarkCard,
        cardElevated = DarkCardElevated,
        text = DarkText,
        secondaryText = DarkSecondaryText,
        mutedText = DarkMutedText,
        accent = NoxBrandAccent,
        border = Color(0xFF242424),
        isDark = true
    )
}

fun getAccentColor(accent: AccentColor, isLightMode: Boolean = false): Color {
    return if (isLightMode && accent == AccentColor.NOX_LIME) {
        LightDefaultAccent
    } else {
        Color(accent.hexCode)
    }
}
