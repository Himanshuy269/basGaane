package com.example.basgaane.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.basgaane.core.utils.AudioMetadataHelper
import com.example.basgaane.domain.model.Song
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.PrismMagenta
import kotlin.math.absoluteValue

/**
 * Curated gradient palettes used to procedurally generate a distinctive
 * album cover image for every song that does not have embedded artwork.
 */
private val CoverGradients = listOf(
    listOf(Color(0xFF00F2FE), Color(0xFF4FACFE)),                     // Electric Cyan
    listOf(Color(0xFFFF0844), Color(0xFFFFB199)),                     // Sunset Coral
    listOf(Color(0xFFB224EF), Color(0xFF7579FF)),                     // Cosmic Orchid
    listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),                     // Ocean Sapphire
    listOf(Color(0xFFF857A6), Color(0xFFFF5858)),                     // Neon Rose
    listOf(Color(0xFF0BA360), Color(0xFF3CBA92)),                     // Emerald Prism
    listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)), // Horizon Flare
    listOf(Color(0xFF654EA3), Color(0xFFEAAFC8)),                     // Velvet Lavender
    listOf(Color(0xFF11998E), Color(0xFF38EF7D)),                     // Mint Aura
    listOf(Color(0xFFFA709A), Color(0xFFFEE140))                      // Radiant Solar
)

/**
 * Universal Song Cover Image component.
 *
 * 1. If [Song.coverUrl] is present (from cloud or local cache), renders it via Coil.
 * 2. If [Song.coverUrl] is null but [Song.isLocal] is true, extracts embedded ID3 picture from the audio file.
 * 3. If no image exists in the music file, generates a beautiful procedural Liquid Crystal
 *    album artwork tailored to that track's title & artist so EVERY song has an eye-catching cover.
 * 4. Supports optional equalizer bar overlay when track is playing.
 */
@Composable
fun SongCoverImage(
    song: Song,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentScale: ContentScale = ContentScale.Crop,
    showEqualizerIfPlaying: Boolean = false,
    isPlaying: Boolean = false
) {
    val context = LocalContext.current

    // If local track has no coverUrl, attempt extracting embedded ID3 picture
    val extractedBytes by produceState<ByteArray?>(initialValue = null, key1 = song.id, key2 = song.coverUrl) {
        if (song.coverUrl.isNullOrEmpty() && (song.audioUrl.startsWith("content://") || song.audioUrl.startsWith("file://"))) {
            value = AudioMetadataHelper.extractEmbeddedPicture(context, song.audioUrl)
        }
    }

    val hasExplicitImage = !song.coverUrl.isNullOrEmpty() || extractedBytes != null

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF121624)),
        contentAlignment = Alignment.Center
    ) {
        if (!song.coverUrl.isNullOrEmpty()) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
                contentScale = contentScale
            )
        } else if (extractedBytes != null) {
            AsyncImage(
                model = extractedBytes,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
                contentScale = contentScale
            )
        } else {
            // Generative Liquid Crystal Album Art tailored to this track
            ProceduralSongCover(
                title = song.title,
                artist = song.artist,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay equalizer bars if active
        if (showEqualizerIfPlaying && isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x77000000)),
                contentAlignment = Alignment.Center
            ) {
                EqualizerBars(isPlaying = true, barColor = Color.White)
            }
        }
    }
}

/**
 * Procedurally generates a unique, vibrant liquid crystal album cover for songs without artwork.
 */
@Composable
fun ProceduralSongCover(
    title: String,
    artist: String,
    modifier: Modifier = Modifier
) {
    val seed = (title.trim() + artist.trim()).hashCode().absoluteValue
    val gradientColors = CoverGradients[seed % CoverGradients.size]

    // Monogram initials (e.g. "Starry Neon Night" -> "SN")
    val words = title.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    val monogram = when {
        words.size >= 2 -> "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
        words.size == 1 && words[0].length >= 2 -> words[0].take(2).uppercase()
        words.size == 1 -> words[0].take(1).uppercase()
        else -> "♫"
    }

    Box(
        modifier = modifier
            .background(Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        // Concentric vinyl groove effect
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .clip(CircleShape)
                .border(1.dp, Color(0x33FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.65f)
                    .clip(CircleShape)
                    .border(1.dp, Color(0x22FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Frosted glass center badge with song monogram
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.70f)
                        .clip(CircleShape)
                        .background(Color(0x4D000000))
                        .border(1.5.dp, Color(0x66FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monogram,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
