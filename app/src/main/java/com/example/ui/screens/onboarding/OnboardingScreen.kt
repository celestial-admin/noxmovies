package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme

data class OnboardingPageData(
    val title: String,
    val headline: String,
    val description: String
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val colors = NoxTheme.colors

    val pages = listOf(
        OnboardingPageData(
            title = "NOX",
            headline = "DISCOVER",
            description = "Find movies and series in one clean, cinematic interface designed for discovery."
        ),
        OnboardingPageData(
            title = "NOX",
            headline = "WATCH",
            description = "Enjoy a smooth playback experience with your authorized media and custom player controls."
        ),
        OnboardingPageData(
            title = "NOX",
            headline = "SAVE",
            description = "Keep favorites, history, and supported offline media organized effortlessly."
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp)
    ) {
        // Top NOX Brand
        Text(
            text = "NOX",
            style = NoxTheme.typography.titleLarge,
            color = colors.text,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 24.dp)
        )

        // Center Content
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.align(Alignment.Center),
            label = "onboarding_content"
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text(
                    text = page.headline,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = colors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = page.description,
                    style = NoxTheme.typography.bodyLarge,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier.height(NoxDimensions.minTouchTarget)
            ) {
                Text(
                    text = "Skip",
                    color = colors.secondaryText,
                    style = NoxTheme.typography.labelMedium
                )
            }

            // Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentPage) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentPage) colors.accent else colors.cardElevated
                            )
                    )
                }
            }

            if (currentPage < pages.size - 1) {
                Button(
                    onClick = { currentPage++ },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(NoxDimensions.radiusButton),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = "Next",
                        color = Color.Black,
                        style = NoxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(NoxDimensions.radiusButton),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = "Get Started",
                        color = Color.Black,
                        style = NoxTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
