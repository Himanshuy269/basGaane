package com.example.basgaane.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.basgaane.presentation.theme.AccentGradientEnd
import com.example.basgaane.presentation.theme.AccentGradientStart
import com.example.basgaane.presentation.theme.NeonCyan
import com.example.basgaane.presentation.theme.NeonPurple
import com.example.basgaane.presentation.theme.SurfaceDark
import com.example.basgaane.presentation.theme.SurfaceDarkVariant
import com.example.basgaane.presentation.theme.TextPrimary
import com.example.basgaane.presentation.theme.TextSecondary
import com.example.basgaane.presentation.theme.TextTertiary

@Composable
fun CreatePlaylistDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String) -> Unit
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(AccentGradientStart, AccentGradientEnd)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Create Playlist",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Text(
                    text = "Organize music matching your taste & mood",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (isError) isError = false
                    },
                    label = { Text("Playlist Name *") },
                    placeholder = { Text("e.g. Chill Vibez, Party Hits", color = TextTertiary) },
                    singleLine = true,
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Please enter a playlist name", color = Color(0xFFF43F5E)) }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDarkVariant,
                        unfocusedContainerColor = SurfaceDarkVariant,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g. Late night road trip", color = TextTertiary) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDarkVariant,
                        unfocusedContainerColor = SurfaceDarkVariant,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = Color.Transparent,
                        focusedLabelColor = NeonPurple,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                isError = true
                            } else {
                                onCreate(name.trim(), description.trim())
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPurple,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Create", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
