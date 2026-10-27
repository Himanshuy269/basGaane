package com.example.basgaane.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basgaane.ui.theme.NoctraBorder
import com.example.basgaane.ui.theme.NoctraDark
import com.example.basgaane.ui.theme.NoctraLime
import com.example.basgaane.viewmodel.HomeTab

/**
 * Bottom Navigation Bar matching the exact 4-tab layout and lime styling from the Noctra design:
 * Library -> Discover -> Playlists -> Search
 */
@Composable
fun AppBottomNavigationBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Ordered strictly to match the screenshots: Library, Discover, Playlists, Search
    val navigationItems = remember {
        listOf(
            HomeTab.HOME to "Library",
            HomeTab.ONLINE to "Discover",
            HomeTab.PLAYLISTS to "Playlists",
            HomeTab.SEARCH to "Search"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NoctraDark)
            .drawBehind {
                // Top hairline border
                drawLine(
                    color = NoctraBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationItems.forEach { (tab, title) ->
                val isSelected = selectedTab == tab

                val tintColor by animateColorAsState(
                    targetValue = if (isSelected) NoctraLime else Color(0xFF636B7C),
                    animationSpec = tween(durationMillis = 180),
                    label = "TabTint"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (tab) {
                            HomeTab.HOME -> {
                                // 3 vertical/slanted library bars matching the screenshot
                                Canvas(modifier = Modifier.size(19.dp)) {
                                    val barWidth = 2.5.dp.toPx()
                                    val r = CornerRadius(1.5f, 1.5f)
                                    // Bar 1 (vertical)
                                    drawRoundRect(
                                        color = tintColor,
                                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                        size = Size(barWidth, size.height - 4.dp.toPx()),
                                        cornerRadius = r
                                    )
                                    // Bar 2 (vertical)
                                    drawRoundRect(
                                        color = tintColor,
                                        topLeft = Offset(8.dp.toPx(), 4.dp.toPx()),
                                        size = Size(barWidth, size.height - 6.dp.toPx()),
                                        cornerRadius = r
                                    )
                                    // Bar 3 (slanted/standing)
                                    drawRoundRect(
                                        color = tintColor,
                                        topLeft = Offset(14.dp.toPx(), 2.dp.toPx()),
                                        size = Size(barWidth, size.height - 4.dp.toPx()),
                                        cornerRadius = r
                                    )
                                }
                            }
                            HomeTab.ONLINE -> {
                                // Radio/signal broadcast concentric waves
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val strokeWidth = 2.dp.toPx()
                                    // Center dot
                                    drawCircle(
                                        color = tintColor,
                                        radius = 2.2.dp.toPx(),
                                        center = center
                                    )
                                    // Inner ring arcs
                                    drawArc(
                                        color = tintColor,
                                        startAngle = 135f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth),
                                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                                        size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx())
                                    )
                                    drawArc(
                                        color = tintColor,
                                        startAngle = -45f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth),
                                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                                        size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx())
                                    )
                                    // Outer ring arcs
                                    drawArc(
                                        color = tintColor,
                                        startAngle = 135f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth),
                                        topLeft = Offset(0.5.dp.toPx(), 0.5.dp.toPx()),
                                        size = Size(size.width - 1.dp.toPx(), size.height - 1.dp.toPx())
                                    )
                                    drawArc(
                                        color = tintColor,
                                        startAngle = -45f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth),
                                        topLeft = Offset(0.5.dp.toPx(), 0.5.dp.toPx()),
                                        size = Size(size.width - 1.dp.toPx(), size.height - 1.dp.toPx())
                                    )
                                }
                            }
                            HomeTab.PLAYLISTS -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = title,
                                    tint = tintColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            HomeTab.SEARCH -> {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = title,
                                    tint = tintColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = tintColor
                    )
                }
            }
        }
    }
}
