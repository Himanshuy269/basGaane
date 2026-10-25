# BasGaane Clean Architecture Documentation

This document explains the technical architecture, layer boundaries, and engineering choices in **BasGaane**.

---

## 1. Clean Architecture Overview

BasGaane strictly adheres to **Clean Architecture** principles:
- **Domain Independence**: Business logic and use cases do not depend on databases, UI frameworks, or device APIs.
- **Dependency Rule**: Dependencies point inward: `Presentation -> Domain <- Data`.
- **Single Responsibility**: Each usecase, repository, and data source has a single, focused responsibility.

```
┌─────────────────────────────────────────────────────────────┐
│                 PRESENTATION LAYER (UI)                     │
│  • Jetpack Compose Components & Screens                     │
│  • MusicViewModel (consumes domain Use Cases)               │
│  • BasGaaneTheme & Design Tokens                            │
└──────────────────────────────┬──────────────────────────────┘
                               │ Observes & Invokes
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│  • Model: Song entity                                       │
│  • Use Cases:                                               │
│    - GetSongsUseCase           - AddSongUseCase             │
│    - DeleteSongUseCase         - SearchSongsUseCase         │
│  • Repository Contract: MusicRepository                     │
└──────────────────────────────┬──────────────────────────────┘
                               │ Implemented by
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
│  • MusicRepositoryImpl (unifies local & cloud sources)      │
│  • LocalSongDataSource (persistent JSON & starter tracks)   │
│  • FirebaseSongDataSource (Storage upload & Firestore sync) │
└─────────────────────────────────────────────────────────────┘
                               ▲
                               │ Uses
┌──────────────────────────────┴──────────────────────────────┐
│                      CORE LAYER                             │
│  • AndroidX Media3 Playback Engine (ExoPlayer)              │
│  • MusicPlaybackService (MediaSessionService)               │
│  • MusicPlayerController (MediaController connection)       │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Layer Breakdown

### A. Domain Layer (`com.example.basgaane.domain`)
The heart of the app containing the core business rules:
- **`model.Song`**: Domain entity representing a music track.
- **`repository.MusicRepository`**: Interface contract defining operations for fetching, adding, uploading, and deleting tracks.
- **Use Cases**:
  - `GetSongsUseCase`: Exposes a reactive `Flow<List<Song>>` stream.
  - `AddSongUseCase`: Validates titles and coordinates track persistence.
  - `DeleteSongUseCase`: Removes tracks by ID.
  - `SearchSongsUseCase`: Filters song lists by query matching title or artist.

### B. Data Layer (`com.example.basgaane.data`)
Implements domain contracts and coordinates raw storage mechanisms:
- **`datasource.local.LocalSongDataSource`**:
  - Stores local tracks using JSON in `SharedPreferences`.
  - Provides fallback royalty-free demo tracks so users can immediately test playback on fresh installations.
- **`datasource.remote.FirebaseSongDataSource`**:
  - Uploads audio files to Firebase Cloud Storage (`/audio/*.mp3`).
  - Listens in real time to the Cloud Firestore `songs` collection using `addSnapshotListener`.
  - Deletes documents from Firestore when requested.
- **`repository.MusicRepositoryImpl`**:
  - Implements `domain.repository.MusicRepository`.
  - Combines cloud tracks, local tracks, and fallback starter tracks into a single sorted list.
  - Gracefully falls back to local storage if the user is offline or Firebase rules are restricted.

### C. Core Layer (`com.example.basgaane.core`)
Handles low-level Android audio framework interactions:
- **`service.MusicPlaybackService`**:
  - Extends `androidx.media3.session.MediaSessionService`.
  - Configures `ExoPlayer` with `AudioAttributes.USAGE_MEDIA` and noisy audio handling.
  - Publishes media notifications and lock screen playback controls.
- **`player.MusicPlayerController`**:
  - Asynchronously binds to `MusicPlaybackService` via `MediaController`.
  - Exposes `StateFlow<PlayerUiState>` for Jetpack Compose.
  - Emits 300ms position updates for smooth scrubber progression.

### D. Presentation Layer (`com.example.basgaane.presentation`)
Reactive declarative UI built with Jetpack Compose:
- **`viewmodel.MusicViewModel`**:
  - Directly injects and invokes domain use cases (`GetSongsUseCase`, `AddSongUseCase`, `DeleteSongUseCase`, `SearchSongsUseCase`).
  - Never touches database or network code directly.
- **`components`**:
  - `AddMusicDialog`: File picker & track metadata editor.
  - `EqualizerBars`: Animated 3-bar frequency visualizer.
  - `MiniPlayer`: Docked bottom bar with linear progress.
  - `FullPlayerSheet`: Rotating vinyl disc, scrubber slider, shuffle/repeat toggles.
- **`screens.HomeScreen`**:
  - Header branding, live track counter, search bar, and lazy list of songs.
- **`theme`**:
  - Modern cyber/neon dark palette with `#8B5CF6` violet and `#06B6D4` cyan accents.

---

## 3. Data Flow (Unidirectional Data Flow)

1. **User Action**: User selects an audio file in `AddMusicDialog` and clicks "Add Track".
2. **UI -> ViewModel**: `AddMusicDialog` triggers `viewModel.addTrack(title, artist, uri)`.
3. **ViewModel -> Use Case**: `MusicViewModel` invokes `addSongUseCase(...)`.
4. **Use Case -> Repository**: `AddSongUseCase` validates data and calls `MusicRepository.uploadAndAddSong(...)`.
5. **Repository -> Data Sources**:
   - `FirebaseSongDataSource` uploads audio bytes to Firebase Cloud Storage and stores metadata in Firestore.
   - `LocalSongDataSource` caches the song locally.
6. **Data Source -> Repository -> ViewModel -> UI**:
   - Firestore listener emits updated track list.
   - `MusicRepositoryImpl` combines local + remote tracks and emits new `List<Song>`.
   - `MusicViewModel` updates `filteredSongs` `StateFlow`.
   - Jetpack Compose recomposes the track list instantly across all friends' devices.
