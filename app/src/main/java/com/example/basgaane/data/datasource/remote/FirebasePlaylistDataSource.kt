package com.example.basgaane.data.datasource.remote

import android.content.Context
import android.util.Log
import com.example.basgaane.domain.model.Playlist
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Remote data source managing Firestore real-time synchronization of playlists.
 * Enables friends to see, create, and share playlists across devices.
 */
class FirebasePlaylistDataSource(
    private val context: Context
) {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

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

            Log.d(TAG, "Attaching Firestore snapshot listener to '$COLLECTION_PLAYLISTS'...")
            registration = firestore.collection(COLLECTION_PLAYLISTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore playlist listen error: ${error.message}", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val playlists = snapshots.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
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
                        }
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
            val data = hashMapOf(
                "id" to playlist.id,
                "name" to playlist.name,
                "description" to playlist.description,
                "songIds" to playlist.songIds,
                "coverUrl" to (playlist.coverUrl ?: ""),
                "createdAt" to playlist.createdAt
            )
            Tasks.await(firestore.collection(COLLECTION_PLAYLISTS).document(playlist.id).set(data))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save playlist to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deletePlaylistDocument(playlistId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated()
            Tasks.await(firestore.collection(COLLECTION_PLAYLISTS).document(playlistId).delete())
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
