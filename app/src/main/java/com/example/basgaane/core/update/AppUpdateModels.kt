package com.example.basgaane.core.update

import androidx.compose.runtime.Immutable

@Immutable
data class AppUpdateInfo(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val downloadUrl: String,
    val releaseNotes: String = "",
    val releaseTitle: String = "",
    val isForceUpdate: Boolean = false,
    val apkSize: Long = 0L
)

sealed interface UpdateDownloadState {
    object Idle : UpdateDownloadState
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : UpdateDownloadState
    data class ReadyToInstall(val apkPath: String) : UpdateDownloadState
    data class Error(val message: String) : UpdateDownloadState
}
