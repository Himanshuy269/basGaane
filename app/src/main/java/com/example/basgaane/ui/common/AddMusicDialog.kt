package com.example.basgaane.ui.common

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.PrismMagenta
import com.example.basgaane.ui.theme.PrismViolet
import com.example.basgaane.ui.theme.SurfaceDarkVariant
import com.example.basgaane.ui.theme.TextPrimary
import com.example.basgaane.ui.theme.TextSecondary
import com.example.basgaane.ui.theme.TextTertiary

@Composable
fun AddMusicDialog(
    isOpen: Boolean,
    isUploading: Boolean,
    currentUserName: String = "Friend",
    onDismiss: () -> Unit,
    onAddTrack: (title: String, artist: String, audioUri: Uri, addedBy: String) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var addedBy by remember { mutableStateOf(currentUserName) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserName) {
        if (addedBy.isBlank() || addedBy == "Friend") {
            addedBy = currentUserName
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            validationError = null
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        val name = cursor.getString(nameIndex)
                        fileName = name
                        if (title.isBlank()) {
                            title = name.substringBeforeLast(".")
                        }
                    }
                }
            } catch (e: Exception) {
                fileName = "Selected Audio Track"
            }
        }
    }

    Dialog(onDismissRequest = { if (!isUploading) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .crystalGlassCard(shape = RoundedCornerShape(24.dp), isSelected = true)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = PrismCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Add Music",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Upload and share with all friends",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // File Picker Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x400C101C))
                        .border(
                            width = 1.dp,
                            brush = if (selectedUri != null) {
                                Brush.linearGradient(listOf(PrismCyan, PrismViolet))
                            } else {
                                Brush.linearGradient(listOf(PrismCyan.copy(alpha = 0.3f), PrismViolet.copy(alpha = 0.2f)))
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { filePickerLauncher.launch("audio/*") }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Pick file",
                                tint = PrismCyan,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap to choose audio file (MP3, WAV, M4A)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = TextPrimary
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = PrismCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fileName ?: "Audio File Selected",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Ready to upload & sync",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrismCyan
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Song Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        validationError = null
                    },
                    label = { Text("Song Title") },
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null, tint = PrismCyan)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrismCyan,
                        unfocusedBorderColor = SurfaceDarkVariant,
                        focusedLabelColor = PrismCyan,
                        unfocusedLabelColor = TextTertiary,
                        focusedContainerColor = Color(0x600C101C),
                        unfocusedContainerColor = Color(0x400C101C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Artist Input
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = PrismViolet)
                    },
                    placeholder = { Text("e.g. Arijit Singh, Diljit, The Weeknd") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrismCyan,
                        unfocusedBorderColor = SurfaceDarkVariant,
                        focusedLabelColor = PrismCyan,
                        unfocusedLabelColor = TextTertiary,
                        focusedContainerColor = Color(0x600C101C),
                        unfocusedContainerColor = Color(0x400C101C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // "Added By" / Your Name Input
                OutlinedTextField(
                    value = addedBy,
                    onValueChange = { addedBy = it },
                    label = { Text("Your Name (Added By)") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrismMagenta)
                    },
                    placeholder = { Text("e.g. Himanshu, Rahul") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrismMagenta,
                        unfocusedBorderColor = SurfaceDarkVariant,
                        focusedLabelColor = PrismMagenta,
                        unfocusedLabelColor = TextTertiary,
                        focusedContainerColor = Color(0x600C101C),
                        unfocusedContainerColor = Color(0x400C101C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (isUploading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .crystalGlassCard(shape = RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PrismCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Uploading to cloud & syncing with friends...",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrismCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
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
                                onClick = { if (!isUploading) onDismiss() }
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
                                    if (isUploading) return@crystalPill
                                    val uri = selectedUri
                                    if (uri == null) {
                                        validationError = "Please select an audio file first."
                                        return@crystalPill
                                    }
                                    if (title.isBlank()) {
                                        validationError = "Please provide a song title."
                                        return@crystalPill
                                    }
                                    onAddTrack(
                                        title.trim(),
                                        artist.trim().ifEmpty { "Unknown Artist" },
                                        uri,
                                        addedBy.trim().ifEmpty { currentUserName }
                                    )
                                }
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        if (isUploading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Uploading...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Track",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
