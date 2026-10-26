package com.example.basgaane.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.basgaane.ui.theme.InputFieldContainerFocused
import com.example.basgaane.ui.theme.InputFieldContainerUnfocused
import com.example.basgaane.ui.theme.LocalBasGaaneColors
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.PrismEmerald
import com.example.basgaane.ui.theme.PrismMagenta
import com.example.basgaane.ui.theme.PrismViolet
import com.example.basgaane.ui.theme.SurfaceDarkVariant
import com.example.basgaane.ui.theme.TextPrimary
import com.example.basgaane.ui.theme.TextSecondary
import com.example.basgaane.ui.theme.TextTertiary
import com.example.basgaane.viewmodel.ThemeMode

@Composable
fun EditProfileDialog(
    isOpen: Boolean,
    currentName: String,
    currentThemeMode: ThemeMode,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit,
    appVersion: String = "1.0.1",
    isCheckingForUpdates: Boolean = false,
    updateFeedback: String? = null,
    onCheckForUpdates: () -> Unit = {}
) {
    if (!isOpen) return

    var name by remember { mutableStateOf(currentName) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentName) {
        name = currentName
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .crystalGlassCard(shape = RoundedCornerShape(24.dp), isSelected = true)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
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
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrismCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Profile & Appearance",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Customize your identity & theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Display Name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = PrismCyan
                        )
                    },
                    placeholder = { Text("e.g. Himanshu, Rahul, Priya") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrismCyan,
                        unfocusedBorderColor = SurfaceDarkVariant,
                        focusedLabelColor = PrismCyan,
                        unfocusedLabelColor = TextTertiary,
                        focusedContainerColor = InputFieldContainerFocused,
                        unfocusedContainerColor = InputFieldContainerUnfocused
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Theme Mode Selector
                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        Triple(ThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto),
                        Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                        Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode)
                    )

                    themeOptions.forEach { (mode, label, icon) ->
                        val isSelected = currentThemeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .crystalPill(
                                    shape = RoundedCornerShape(14.dp),
                                    isActive = isSelected,
                                    onClick = { onSelectThemeMode(mode) }
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) PrismCyan else TextTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // App Version & In-App Update Section
                Text(
                    text = "App Updates",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val colors = LocalBasGaaneColors.current
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.glassCard.copy(alpha = 0.5f))
                        .border(
                            width = 1.dp,
                            brush = colors.borderSubtleBrush,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PrismCyan.copy(alpha = 0.15f))
                                        .border(
                                            width = 1.dp,
                                            color = PrismCyan.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = PrismCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "BasGaane v$appVersion",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Free GitHub Cloud Updates",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                }
                            }

                            // Check Updates Pill Button
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
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
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

                        if (!updateFeedback.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val isLatest = updateFeedback.contains("latest", ignoreCase = true)
                            Text(
                                text = updateFeedback,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isLatest) PrismEmerald else PrismCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            text = "Cancel",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .crystalPill(
                                shape = RoundedCornerShape(14.dp),
                                isActive = true,
                                onClick = {
                                    val clean = name.trim()
                                    if (clean.isEmpty()) {
                                        error = "Please enter your name"
                                    } else {
                                        onSaveName(clean)
                                    }
                                }
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = TextPrimary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
