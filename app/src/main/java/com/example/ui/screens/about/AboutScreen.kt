package com.example.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = NoxTheme.colors
    val scrollState = rememberScrollState()

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
                    text = "About NOX",
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
            // Brand Banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NoxDimensions.radiusHero))
                    .background(colors.card)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NOX",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A clean media experience designed for discovery and playback.",
                    style = NoxTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .background(colors.cardElevated, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Version 1.0 (Production)",
                        color = colors.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Watchmode API Attribution & Integration
            Text(
                text = "Streaming Availability & Discovery",
                style = NoxTheme.typography.titleMedium,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(NoxDimensions.radiusMedium),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Tv,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Powered by Watchmode",
                            style = NoxTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Movie and TV streaming availability data, metadata, provider links, and regional catalog information are provided by the official Watchmode API (api.watchmode.com).",
                        style = NoxTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Data Retention & Terms Compliance: Non-image title metadata and source records are cached locally on-device with a maximum 30-day retention period in strict compliance with the Watchmode API terms of service.",
                        style = NoxTheme.typography.bodySmall,
                        color = colors.mutedText,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Media Rights & Compliance
            Text(
                text = "Media Rights & Compliance",
                style = NoxTheme.typography.titleMedium,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(NoxDimensions.radiusMedium),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "NOX is built exclusively for authorized media streams, open public-domain content, and locally authorized files. Watchmode is used strictly to determine legal streaming availability and redirect users to official provider platforms. The NOX player never extracts, scrapes, or circumvents copyright protections.",
                    style = NoxTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy & Data Storage
            Text(
                text = "Privacy & Local Data",
                style = NoxTheme.typography.titleMedium,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(NoxDimensions.radiusMedium),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your watch history, saved favorites, playback progress, and app customizations are stored strictly on your device. NOX operates with zero ad-trackers, zero remote telemetry, and zero data profiling.",
                    style = NoxTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Open Source Acknowledgments
            Text(
                text = "Open Source Frameworks",
                style = NoxTheme.typography.titleMedium,
                color = colors.accent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(NoxDimensions.radiusMedium),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("• Android Jetpack Compose & Material 3", color = colors.text, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Android Media3 / ExoPlayer", color = colors.text, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Square Retrofit & Moshi", color = colors.text, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Coil Image Loader for Compose", color = colors.text, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Kotlin Coroutines & Reactive Flows", color = colors.text, fontSize = 13.sp)
                }
            }
        }
    }
}
