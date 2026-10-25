# BasGaane 🎵

A sleek, cool, and modern Android music player built with **Kotlin** and **Jetpack Compose**. BasGaane allows you to play high-fidelity audio, import local tracks directly from your device, and is designed to sync music across devices so friends installing the app can listen to the exact same tracks.

---

## ✨ Features

- **Cool & Minimalist Dark UI**: Crafted with modern Material 3, cyber-neon accents (Violet `#8B5CF6` & Cyan `#06B6D4`), and edge-to-edge support.
- **AndroidX Media3 Audio Engine**:
  - Background audio playback using `MediaSessionService`.
  - Notification controls with live playback progress, Play/Pause, Next, and Previous.
  - Automatic audio focus management (pauses during phone calls, ducks on alerts).
- **Add Music Section**:
  - One-tap audio file picker supporting MP3, WAV, M4A, and AAC files.
  - Automatic metadata extraction (title, artist, and duration from ID3 tags).
  - Ability to customize song title and artist before adding.
- **Cross-Device Ready (Shared Music for Friends)**:
  - Repository architecture designed for instant cloud sync via Firebase Cloud Storage & Firestore.
  - Preloaded royalty-free starter tracks for immediate out-of-the-box listening.
- **Animated Audio Visualizer**: Dynamic equalizer bars that dance to active music playback.
- **Mini-Player & Full-Screen Vinyl Player**:
  - Persistent bottom mini-player with linear progress and fast controls.
  - Expandable full-screen player with rotating vinyl art disc, interactive scrubber slider, shuffle, and repeat modes (Off, All, One).
- **Instant Search**: Real-time filtering by song title or artist name.

---

## 🏗️ Architecture Overview

BasGaane follows **Clean Architecture** with strict layer decoupling and Unidirectional Data Flow (UDF):

```
┌─────────────────────────────────────────────────────────────┐
│                 PRESENTATION LAYER (UI)                     │
│  • HomeScreen         • MiniPlayer       • FullPlayerSheet  │
│  • AddMusicDialog     • EqualizerBars    • MusicViewModel   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Observes & Invokes
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│  • Model: Song                                              │
│  • Use Cases:                                               │
│    - GetSongsUseCase           - AddSongUseCase             │
│    - DeleteSongUseCase         - SearchSongsUseCase         │
│  • Repository Interface: MusicRepository                    │
└──────────────────────────────┬──────────────────────────────┘
                               │ Implemented by
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
│  • Repository Implementation: MusicRepositoryImpl           │
│  • Local Data Source: LocalSongDataSource (Storage + Demos) │
│  • Remote Data Source: FirebaseSongDataSource (Cloud Sync)  │
└─────────────────────────────────────────────────────────────┘
                               ▲
                               │ Uses
┌──────────────────────────────┴──────────────────────────────┐
│                      CORE LAYER                             │
│  • Audio Engine: AndroidX Media3 (ExoPlayer)                │
│  • Background Service: MusicPlaybackService                 │
│  • Controller: MusicPlayerController                        │
└─────────────────────────────────────────────────────────────┘
```

For complete architectural details, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17 or JDK 21 (bundled in Android Studio under `jbr`)
- Android device or emulator running Android 7.0+ (API 24+)

### Building the Project
From the project root:
```bash
./gradlew assembleDebug --no-configuration-cache
```

To install on a connected device/emulator:
```bash
./gradlew installDebug
```

---

## ☁️ Cross-Device Music Sharing Setup

To enable friends to upload and automatically share music across devices:
1. Open the [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md) guide.
2. Follow the 3-step setup to connect Firebase Storage and Firestore.
3. Once configured, every song added in the "Add Music" section will instantly sync to all your friends' devices!

---

## 📂 Project Structure (Clean Architecture)

```
BasGaane/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/basgaane/
│   │   │   ├── domain/                          # Business logic & contracts
│   │   │   │   ├── model/
│   │   │   │   │   └── Song.kt                  # Domain track entity
│   │   │   │   ├── repository/
│   │   │   │   │   └── MusicRepository.kt       # Repository contract
│   │   │   │   └── usecase/
│   │   │   │       ├── GetSongsUseCase.kt       # Stream all songs
│   │   │   │       ├── AddSongUseCase.kt        # Upload & add song
│   │   │   │       ├── DeleteSongUseCase.kt     # Remove song
│   │   │   │       └── SearchSongsUseCase.kt    # Filter tracks
│   │   │   ├── data/                            # Data implementations
│   │   │   │   ├── datasource/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   └── LocalSongDataSource.kt
│   │   │   │   │   └── remote/
│   │   │   │   │       └── FirebaseSongDataSource.kt
│   │   │   │   └── repository/
│   │   │   │       └── MusicRepositoryImpl.kt   # Unified repository
│   │   │   ├── core/                            # Media3 system engine
│   │   │   │   ├── player/
│   │   │   │   │   └── MusicPlayerController.kt # Player controller
│   │   │   │   └── service/
│   │   │   │       └── MusicPlaybackService.kt  # MediaSessionService
│   │   │   ├── presentation/                    # Jetpack Compose UI
│   │   │   │   ├── components/
│   │   │   │   │   ├── AddMusicDialog.kt        # File picker dialog
│   │   │   │   │   ├── EqualizerBars.kt         # Animated visualizer
│   │   │   │   │   ├── FullPlayerSheet.kt       # Vinyl player modal
│   │   │   │   │   └── MiniPlayer.kt            # Bottom bar
│   │   │   │   ├── screens/
│   │   │   │   │   └── HomeScreen.kt            # Catalog screen
│   │   │   │   ├── theme/                       # Cyber/Neon dark styling
│   │   │   │   └── viewmodel/
│   │   │   │       └── MusicViewModel.kt        # UI State coordinator
│   │   │   └── MainActivity.kt                  # Entry point & permissions
│   │   └── AndroidManifest.xml                  # Foreground service config
├── docs/
│   ├── ARCHITECTURE.md                          # Clean Architecture deep dive
│   └── FIREBASE_SETUP.md                        # Cloud sync guide for friends
└── README.md                                    # Project documentation
```
