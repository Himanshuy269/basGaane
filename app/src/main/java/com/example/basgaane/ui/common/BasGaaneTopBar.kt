package com.example.basgaane.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basgaane.ui.theme.NoctraLime

/**
 * Global Top Header matching the dark minimalist & lime aesthetic.
 * Displays the audio waveform bars logo icon, the "bacGaane" brand title,
 * and the circular user avatar profile trigger.
 */
@Composable
fun BasGaaneTopBar(
    userName: String = "User",
    isUpdateAvailable: Boolean = false,
    onOpenEditProfileDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand: Waveform Logo + bacGaane
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 5 Audio Waveform Bars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.height(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(11.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NoctraLime)
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NoctraLime)
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(13.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NoctraLime)
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NoctraLime)
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NoctraLime)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "bacGaane",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        }

        // User Avatar Button
        val initials = rememberUserInitials(userName)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF222733))
                .clickable { onOpenEditProfileDialog() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Green notification badge if app update is ready
            if (isUpdateAvailable) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(NoctraLime)
                )
            }
        }
    }
}

private fun rememberUserInitials(name: String): String {
    val clean = name.trim()
    if (clean.isBlank()) return "JD"
    val parts = clean.split(Regex("\\s+"))
    return if (parts.size >= 2) {
        "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
    } else {
        clean.take(2).uppercase()
    }
}
