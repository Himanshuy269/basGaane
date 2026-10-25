# BasGaane Architecture Documentation

This document explains the technical architecture, design patterns, and engineering choices in **BasGaane**.

---

## 1. High-Level Architecture

BasGaane follows the official Android Architecture recommendations using **Model-View-ViewModel (MVVM)**, **Unidirectional Data Flow (UDF)**, and **Repository Pattern**.

```
┌─────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                    │
│   • HomeScreen         • MiniPlayer       • FullPlayerSheet │
│   • AddMusicDialog     • EqualizerBars                      │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow & Events
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                        MusicViewModel                       │
│    Holds UI state, active filter, dialog visibility, and    │
│    bridges repository queries to playback controls.         │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
               ▼                               ▼
┌───────────────────────────────┐   ┌─────────────────────────┐
│        MusicRepository        │   │  MusicPlayerController  │
│  - SharedPreferences JSON     │   │  - Connects to Media3   │
│  - Starter Royalty-Free tracks│   │  - Exposes PlayerState  │
│  - Firebase/Cloud Sync hook   │   │  - 300ms Progress ticker│
└───────────────────────────────┘   └────────────┬────────────┘
                                                 │ MediaController
                                                 ▼
                                    ┌─────────────────────────┐
                                    │  MusicPlaybackService   │
                                    │   (MediaSessionService) │
                                    │    - ExoPlayer instance │
                                    │    - System notification│
                                    │    - Background playback│
                                    └─────────────────────────┘
```

---

## 2. Audio Engine (AndroidX Media3)

Prior versions of Android used `MediaPlayer` or legacy `MediaBrowserServiceCompat`. BasGaane uses **AndroidX Media3**, Google's unified media library.

### Why Media3?
1. **Background Playback**: Music continues playing seamlessly when the user switches apps or locks their screen.
2. **System Notification & Lockscreen**: Media3 automatically manages notifications with action buttons (Play, Pause, Skip, Seek) without needing manual `NotificationCompat.Builder` code.
3. **Audio Focus**: Automatically ducks when navigation apps speak or pauses when phone calls arrive.
4. **Noisy Audio Handling**: Automatically pauses when Bluetooth headphones or wired headphones disconnect.

### Core Components
- **`MusicPlaybackService`**:
  - Extends `androidx.media3.session.MediaSessionService`.
  - Configures `ExoPlayer` with `AudioAttributes.USAGE_MEDIA` and `handleAudioFocus = true`.
  - Exposes `MediaSession` connected to an open-app `PendingIntent`.
- **`MusicPlayerController`**:
  - Connects asynchronously to `MusicPlaybackService` via `SessionToken` and `MediaController`.
  - Listens to playback callbacks (`onIsPlayingChanged`, `onMediaItemTransition`, `onPlaybackStateChanged`).
  - Runs a lightweight coroutine ticker that emits playback position updates every 300ms when playing, giving the UI smooth seekbar progression.

---

## 3. Data & Storage Layer

### `Song` Model
```kotlin
data class Song(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String = "Unknown Artist",
    val audioUrl: String,           // Remote URL or content:// URI
    val coverUrl: String? = null,   // Artwork URL or null
    val durationMs: Long = 0L,      // Length in milliseconds
    val isLocal: Boolean = false,   // Local file vs shared cloud stream
    val addedAt: Long = System.currentTimeMillis()
)
```

### Local Persistence
- `DefaultMusicRepository` stores tracks locally using structured JSON in `SharedPreferences`.
- When a user imports audio via the Android file picker, the content URI is registered and its ID3 tags (duration, title) are extracted via `MediaMetadataRetriever`.

### Cloud Sync Pattern (For Friends)
- The repository is structured to seamlessly plug into Firebase Cloud Storage & Firestore.
- Audio blobs upload to Cloud Storage; metadata documents write to `/tracks`.
- Any phone with the app subscribed to `/tracks` receives new tracks in real time.

---

## 4. UI Layer (Jetpack Compose)

- **Design System**: Neon Dark Aesthetic (`#0C0E14` background with `#8B5CF6` violet and `#06B6D4` cyan).
- **Equalizer Animation**: `EqualizerBars` uses `rememberInfiniteTransition` with staggered easing curves to simulate live frequency bars.
- **Vinyl Rotation**: `FullPlayerSheet` calculates a 360-degree linear rotation when `isPlaying == true`, giving a retro vinyl record feel.
- **Edge-to-Edge**: Android 15/16 compliant using `enableEdgeToEdge()` and Compose inner padding.
