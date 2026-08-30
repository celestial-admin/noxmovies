package com.example.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieRepository
import com.example.model.NotificationItem
import com.example.ui.components.NoxEmptyState
import com.example.ui.theme.NoxDimensions
import com.example.ui.theme.NoxTheme

@Composable
fun NotificationCenterScreen(
    repository: MovieRepository,
    onBack: () -> Unit
) {
    val colors = NoxTheme.colors
    val notifications by repository.notifications.collectAsState()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.text
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifications",
                        style = NoxTheme.typography.titleLarge,
                        color = colors.text
                    )
                }

                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { repository.clearNotifications() }) {
                        Text(
                            text = "Clear All",
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            NoxEmptyState(
                icon = Icons.Default.Notifications,
                title = "No notifications",
                subtitle = "You are all caught up on new releases and downloads.",
                actionLabel = "Back to Home",
                onActionClick = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationRow(
                        item = item,
                        onClick = { repository.markNotificationAsRead(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val colors = NoxTheme.colors

    val icon: ImageVector = when (item.category) {
        "New Releases" -> Icons.Default.Movie
        "Downloads" -> Icons.Default.DownloadDone
        else -> Icons.Default.SystemUpdate
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NoxDimensions.radiusMedium))
            .background(if (item.isRead) colors.card else colors.cardElevated)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(colors.card, CircleShape),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = NoxTheme.typography.titleMedium,
                    color = colors.text,
                    fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold
                )
                Text(
                    text = item.timestamp,
                    color = colors.mutedText,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                style = NoxTheme.typography.bodyMedium,
                color = colors.secondaryText,
                lineHeight = 18.sp
            )
        }
    }
}
