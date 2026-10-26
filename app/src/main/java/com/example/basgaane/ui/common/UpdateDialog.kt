package com.example.basgaane.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.basgaane.core.update.AppUpdateInfo
import com.example.basgaane.core.update.UpdateDownloadState
import com.example.basgaane.ui.theme.LocalBasGaaneColors
import com.example.basgaane.ui.theme.PrismBlue
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.PrismEmerald
import com.example.basgaane.ui.theme.PrismMagenta
import com.example.basgaane.ui.theme.PrismViolet
import com.example.basgaane.ui.theme.TextPrimary
import com.example.basgaane.ui.theme.TextSecondary
import com.example.basgaane.ui.theme.TextTertiary

@Composable
fun UpdateDialog(
    updateInfo: AppUpdateInfo?,
    currentVersionName: String,
    downloadState: UpdateDownloadState,
    onStartDownload: () -> Unit,
    onInstall: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (updateInfo == null) return

    val colors = LocalBasGaaneColors.current
    val isDismissible = !updateInfo.isForceUpdate && downloadState !is UpdateDownloadState.Downloading

    Dialog(
        onDismissRequest = {
            if (isDismissible) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = isDismissible,
            dismissOnClickOutside = isDismissible
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .crystalGlassCard(shape = RoundedCornerShape(26.dp), isSelected = true)
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with glowing rocket badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        PrismCyan.copy(alpha = 0.25f),
                                        PrismViolet.copy(alpha = 0.25f),
                                        PrismMagenta.copy(alpha = 0.25f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(listOf(PrismCyan, PrismMagenta)),
                                shape = RoundedCornerShape(15.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Update Available",
                            tint = PrismCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (updateInfo.releaseTitle.isNotBlank()) updateInfo.releaseTitle else "New Update Available!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "A fresh version of BasGaane is ready",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Version Badge Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current version pill
                    Box(
                        modifier = Modifier
                            .crystalPill(shape = RoundedCornerShape(10.dp), isActive = false)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "v$currentVersionName",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PrismCyan,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // New version pill
                    Box(
                        modifier = Modifier
                            .crystalPill(shape = RoundedCornerShape(10.dp), isActive = true)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "v${updateInfo.latestVersionName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    if (updateInfo.apkSize > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        val mb = String.format("%.1f", updateInfo.apkSize / (1024f * 1024f))
                        Text(
                            text = "$mb MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Release Notes Card
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Text(
                        text = "What's New:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.glassCard.copy(alpha = 0.6f))
                            .border(
                                width = 1.dp,
                                brush = colors.borderSubtleBrush,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = updateInfo.releaseNotes,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Download Progress / Action States
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!updateInfo.isForceUpdate) {
                                Box(
                                    modifier = Modifier
                                        .crystalPill(
                                            shape = RoundedCornerShape(14.dp),
                                            isActive = false,
                                            onClick = onDismiss
                                        )
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Later",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .crystalPill(
                                        shape = RoundedCornerShape(14.dp),
                                        isActive = true,
                                        onClick = onStartDownload
                                    )
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Update Now",
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    is UpdateDownloadState.Downloading -> {
                        val pct = (downloadState.progress * 100).toInt().coerceIn(0, 100)
                        val dlMb = String.format("%.1f", downloadState.downloadedBytes / (1024f * 1024f))
                        val totMb = if (downloadState.totalBytes > 0) {
                            String.format("%.1f", downloadState.totalBytes / (1024f * 1024f))
                        } else {
                            "--"
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloading update...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = PrismCyan
                                )
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Liquid Shimmer Progress Bar
                            val shimmerTransition = rememberInfiniteTransition(label = "update_shimmer")
                            val shimmerOffset by shimmerTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "shimmer_offset"
                            )

                            val trackBg = colors.glassCard
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .drawBehind {
                                        // Background track
                                        drawRoundRect(
                                            color = trackBg,
                                            cornerRadius = CornerRadius(4.dp.toPx())
                                        )

                                        val fillWidth = size.width * downloadState.progress.coerceIn(0.02f, 1f)
                                        if (fillWidth > 0f) {
                                            val grad = Brush.linearGradient(
                                                colors = listOf(PrismCyan, PrismBlue, PrismViolet, PrismMagenta),
                                                start = Offset(shimmerOffset * fillWidth, 0f),
                                                end = Offset((shimmerOffset + 1f) * fillWidth, 0f)
                                            )
                                            drawRoundRect(
                                                brush = grad,
                                                size = Size(fillWidth, size.height),
                                                cornerRadius = CornerRadius(4.dp.toPx())
                                            )
                                        }
                                    }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "$dlMb MB / $totMb MB",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrismEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Download complete! Ready to install.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .crystalPill(
                                        shape = RoundedCornerShape(14.dp),
                                        isActive = true,
                                        onClick = { onInstall(downloadState.apkPath) }
                                    )
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Install & Restart",
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    is UpdateDownloadState.Error -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Update error: ${downloadState.message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .crystalPill(
                                            shape = RoundedCornerShape(14.dp),
                                            isActive = false,
                                            onClick = onDismiss
                                        )
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Cancel",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Box(
                                    modifier = Modifier
                                        .crystalPill(
                                            shape = RoundedCornerShape(14.dp),
                                            isActive = true,
                                            onClick = onStartDownload
                                        )
                                        .padding(horizontal = 18.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Retry",
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
