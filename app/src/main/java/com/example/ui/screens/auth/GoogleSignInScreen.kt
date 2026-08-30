package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthUiState
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme
import kotlinx.coroutines.launch

@Composable
fun GoogleSignInScreen(
    authRepository: AuthRepository,
    onSignInSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val colors = NoxTheme.colors
    val scope = rememberCoroutineScope()
    val authState by authRepository.authState.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is AuthUiState.Success -> {
                isLoading = false
                onSignInSuccess()
            }
            is AuthUiState.Error -> {
                isLoading = false
                errorMessage = (authState as AuthUiState.Error).message
            }
            is AuthUiState.Idle -> {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Ambient background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accent.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.25f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / NOX Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                // NOX Geometric Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.card)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NOX",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accent,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome to NOX",
                    style = NoxTheme.typography.displaySmall,
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Sign in to sync your library across devices and unlock personalized discovery.",
                    style = NoxTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Benefit cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SyncBenefitItem(
                    icon = Icons.Default.CloudSync,
                    title = "Cloud Library Sync",
                    subtitle = "Sync favorites, watch history, and progress anywhere"
                )
                SyncBenefitItem(
                    icon = Icons.Default.Psychology,
                    title = "Ask NOX AI Discovery",
                    subtitle = "Tailored recommendations powered by your taste"
                )
                SyncBenefitItem(
                    icon = Icons.Default.Favorite,
                    title = "Custom Collections",
                    subtitle = "Organize movies and shows in personal lists"
                )
            }

            // Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Error Alert
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Surface(
                            color = Color(0x33EF4444),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = Color(0xFFFF6B6B),
                                style = NoxTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }
                    }
                }

                // Official Google Sign-In Button
                Button(
                    onClick = {
                        scope.launch {
                            authRepository.signInWithGoogle()
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(NoxDimensions.radiusButton),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleGLogo(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Continue as Guest Button
                TextButton(
                    onClick = {
                        authRepository.continueAsGuest()
                        onContinueAsGuest()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continue as Guest",
                        style = NoxTheme.typography.titleMedium,
                        color = colors.secondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Guest users can browse and watch demo content offline.",
                    style = NoxTheme.typography.labelSmall,
                    color = colors.mutedText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SyncBenefitItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val colors = NoxTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.cardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = NoxTheme.typography.titleSmall,
                color = colors.text,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = NoxTheme.typography.labelMedium,
                color = colors.secondaryText
            )
        }
    }
}

@Composable
fun GoogleGLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = (w / 2f) * 0.9f

        // Draw Google Quad-Color G Shape simplified vector
        drawArc(
            color = Color(0xFFEA4335), // Red
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true,
            size = size
        )
        drawArc(
            color = Color(0xFF4285F4), // Blue
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true,
            size = size
        )
        drawArc(
            color = Color(0xFF34A853), // Green
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            size = size
        )
        drawArc(
            color = Color(0xFFFBBC05), // Yellow
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            size = size
        )

        // Inner circle cutout
        drawCircle(
            color = Color.White,
            radius = radius * 0.55f,
            center = Offset(cx, cy)
        )

        // Center right bar
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(cx, cy - h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.45f, h * 0.3f)
        )
    }
}
