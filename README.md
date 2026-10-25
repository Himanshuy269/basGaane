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

BasGaane follows modern Android Architecture Components (MVVM) and Unidirectional Data Flow (UDF):

```
                   ┌──────────────────────────────────────────┐
                   │               Jetpack Compose UI         │
                   │ (HomeScreen, MiniPlayer, FullPlayerSheet)│
                   └────────────────────┬─────────────────────┘
                                        │ Events & StateFlow
                                        ▼
                   ┌──────────────────────────────────────────┐
                   │             MusicViewModel               │
                   └──────────┬────────────────────┬──────────┘
                              │                    │
              Tracks State    ▼                    ▼ Playback Commands
     ┌───────────────────────────────┐   ┌──────────────────────────────┐
     │       MusicRepository         │   │    MusicPlayerController     │
     │  (Local Store + Cloud Sync)   │   └──────────────┬───────────────┘
     └───────────────────────────────┘                  │ MediaController
                                                        ▼
                                         ┌──────────────────────────────┐
                                         │    MusicPlaybackService      │
                                         │   (AndroidX Media3 + Exo)    │
                                         └──────────────────────────────┘
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
./gradlew assembleDebug
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

## 📂 Project Structure

```
BasGaane/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/basgaane/
│   │   │   ├── data/
│   │   │   │   └── MusicRepository.kt       # Persistent storage & cloud sync abstraction
│   │   │   ├── model/
│   │   │   │   └── Song.kt                  # Audio track data model
│   │   │   ├── player/
│   │   │   │   └── MusicPlayerController.kt # Media3 MediaController wrapper
│   │   │   ├── service/
│   │   │   │   └── MusicPlaybackService.kt  # AndroidX MediaSessionService
│   │   │   ├── ui/
│   │   │   │   ├── components/
│   │   │   │   │   ├── AddMusicDialog.kt    # "Add Music" file picker & form
│   │   │   │   │   ├── EqualizerBars.kt     # Animated visualizer bars
│   │   │   │   │   ├── FullPlayerSheet.kt   # Full-screen vinyl player
│   │   │   │   │   └── MiniPlayer.kt        # Persistent bottom player bar
│   │   │   │   ├── screens/
│   │   │   │   │   └── HomeScreen.kt        # Main music catalog & search
│   │   │   │   ├── theme/                   # Neon/Dark cyber music theme
│   │   │   │   └── MusicViewModel.kt        # MVVM StateFlow coordinator
│   │   │   └── MainActivity.kt              # App entry point & permission flow
│   │   └── AndroidManifest.xml              # Foreground service & permissions
├── docs/
│   ├── ARCHITECTURE.md                      # Audio engine & reactive state docs
│   └── FIREBASE_SETUP.md                    # Cloud sync guide for friends
└── README.md                                # Project documentation
```
