package com.example.ui.screens.stubs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.theme.NoxPrimaryBackground
import com.example.ui.theme.NoxSecondaryText
import com.example.ui.theme.Typography

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NoxPrimaryBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title coming soon", color = NoxSecondaryText, style = Typography.bodyLarge)
    }
}
