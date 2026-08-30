package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.AccentColor
import com.example.model.ThemeMode

object NoxTheme {
    val colors: NoxColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalNoxColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val dimensions: NoxDimensions
        get() = NoxDimensions
}

@Composable
fun NoxTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentColor: AccentColor = AccentColor.NOX_LIME,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.AMOLED -> true
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.DYNAMIC -> isSystemDark
        ThemeMode.SYSTEM -> isSystemDark
    }

    val context = LocalContext.current
    val accent = getAccentColor(accentColor, isLightMode = !isDark)

    val palette: NoxColorPalette = when {
        themeMode == ThemeMode.AMOLED -> NoxColorPalette(
            background = AmoledBackground,
            surface = AmoledSurface,
            card = AmoledCard,
            cardElevated = AmoledCardElevated,
            text = AmoledText,
            secondaryText = AmoledSecondaryText,
            mutedText = AmoledMutedText,
            accent = accent,
            isDark = true
        )
        themeMode == ThemeMode.LIGHT -> NoxColorPalette(
            background = LightBackground,
            surface = LightSurface,
            card = LightCard,
            cardElevated = LightCardElevated,
            text = LightText,
            secondaryText = LightSecondaryText,
            mutedText = LightMutedText,
            accent = accent,
            isDark = false
        )
        themeMode == ThemeMode.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamicColors = if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isSystemDark) {
                NoxColorPalette(
                    background = dynamicColors.background,
                    surface = dynamicColors.surface,
                    card = dynamicColors.surfaceVariant,
                    cardElevated = dynamicColors.surfaceContainerHigh,
                    text = dynamicColors.onBackground,
                    secondaryText = dynamicColors.onSurfaceVariant,
                    mutedText = dynamicColors.outline,
                    accent = dynamicColors.primary,
                    isDark = true
                )
            } else {
                NoxColorPalette(
                    background = dynamicColors.background,
                    surface = dynamicColors.surface,
                    card = dynamicColors.surfaceVariant,
                    cardElevated = dynamicColors.surfaceContainerHigh,
                    text = dynamicColors.onBackground,
                    secondaryText = dynamicColors.onSurfaceVariant,
                    mutedText = dynamicColors.outline,
                    accent = dynamicColors.primary,
                    isDark = false
                )
            }
        }
        else -> NoxColorPalette( // DARK or SYSTEM (when dark)
            background = if (isDark) DarkBackground else LightBackground,
            surface = if (isDark) DarkSurface else LightSurface,
            card = if (isDark) DarkCard else LightCard,
            cardElevated = if (isDark) DarkCardElevated else LightCardElevated,
            text = if (isDark) DarkText else LightText,
            secondaryText = if (isDark) DarkSecondaryText else LightSecondaryText,
            mutedText = if (isDark) DarkMutedText else LightMutedText,
            accent = accent,
            isDark = isDark
        )
    }

    val materialColorScheme: ColorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color.Black,
            primaryContainer = palette.cardElevated,
            onPrimaryContainer = palette.text,
            secondary = palette.accent,
            onSecondary = Color.Black,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.card,
            onSurfaceVariant = palette.secondaryText
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            primaryContainer = palette.cardElevated,
            onPrimaryContainer = palette.text,
            secondary = palette.accent,
            onSecondary = Color.White,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.card,
            onSurfaceVariant = palette.secondaryText
        )
    }

    CompositionLocalProvider(LocalNoxColors provides palette) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = NoxTypography,
            shapes = NoxShapes,
            content = content
        )
    }
}

// Backward compatibility helper
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NoxTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        accentColor = AccentColor.NOX_LIME,
        content = content
    )
}
