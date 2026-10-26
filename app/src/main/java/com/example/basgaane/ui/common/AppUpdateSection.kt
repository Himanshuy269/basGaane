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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

/**
 * Dedicated, reusable UI section for App Updates.
 * - If update is available: displays new version info and an option to update the app.
 * - If not available: explicitly displays "App is up to date" with check option.
 */
@Composable
fun AppUpdateSection(
    currentVersionName: String,
    updateInfo: AppUpdateInfo?,
    isCheckingForUpdates: Boolean,
    downloadState: UpdateDownloadState,
    onCheckForUpdates: () -> Unit,
    onStartDownload: () -> Unit,
    onInstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalBasGaaneColors.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrismCyan.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = PrismCyan.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "App Updates",
                        tint = PrismCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "App Update",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Current: v$currentVersionName",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            // Quick Check Button
            Box(
                modifier = Modifier
                    .crystalPill(
                        shape = RoundedCornerShape(12.dp),
                        isActive = false,
                        onClick = {
                            if (!isCheckingForUpdates) {
                                onCheckForUpdates()
                            }
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCheckingForUpdates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = PrismCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Checking...",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check for updates",
                            tint = PrismCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Check",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.glassCard.copy(alpha = 0.6f))
                .border(
                    width = 1.dp,
                    brush = if (updateInfo != null) colors.borderActiveBrush else colors.borderSubtleBrush,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            when {
                // STATE 1: Checking in progress
                isCheckingForUpdates -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = PrismCyan
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Checking for new updates on GitHub...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // STATE 2: Update Available! Option to update the app
                updateInfo != null -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(PrismCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Update Available: v${updateInfo.latestVersionName}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = PrismCyan
                                )
                            }

                            if (updateInfo.apkSize > 0) {
                                val mb = String.format("%.1f", updateInfo.apkSize / (1024f * 1024f))
                                Text(
                                    text = "$mb MB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                            }
                        }

                        if (updateInfo.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = updateInfo.releaseNotes,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                                color = TextSecondary,
                                maxLines = 3
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Download State & Action Button
                        when (downloadState) {
                            is UpdateDownloadState.Idle -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .crystalPill(
                                            shape = RoundedCornerShape(12.dp),
                                            isActive = true,
                                            onClick = onStartDownload
                                        )
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SystemUpdate,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Update App (v${updateInfo.latestVersionName})",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            is UpdateDownloadState.Downloading -> {
                                val pct = (downloadState.progress * 100).toInt().coerceIn(0, 100)
                                val dlMb = String.format("%.1f", downloadState.downloadedBytes / (1024f * 1024f))
                                val totMb = if (downloadState.totalBytes > 0) {
                                    String.format("%.1f", downloadState.totalBytes / (1024f * 1024f))
                                } else "--"

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Downloading update...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrismCyan
                                        )
                                        Text(
                                            text = "$pct% ($dlMb / $totMb MB)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val shimmerTransition = rememberInfiniteTransition(label = "section_shimmer")
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
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .drawBehind {
                                                drawRoundRect(color = trackBg, cornerRadius = CornerRadius(3.dp.toPx()))
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
                                                        cornerRadius = CornerRadius(3.dp.toPx())
                                                    )
                                                }
                                            }
                                    )
                                }
                            }

                            is UpdateDownloadState.ReadyToInstall -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .crystalPill(
                                            shape = RoundedCornerShape(12.dp),
                                            isActive = true,
                                            onClick = { onInstall(downloadState.apkPath) }
                                        )
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = PrismEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Install & Restart (v${updateInfo.latestVersionName})",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            is UpdateDownloadState.Error -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = downloadState.message,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .crystalPill(
                                                shape = RoundedCornerShape(10.dp),
                                                isActive = true,
                                                onClick = onStartDownload
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Retry",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // STATE 3: No update available -> print "app is up to date"
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PrismEmerald.copy(alpha = 0.15f))
                                .border(
                                    width = 1.dp,
                                    color = PrismEmerald.copy(alpha = 0.4f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Up to date",
                                tint = PrismEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "App is up to date",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = PrismEmerald
                            )
                            Text(
                                text = "You have the latest version of BasGaane installed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Full Modal Dialog displaying the separate App Update section.
 */
@Composable
fun AppUpdateSectionDialog(
    isOpen: Boolean,
    currentVersionName: String,
    updateInfo: AppUpdateInfo?,
    isCheckingForUpdates: Boolean,
    downloadState: UpdateDownloadState,
    onCheckForUpdates: () -> Unit,
    onStartDownload: () -> Unit,
    onInstall: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .crystalGlassCard(shape = RoundedCornerShape(24.dp), isSelected = true)
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "App Updates",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Render the Update Section
                AppUpdateSection(
                    currentVersionName = currentVersionName,
                    updateInfo = updateInfo,
                    isCheckingForUpdates = isCheckingForUpdates,
                    downloadState = downloadState,
                    onCheckForUpdates = onCheckForUpdates,
                    onStartDownload = onStartDownload,
                    onInstall = onInstall
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Close Button
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .crystalPill(
                            shape = RoundedCornerShape(12.dp),
                            isActive = false,
                            onClick = onDismiss
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
