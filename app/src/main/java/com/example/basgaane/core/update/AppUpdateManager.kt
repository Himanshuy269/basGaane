package com.example.basgaane.core.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manages checking, downloading, and launching in-app updates for BasGaane.
 * Primary source: GitHub Releases (free, unlimited bandwidth, zero Firebase storage).
 * Fallback source: Firebase Firestore (app_config/update).
 */
class AppUpdateManager(private val context: Context) {

    companion object {
        private const val TAG = "AppUpdateManager"
        private const val GITHUB_OWNER = "Himanshuy269"
        private const val GITHUB_REPO = "basGaane"
        private const val GITHUB_LATEST_RELEASE_URL =
            "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    }

    /**
     * Retrieves the currently installed version code.
     */
    fun getCurrentVersionCode(): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            PackageInfoCompat.getLongVersionCode(packageInfo)
        } catch (e: Exception) {
            1L
        }
    }

    /**
     * Retrieves the currently installed version name (e.g., "1.0.1").
     */
    fun getCurrentVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * Checks if a newer version of BasGaane is available.
     * Checks GitHub Releases first, then Firestore as fallback.
     */
    suspend fun checkForUpdates(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val currentCode = getCurrentVersionCode()
        val currentName = getCurrentVersionName()

        // 1. Try GitHub Releases
        try {
            val githubUpdate = fetchFromGitHub(currentCode, currentName)
            if (githubUpdate != null) {
                Log.d(TAG, "Update found on GitHub: ${githubUpdate.latestVersionName}")
                return@withContext githubUpdate
            }
        } catch (e: Exception) {
            Log.d(TAG, "GitHub update check info: ${e.message}")
        }

        // 2. Try Firestore as fallback or manual override
        try {
            val firestoreUpdate = fetchFromFirestore(currentCode, currentName)
            if (firestoreUpdate != null) {
                Log.d(TAG, "Update found on Firestore: ${firestoreUpdate.latestVersionName}")
                return@withContext firestoreUpdate
            }
        } catch (e: Exception) {
            Log.d(TAG, "Firestore update check info: ${e.message}")
        }

        null
    }

    private fun fetchFromGitHub(currentCode: Long, currentName: String): AppUpdateInfo? {
        val url = URL(GITHUB_LATEST_RELEASE_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("User-Agent", "BasGaane-Android/$currentName")
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json")

        if (conn.responseCode != 200) {
            conn.disconnect()
            return null
        }

        val jsonString = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
        conn.disconnect()

        val json = JSONObject(jsonString)
        val tagName = json.optString("tag_name", "").trim()
        val releaseName = json.optString("name", "BasGaane Update")
        val releaseNotes = json.optString("body", "Bug fixes and performance improvements.")
        val cleanVersionName = tagName.removePrefix("v").removePrefix("V").trim()

        // Look for release APK in assets
        val assets = json.optJSONArray("assets") ?: return null
        var downloadUrl: String? = null
        var apkSize = 0L

        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val assetName = asset.optString("name", "")
            if (assetName.endsWith(".apk", ignoreCase = true)) {
                // Prefer release APK if multiple are attached
                if (downloadUrl == null || assetName.contains("release", ignoreCase = true)) {
                    downloadUrl = asset.optString("browser_download_url", "")
                    apkSize = asset.optLong("size", 0L)
                }
            }
        }

        if (downloadUrl.isNullOrBlank()) {
            return null
        }

        val isNewer = isNewerVersion(currentName, cleanVersionName, currentCode, 0L)
        if (isNewer) {
            return AppUpdateInfo(
                latestVersionCode = currentCode + 1,
                latestVersionName = cleanVersionName,
                downloadUrl = downloadUrl,
                releaseNotes = releaseNotes,
                releaseTitle = releaseName,
                isForceUpdate = false,
                apkSize = apkSize
            )
        }

        return null
    }

    private fun fetchFromFirestore(currentCode: Long, currentName: String): AppUpdateInfo? {
        return try {
            val snapshot = Tasks.await(
                FirebaseFirestore.getInstance()
                    .collection("app_config")
                    .document("update")
                    .get()
            )

            if (!snapshot.exists()) return null

            val latestCode = snapshot.getLong("latestVersionCode") ?: 0L
            val latestName = snapshot.getString("latestVersionName") ?: ""
            val downloadUrl = snapshot.getString("downloadUrl") ?: snapshot.getString("apkUrl") ?: ""
            val releaseNotes = snapshot.getString("releaseNotes") ?: "New features and stability enhancements."
            val releaseTitle = snapshot.getString("releaseTitle") ?: "BasGaane Update"
            val isForce = snapshot.getBoolean("isForceUpdate") ?: false
            val apkSize = snapshot.getLong("apkSize") ?: 0L

            if (downloadUrl.isBlank()) return null

            val isNewer = isNewerVersion(currentName, latestName, currentCode, latestCode)
            if (isNewer) {
                AppUpdateInfo(
                    latestVersionCode = latestCode,
                    latestVersionName = latestName.ifBlank { "v$latestCode" },
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes,
                    releaseTitle = releaseTitle,
                    isForceUpdate = isForce,
                    apkSize = apkSize
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking Firestore updates: ${e.message}")
            null
        }
    }

    /**
     * Determines whether [latestName] / [latestCode] is newer than [currentName] / [currentCode].
     */
    fun isNewerVersion(
        currentName: String,
        latestName: String,
        currentCode: Long,
        latestCode: Long
    ): Boolean {
        if (latestCode > 0 && currentCode > 0 && latestCode != currentCode) {
            return latestCode > currentCode
        }

        // Compare semantic version components
        val currentParts = currentName.split(Regex("[^0-9]+")).filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }
        val latestParts = latestName.split(Regex("[^0-9]+")).filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }

        return false
    }

    /**
     * Downloads the APK file to cache directory with progress reporting.
     */
    suspend fun downloadUpdate(
        updateInfo: AppUpdateInfo,
        onProgress: (UpdateDownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        onProgress(UpdateDownloadState.Downloading(0f, 0L, updateInfo.apkSize))

        var connection: HttpURLConnection? = null
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            } else {
                // Clear old APKs
                updatesDir.listFiles()?.forEach { if (it.name.endsWith(".apk")) it.delete() }
            }

            val targetFile = File(updatesDir, "BasGaane_${updateInfo.latestVersionName}.apk")
            if (targetFile.exists()) targetFile.delete()

            connection = openConnectionWithRedirects(updateInfo.downloadUrl)
            val totalBytes = if (connection.contentLengthLong > 0) connection.contentLengthLong else updateInfo.apkSize

            connection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastReportedTime = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastReportedTime > 150 || totalRead == totalBytes) {
                            lastReportedTime = now
                            val progress = if (totalBytes > 0) {
                                (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            } else {
                                0.5f
                            }
                            onProgress(
                                UpdateDownloadState.Downloading(
                                    progress = progress,
                                    downloadedBytes = totalRead,
                                    totalBytes = totalBytes
                                )
                            )
                        }
                    }
                    output.flush()
                }
            }

            if (targetFile.exists() && targetFile.length() > 0) {
                Log.d(TAG, "Update downloaded successfully: ${targetFile.absolutePath} (${targetFile.length()} bytes)")
                onProgress(UpdateDownloadState.ReadyToInstall(targetFile.absolutePath))
            } else {
                onProgress(UpdateDownloadState.Error("Downloaded file is invalid or empty."))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update", e)
            onProgress(UpdateDownloadState.Error(e.localizedMessage ?: "Download failed"))
        } finally {
            connection?.disconnect()
        }
    }

    private fun openConnectionWithRedirects(urlString: String, maxRedirects: Int = 6): HttpURLConnection {
        var currentUrl = urlString
        var redirects = 0
        while (redirects < maxRedirects) {
            val url = URL(currentUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 25000
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", "BasGaane-Android/${getCurrentVersionName()}")

            val status = conn.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                status == HttpURLConnection.HTTP_MOVED_PERM ||
                status == HttpURLConnection.HTTP_SEE_OTHER ||
                status == 307 || status == 308
            ) {
                val location = conn.getHeaderField("Location")
                conn.disconnect()
                if (!location.isNullOrBlank()) {
                    currentUrl = location
                    redirects++
                    continue
                }
            }
            return conn
        }
        throw IllegalStateException("Too many redirects while accessing update URL")
    }

    /**
     * Launches the system package installer for the downloaded APK.
     */
    fun installUpdate(apkPath: String): Result<Unit> {
        return try {
            val apkFile = File(apkPath)
            if (!apkFile.exists()) {
                return Result.failure(Exception("Update file not found on disk."))
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            Result.failure(e)
        }
    }
}
