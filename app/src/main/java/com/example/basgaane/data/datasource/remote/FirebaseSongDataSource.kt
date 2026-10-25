package com.example.basgaane.data.datasource.remote

import android.net.Uri
import com.example.basgaane.domain.model.Song
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Remote data source managing Firebase Cloud Storage audio uploads
 * and Firestore real-time synchronization.
 */
class FirebaseSongDataSource {

    fun observeRemoteSongs(): Flow<List<Song>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            val firestore = FirebaseFirestore.getInstance()
            registration = firestore.collection(COLLECTION_SONGS)
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val songs = snapshots.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: return@mapNotNull null
                            val artist = doc.getString("artist") ?: "Unknown Artist"
                            val audioUrl = doc.getString("audioUrl") ?: return@mapNotNull null
                            val coverUrl = doc.getString("coverUrl")
                            val durationMs = doc.getLong("durationMs") ?: 0L
                            val addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()

                            Song(
                                id = id,
                                title = title,
                                artist = artist,
                                audioUrl = audioUrl,
                                coverUrl = coverUrl,
                                durationMs = durationMs,
                                isLocal = false,
                                addedAt = addedAt
                            )
                        }
                        trySend(songs)
                    }
                }
        } catch (e: Exception) {
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    suspend fun uploadAudio(audioUri: Uri, songId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileRef = storageRef.child("audio").child("audio_${songId}.mp3")
            Tasks.await(fileRef.putFile(audioUri))
            val downloadUrl = Tasks.await(fileRef.downloadUrl).toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSongDocument(song: Song): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val songData = hashMapOf(
                "id" to song.id,
                "title" to song.title,
                "artist" to song.artist,
                "audioUrl" to song.audioUrl,
                "coverUrl" to (song.coverUrl ?: ""),
                "durationMs" to song.durationMs,
                "isLocal" to false,
                "addedAt" to song.addedAt
            )
            Tasks.await(firestore.collection(COLLECTION_SONGS).document(song.id).set(songData))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSongDocument(songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            Tasks.await(firestore.collection(COLLECTION_SONGS).document(songId).delete())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val COLLECTION_SONGS = "songs"
    }
}
