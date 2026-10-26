package com.example.basgaane.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.SurfaceDark
import com.example.basgaane.ui.theme.TextSecondary
import com.example.basgaane.ui.theme.TextTertiary
import com.example.basgaane.viewmodel.HomeTab

/**
 * Spotify-style floating Liquid Crystal Bottom Navigation Bar.
 * Houses primary destinations: Library, Online, Search, and Playlists.
 */
@Composable
fun AppBottomNavigationBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        SurfaceDark.copy(alpha = 0.88f),
                        SurfaceDark.copy(alpha = 0.98f)
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .crystalGlassCard(shape = RoundedCornerShape(22.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val (title, icon) = when (tab) {
                    HomeTab.HOME -> "Library" to Icons.Default.Headphones
                    HomeTab.ONLINE -> "Online" to Icons.Default.Language
                    HomeTab.SEARCH -> "Search" to Icons.Default.Search
                    HomeTab.PLAYLISTS -> "Playlists" to Icons.AutoMirrored.Filled.PlaylistAdd
                }

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) PrismCyan else TextTertiary,
                    animationSpec = tween(durationMillis = 200),
                    label = "BottomNavIconTint"
                )

                val textTint by animateColorAsState(
                    targetValue = if (isSelected) PrismCyan else TextSecondary,
                    animationSpec = tween(durationMillis = 200),
                    label = "BottomNavTextTint"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .clip(CircleShape)
                                        .background(PrismCyan.copy(alpha = 0.16f))
                                        .border(1.dp, PrismCyan.copy(alpha = 0.35f), CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = textTint
                    )
                }
            }
        }
    }
}
