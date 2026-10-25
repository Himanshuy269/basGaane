package com.example.basgaane.data.datasource.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.basgaane.domain.model.Song
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
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
            val msg = if (e is FirebaseAuthException || e.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true) {
                "Anonymous Authentication is disabled! Please enable it in Firebase Console > Authentication > Sign-in method."
            } else {
                e.localizedMessage ?: "Auth error"
            }
            Log.w(TAG, "Anonymous auth notice: $msg")
            Result.failure(Exception(msg, e))
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
                            val addedBy = doc.getString("addedBy")?.takeIf { it.isNotBlank() } ?: "Friend"

                            Song(
                                id = id,
                                title = title,
                                artist = artist,
                                audioUrl = audioUrl,
                                coverUrl = coverUrl,
                                durationMs = durationMs,
                                isLocal = false,
                                addedAt = addedAt,
                                addedBy = addedBy
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

            val mimeType = context.contentResolver.getType(audioUri) ?: "audio/mpeg"
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                .build()

            Log.d(TAG, "Uploading audio to Firebase Storage: audio/$fileName (mime: $mimeType)...")

            // Try uploading via putFile first (with content resolver fallback to putStream)
            val uploadTask = try {
                fileRef.putFile(audioUri, metadata)
            } catch (e: Exception) {
                Log.w(TAG, "putFile failed, falling back to putStream: ${e.message}")
                stream = context.contentResolver.openInputStream(audioUri)
                    ?: throw IllegalArgumentException("Could not open audio file stream.")
                fileRef.putStream(stream, metadata)
            }

            // Chain to downloadUrl reliably via continueWithTask to eliminate race conditions
            val urlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Audio upload task failed.")
                }
                fileRef.downloadUrl
            }

            val downloadUrl = Tasks.await(urlTask).toString()
            Log.d(TAG, "Audio download URL obtained: $downloadUrl")

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Storage upload error: ${e.message}", e)
            val friendlyMsg = when {
                e is StorageException && (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND || e.message?.contains("Object does not exist", ignoreCase = true) == true) ->
                    "Firebase Storage bucket not found! In Firebase Console, go to Build > Storage and click 'Get Started' to activate Cloud Storage."
                e is StorageException && e.errorCode == StorageException.ERROR_NOT_AUTHENTICATED ->
                    "Not authenticated! In Firebase Console, go to Build > Authentication > Sign-in method and enable 'Anonymous'."
                e is StorageException && e.errorCode == StorageException.ERROR_NOT_AUTHORIZED ->
                    "Permission denied! In Firebase Console > Storage > Rules, allow read and write for authenticated users."
                e.message?.contains("Object does not exist", ignoreCase = true) == true ->
                    "Firebase Storage bucket not found! In Firebase Console, go to Build > Storage and click 'Get Started' to activate Cloud Storage."
                else -> e.localizedMessage ?: "Unknown storage upload error"
            }
            Result.failure(Exception(friendlyMsg, e))
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
                "addedAt" to song.addedAt,
                "addedBy" to song.addedBy
            )
            Tasks.await(firestore.collection(COLLECTION_SONGS).document(song.id).set(songData))
            Log.d(TAG, "Song '${song.title}' saved to Firestore successfully.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore write error: ${e.message}", e)
            val friendlyMsg = when {
                e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Firestore permission denied! In Firebase Console > Firestore Database > Rules, allow read and write for authenticated users."
                e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.NOT_FOUND ->
                    "Firestore database not found! In Firebase Console, go to Build > Firestore Database and click 'Create database'."
                else -> e.localizedMessage ?: "Unknown Firestore error"
            }
            Result.failure(Exception(friendlyMsg, e))
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
