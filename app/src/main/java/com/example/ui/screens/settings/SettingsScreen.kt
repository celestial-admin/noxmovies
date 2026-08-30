package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieRepository
import com.example.data.NoxPreferencesManager
import com.example.data.auth.AuthRepository
import com.example.model.AVAILABLE_REGIONS
import com.example.model.AccentColor
import com.example.model.PlaybackQuality
import com.example.model.SubtitleLanguage
import com.example.model.ThemeMode
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.LightBackground
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme
import com.example.ui.theme.getAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: NoxPreferencesManager,
    repository: MovieRepository,
    authRepository: AuthRepository,
    onNavigateToSignIn: () -> Unit,
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val colors = NoxTheme.colors
    val scrollState = rememberScrollState()

    val currentUser by authRepository.currentUser.collectAsState()
    val syncLibraryEnabled by preferencesManager.syncLibraryEnabled.collectAsState()
    val currentTheme by preferencesManager.themeMode.collectAsState()
    val currentAccent by preferencesManager.accentColor.collectAsState()
    val streamingRegion by preferencesManager.streamingRegion.collectAsState()
    val reduceMotion by preferencesManager.reduceMotion.collectAsState()
    val haptics by preferencesManager.hapticFeedback.collectAsState()
    val wifiOnly by preferencesManager.wifiOnlyDownloads.collectAsState()
    val autoplay by preferencesManager.autoplay.collectAsState()
    val autoDeleteWatched by preferencesManager.autoDeleteWatched.collectAsState()
    val defaultQuality by preferencesManager.defaultQuality.collectAsState()
    val defaultSubtitle by preferencesManager.defaultSubtitle.collectAsState()

    var showRegionDialog by remember { mutableStateOf(false) }
    var cacheClearedMessage by remember { mutableStateOf(false) }

    val currentRegionObj = AVAILABLE_REGIONS.find { it.code.equals(streamingRegion, ignoreCase = true) } ?: AVAILABLE_REGIONS.first()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.text
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    style = NoxTheme.typography.titleLarge,
                    color = colors.text
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 90.dp)
        ) {
            // SECTION 0: ACCOUNT & SYNC
            Text(
                text = "Account",
                style = NoxTheme.typography.titleLarge,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
                    .background(colors.card)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.cardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (currentUser.isGuest) "Guest Account" else currentUser.displayName,
                                style = NoxTheme.typography.titleMedium,
                                color = colors.text,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentUser.isGuest) "Local offline session" else currentUser.email,
                                style = NoxTheme.typography.labelMedium,
                                color = colors.secondaryText
                            )
                        }
                    }

                    if (currentUser.isGuest) {
                        Button(
                            onClick = onNavigateToSignIn,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Sign In", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(onClick = { authRepository.signOut() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(color = colors.cardElevated, thickness = 1.dp)

                Spacer(modifier = Modifier.height(12.dp))

                // Sync Library Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sync Library", style = NoxTheme.typography.titleSmall, color = colors.text)
                        Text(
                            "Sync favorites, history, and theme settings across devices",
                            style = NoxTheme.typography.labelSmall,
                            color = colors.secondaryText
                        )
                    }
                    Switch(
                        checked = syncLibraryEnabled,
                        onCheckedChange = { preferencesManager.setSyncLibrary(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.mutedText,
                            uncheckedTrackColor = colors.cardElevated
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION: STREAMING & REGION
            Text(
                text = "Streaming & Availability",
                style = NoxTheme.typography.titleLarge,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
                    .background(colors.card)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRegionDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Public,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Streaming Region",
                                style = NoxTheme.typography.titleMedium,
                                color = colors.text,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Show providers for ${currentRegionObj.name} (${currentRegionObj.code})",
                                style = NoxTheme.typography.labelMedium,
                                color = colors.secondaryText
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(colors.accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentRegionObj.code,
                            color = colors.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Availability data is provided by Watchmode for platforms including Netflix, Prime Video, Disney+ Hotstar, Apple TV, YouTube, JioCinema, Zee5, and SonyLIV.",
                    style = NoxTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION 1: APPEARANCE
            Text(
                text = "Appearance",
                style = NoxTheme.typography.titleLarge,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Theme Selector
            Text(
                text = "Theme",
                style = NoxTheme.typography.titleMedium,
                color = colors.text
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemePreviewCard(
                    title = "AMOLED",
                    bgSample = AmoledBackground,
                    cardSample = Color(0xFF111111),
                    isSelected = currentTheme == ThemeMode.AMOLED,
                    onClick = { preferencesManager.setThemeMode(ThemeMode.AMOLED) },
                    modifier = Modifier.weight(1f)
                )
                ThemePreviewCard(
                    title = "DARK",
                    bgSample = DarkBackground,
                    cardSample = Color(0xFF161616),
                    isSelected = currentTheme == ThemeMode.DARK,
                    onClick = { preferencesManager.setThemeMode(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )
                ThemePreviewCard(
                    title = "LIGHT",
                    bgSample = LightBackground,
                    cardSample = Color(0xFFEEEEEE),
                    isSelected = currentTheme == ThemeMode.LIGHT,
                    onClick = { preferencesManager.setThemeMode(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Visual Accent Color Picker
            Text(
                text = "Accent Color",
                style = NoxTheme.typography.titleMedium,
                color = colors.text
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AccentColor.values()) { accent ->
                    val isSelected = (currentAccent == accent)
                    val accentColorSample = getAccentColor(accent, isLightMode = false)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { preferencesManager.setAccentColor(accent) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(accentColorSample)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) colors.text else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = accent.displayName.split(" ").first(),
                            fontSize = 10.sp,
                            color = if (isSelected) colors.accent else colors.secondaryText,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Appearance Toggles
            SettingsSwitchRow(
                title = "Reduce Motion",
                subtitle = "Disable decorative transition animations",
                checked = reduceMotion,
                onCheckedChange = { preferencesManager.setReduceMotion(it) }
            )

            SettingsSwitchRow(
                title = "Haptic Feedback",
                subtitle = "Vibrate on button and favorite interactions",
                checked = haptics,
                onCheckedChange = { preferencesManager.setHapticFeedback(it) }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION 2: PLAYBACK
            Text(
                text = "Playback",
                style = NoxTheme.typography.titleLarge,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingsSwitchRow(
                title = "Autoplay Next in Queue",
                subtitle = "Automatically start next title when current ends",
                checked = autoplay,
                onCheckedChange = { preferencesManager.setAutoplay(it) }
            )

            // Default Quality Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Default Quality", style = NoxTheme.typography.titleMedium, color = colors.text)
                    Text("Selected video stream resolution", style = NoxTheme.typography.labelMedium, color = colors.secondaryText)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PlaybackQuality.AUTO, PlaybackQuality.P1080, PlaybackQuality.P720).forEach { q ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (defaultQuality == q) colors.accent else colors.card)
                                .clickable { preferencesManager.setDefaultQuality(q) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = q.label,
                                color = if (defaultQuality == q) Color.Black else colors.secondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION 3: DOWNLOADS
            Text(
                text = "Downloads & Offline",
                style = NoxTheme.typography.titleLarge,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            SettingsSwitchRow(
                title = "Wi-Fi Only Downloads",
                subtitle = "Prevent downloads on cellular mobile network",
                checked = wifiOnly,
                onCheckedChange = { preferencesManager.setWifiOnlyDownloads(it) }
            )

            SettingsSwitchRow(
                title = "Auto-delete Watched Media",
                subtitle = "Free space after reaching 100% completion",
                checked = autoDeleteWatched,
                onCheckedChange = { preferencesManager.setAutoDeleteWatched(it) }
            )

            Button(
                onClick = { repository.clearAllDownloads() },
                colors = ButtonDefaults.buttonColors(containerColor = colors.card),
                shape = RoundedCornerShape(NoxDimensions.radiusButton),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = colors.accent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Downloaded Media", color = colors.text, style = NoxTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION 4: STORAGE & CACHE
            Text(
                text = "Storage & Cache",
                style = NoxTheme.typography.titleLarge,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
                    .background(colors.card)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Metadata & Discovery Cache", style = NoxTheme.typography.titleMedium, color = colors.text)
                    Text("30-day retention • Compliant with API terms", style = NoxTheme.typography.labelMedium, color = colors.secondaryText)
                }
                Button(
                    onClick = {
                        repository.cache?.clearAll()
                        cacheClearedMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardElevated),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (cacheClearedMessage) "Cleared!" else "Clear Cache", color = colors.accent, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SECTION 5: ABOUT & LEGAL
            Text(
                text = "About NOX",
                style = NoxTheme.typography.titleLarge,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onNavigateToAbout,
                colors = ButtonDefaults.buttonColors(containerColor = colors.card),
                shape = RoundedCornerShape(NoxDimensions.radiusButton),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "NOX Version 1.0 • Watchmode Attribution & Terms",
                    color = colors.text,
                    style = NoxTheme.typography.titleSmall
                )
            }
        }
    }

    // Streaming Region Selection Dialog
    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            containerColor = colors.cardElevated,
            title = {
                Text(
                    text = "Select Streaming Region",
                    style = NoxTheme.typography.titleLarge,
                    color = colors.text
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AVAILABLE_REGIONS.forEach { region ->
                        val isSelected = region.code.equals(streamingRegion, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.card else Color.Transparent)
                                .clickable {
                                    preferencesManager.setStreamingRegion(region.code)
                                    showRegionDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${region.flag}  ${region.name}",
                                style = NoxTheme.typography.bodyLarge,
                                color = if (isSelected) colors.accent else colors.text,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRegionDialog = false }) {
                    Text("Close", color = colors.accent)
                }
            }
        )
    }
}

@Composable
fun ThemePreviewCard(
    title: String,
    bgSample: Color,
    cardSample: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) colors.accent else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgSample)
                .padding(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(cardSample)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.accent else colors.text
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = NoxTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = NoxTheme.typography.titleMedium, color = colors.text)
            Text(text = subtitle, style = NoxTheme.typography.labelMedium, color = colors.secondaryText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.mutedText,
                uncheckedTrackColor = colors.card
            )
        )
    }
}
