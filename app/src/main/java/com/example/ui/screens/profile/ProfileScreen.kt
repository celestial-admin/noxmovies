package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.NoxPreferencesManager
import com.example.data.auth.AuthRepository
import com.example.ui.screens.auth.GoogleGLogo
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme

@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    preferencesManager: NoxPreferencesManager,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val colors = NoxTheme.colors
    val scrollState = rememberScrollState()
    val currentUser by authRepository.currentUser.collectAsState()
    val syncLibraryEnabled by preferencesManager.syncLibraryEnabled.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }

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
                    text = "NOX",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = colors.accent
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = colors.text
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (!currentUser.isGuest) {
                // SIGNED-IN GOOGLE USER VIEW
                // [ Google Profile Photo ]
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(colors.card)
                        .border(2.dp, colors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentUser.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Google Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = currentUser.displayName.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Display Name
                Text(
                    text = currentUser.displayName,
                    style = NoxTheme.typography.titleLarge,
                    color = colors.text,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Email
                Text(
                    text = currentUser.email,
                    style = NoxTheme.typography.bodyMedium,
                    color = colors.secondaryText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sync status indicator pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.card)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (syncLibraryEnabled) colors.accent else colors.mutedText, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (syncLibraryEnabled) "Cloud Library Synced" else "Sync Paused",
                        color = colors.text,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // GUEST MODE VIEW
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colors.card),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PersonOutline,
                        contentDescription = "Guest",
                        tint = colors.accent,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Guest Mode",
                    style = NoxTheme.typography.titleLarge,
                    color = colors.text,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Sign in to sync your library across devices.",
                    style = NoxTheme.typography.bodyMedium,
                    color = colors.secondaryText
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sign in with Google Button
                Button(
                    onClick = onNavigateToSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(NoxDimensions.radiusButton),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoogleGLogo(modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DIVIDER 1
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.cardElevated,
                thickness = 1.dp
            )

            // SECTION 1: LIBRARY (My List, History, Downloads)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileLibraryTile(
                    title = "My List",
                    icon = Icons.Default.Favorite,
                    onClick = onNavigateToFavorites,
                    modifier = Modifier.weight(1f)
                )
                ProfileLibraryTile(
                    title = "History",
                    icon = Icons.Default.History,
                    onClick = onNavigateToHistory,
                    modifier = Modifier.weight(1f)
                )
                ProfileLibraryTile(
                    title = "Downloads",
                    icon = Icons.Default.Download,
                    onClick = onNavigateToDownloads,
                    modifier = Modifier.weight(1f)
                )
            }

            // DIVIDER 2
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.cardElevated,
                thickness = 1.dp
            )

            // SECTION 2: SETTINGS & ABOUT
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sync Library Toggle (if signed in)
                if (!currentUser.isGuest) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
                            .background(colors.card)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Sync Library",
                                    style = NoxTheme.typography.titleMedium,
                                    color = colors.text
                                )
                                Text(
                                    text = "Favorites, history, & playback state",
                                    style = NoxTheme.typography.labelMedium,
                                    color = colors.secondaryText
                                )
                            }
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

                ProfileNavRow(
                    title = "Settings",
                    subtitle = "Themes, accents, playback quality, audio",
                    icon = Icons.Default.Settings,
                    onClick = onNavigateToSettings
                )

                ProfileNavRow(
                    title = "Notifications",
                    subtitle = "Release alerts, series updates",
                    icon = Icons.Default.Notifications,
                    onClick = onNavigateToNotifications
                )

                ProfileNavRow(
                    title = "About",
                    subtitle = "Version 1.0, authorized media rights & privacy",
                    icon = Icons.Default.Info,
                    onClick = onNavigateToAbout
                )
            }

            // DIVIDER 3
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colors.cardElevated,
                thickness = 1.dp
            )

            // SECTION 3: SIGN OUT or SIGN IN
            if (!currentUser.isGuest) {
                Button(
                    onClick = { showSignOutDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.card),
                    shape = RoundedCornerShape(NoxDimensions.radiusButton),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign Out",
                        color = Color(0xFFFF5252),
                        style = NoxTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = onNavigateToSignIn,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(NoxDimensions.radiusButton),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Sign in to NOX",
                        color = Color.Black,
                        style = NoxTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = colors.cardElevated,
            title = {
                Text(
                    text = "Sign Out of NOX",
                    style = NoxTheme.typography.titleLarge,
                    color = colors.text
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out? You will switch back to Guest mode.",
                    style = NoxTheme.typography.bodyMedium,
                    color = colors.secondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authRepository.signOut()
                        showSignOutDialog = false
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = colors.secondaryText)
                }
            }
        )
    }
}

@Composable
fun ProfileLibraryTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NoxTheme.colors

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = NoxTheme.typography.titleSmall,
            color = colors.text,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProfileNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = NoxTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = NoxTheme.typography.titleMedium,
                color = colors.text
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = NoxTheme.typography.labelMedium,
                color = colors.secondaryText
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = colors.mutedText,
            modifier = Modifier.size(14.dp)
        )
    }
}
