package com.example.basgaane.data.datasource.remote

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.basgaane.data.datasource.local.LocalPlaylistDataSource
import com.example.basgaane.domain.model.Playlist
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Remote data source managing Firestore real-time synchronization of playlists.
 * Enables friends to see, create, and share playlists across devices.
 * Liked Songs playlists are isolated per-device using Android ID so reinstallation
 * restores the exact liked songs without colliding with or wiping other users' data.
 */
class FirebasePlaylistDataSource(
    private val context: Context
) {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val deviceId: String by lazy { resolveDeviceId() }

    @SuppressLint("HardwareIds")
    private fun resolveDeviceId(): String {
        return try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )?.takeIf { it.isNotBlank() } ?: "default_device"
        } catch (e: Exception) {
            "default_device"
        }
    }

    private suspend fun ensureAuthenticated() = withContext(Dispatchers.IO) {
        try {
            if (auth.currentUser == null) {
                Tasks.await(auth.signInAnonymously())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Auth notice in FirebasePlaylistDataSource: ${e.message}")
        }
    }

    /**
     * Real-time stream of all playlists in Cloud Firestore.
     */
    fun observeRemotePlaylists(): Flow<List<Playlist>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            ensureAuthenticated()

            Log.d(TAG, "Attaching Firestore snapshot listener to '$COLLECTION_PLAYLISTS' (deviceId=$deviceId)...")
            registration = firestore.collection(COLLECTION_PLAYLISTS)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore playlist listen error: ${error.message}", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val deviceLikedDocId = "liked_songs_$deviceId"
                        val hasDeviceLikedDoc = snapshots.documents.any { it.id == deviceLikedDocId }

                        val playlists = snapshots.documents.mapNotNull { doc ->
                            val docId = doc.id

                            // Filter out other devices' liked songs playlists
                            if (docId.startsWith("liked_songs_") && docId != deviceLikedDocId && docId != LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID) {
                                return@mapNotNull null
                            }

                            // If we have a modern device-scoped document, skip the legacy shared document to prevent duplicates
                            if (docId == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID && hasDeviceLikedDoc) {
                                return@mapNotNull null
                            }

                            val rawId = doc.getString("id") ?: docId
                            val isLiked = docId == deviceLikedDocId ||
                                    docId == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID ||
                                    rawId == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID
                            val id = if (isLiked) LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID else rawId

                            val name = doc.getString("name") ?: return@mapNotNull null
                            val description = doc.getString("description") ?: ""
                            @Suppress("UNCHECKED_CAST")
                            val songIds = (doc.get("songIds") as? List<String>) ?: emptyList()
                            val coverUrl = doc.getString("coverUrl")
                            val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                            Playlist(
                                id = id,
                                name = name,
                                description = description,
                                songIds = songIds,
                                coverUrl = coverUrl,
                                createdAt = createdAt
                            )
                        }.sortedByDescending { it.createdAt }

                        trySend(playlists)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firestore playlist listener: ${e.message}", e)
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
            Log.d(TAG, "Firestore playlist listener removed.")
        }
    }

    suspend fun savePlaylistDocument(playlist: Playlist): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated()
            val targetDocId = if (playlist.id == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID) {
                "liked_songs_$deviceId"
            } else {
                playlist.id
            }

            val data = hashMapOf(
                "id" to playlist.id,
                "name" to playlist.name,
                "description" to playlist.description,
                "songIds" to playlist.songIds,
                "coverUrl" to (playlist.coverUrl ?: ""),
                "createdAt" to playlist.createdAt
            )
            Tasks.await(firestore.collection(COLLECTION_PLAYLISTS).document(targetDocId).set(data))
            Log.d(TAG, "Successfully synced playlist '$targetDocId' (${playlist.songIds.size} songs) to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save playlist to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deletePlaylistDocument(playlistId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated()
            val targetDocId = if (playlistId == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID) {
                "liked_songs_$deviceId"
            } else {
                playlistId
            }
            Tasks.await(firestore.collection(COLLECTION_PLAYLISTS).document(targetDocId).delete())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete playlist from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "FirebasePlaylistDS"
        private const val COLLECTION_PLAYLISTS = "playlists"
    }
}
