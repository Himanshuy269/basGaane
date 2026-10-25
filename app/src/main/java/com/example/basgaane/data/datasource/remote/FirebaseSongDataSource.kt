package com.example.basgaane.data.datasource.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.basgaane.domain.model.Song
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Remote data source managing Firebase Cloud Storage audio uploads
 * and Firestore real-time synchronization between devices.
 */
class FirebaseSongDataSource(
    private val context: Context
) {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    /**
     * Ensures the device is signed into Firebase anonymously so that
     * both this phone and friends' phones have valid credentials.
     */
    suspend fun ensureAuthenticated(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (auth.currentUser == null) {
                Log.d(TAG, "Signing in anonymously to Firebase...")
                Tasks.await(auth.signInAnonymously())
                Log.d(TAG, "Signed in anonymously as UID: ${auth.currentUser?.uid}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Anonymous auth notice (proceeding): ${e.localizedMessage}")
            // Proceed anyway, in case Firestore/Storage has open rules
            Result.success(Unit)
        }
    }

    /**
     * Real-time stream of all songs in Cloud Firestore.
     * Whenever any friend adds a song, this callback emits the updated list immediately.
     */
    fun observeRemoteSongs(): Flow<List<Song>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            ensureAuthenticated()

            Log.d(TAG, "Attaching Firestore snapshot listener to '$COLLECTION_SONGS'...")
            registration = firestore.collection(COLLECTION_SONGS)
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore listen error: ${error.message}. Check Firestore security rules!", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        Log.d(TAG, "Received ${snapshots.documents.size} remote songs from Firestore.")
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
            Log.e(TAG, "Failed to initialize Firestore listener: ${e.message}", e)
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
            Log.d(TAG, "Firestore snapshot listener removed.")
        }
    }

    /**
     * Uploads the picked audio file to Firebase Cloud Storage and returns the public streaming URL.
     */
    suspend fun uploadAudio(audioUri: Uri, songId: String): Result<String> = withContext(Dispatchers.IO) {
        var stream: InputStream? = null
        try {
            ensureAuthenticated()

            val storageRef = storage.reference
            val fileName = "track_${songId}.mp3"
            val fileRef = storageRef.child("audio").child(fileName)

            Log.d(TAG, "Opening audio stream for upload: $audioUri")
            stream = context.contentResolver.openInputStream(audioUri)
                ?: throw IllegalArgumentException("Could not open audio file stream.")

            val mimeType = context.contentResolver.getType(audioUri) ?: "audio/mpeg"
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                .build()

            Log.d(TAG, "Uploading audio to Firebase Storage: audio/$fileName (mime: $mimeType)...")
            Tasks.await(fileRef.putStream(stream, metadata))

            Log.d(TAG, "Upload complete. Fetching download URL...")
            val downloadUrl = Tasks.await(fileRef.downloadUrl).toString()
            Log.d(TAG, "Audio download URL obtained: $downloadUrl")

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Storage upload error: ${e.message}", e)
            Result.failure(e)
        } finally {
            try {
                stream?.close()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Writes track metadata to Firestore so all friends can discover and stream it.
     */
    suspend fun saveSongDocument(song: Song): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated()

            Log.d(TAG, "Saving song '${song.title}' to Firestore...")
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
            Log.d(TAG, "Song '${song.title}' saved to Firestore successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore write error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSongDocument(songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated()
            Tasks.await(firestore.collection(COLLECTION_SONGS).document(songId).delete())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore delete error: ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "FirebaseSongDataSource"
        private const val COLLECTION_SONGS = "songs"
    }
}
