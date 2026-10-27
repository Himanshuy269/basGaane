package com.example.basgaane.data.remote.repository

import android.util.Log
import com.example.basgaane.data.remote.api.JioSaavnApiService
import com.example.basgaane.data.remote.model.JioSaavnSongItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Repository for querying online music catalog and resolving streaming audio URLs via Retrofit.
 */
class OnlineMusicRepository {

    private val apiService: JioSaavnApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.jiosaavn.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(JioSaavnApiService::class.java)
    }

    /**
     * Search songs online using keywords.
     */
    suspend fun searchSongs(query: String): Result<List<JioSaavnSongItem>> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext Result.success(emptyList())
        }
        try {
            val response = apiService.searchSongs(query = cleanQuery)
            val songs = response.results ?: emptyList()
            Result.success(songs.filter { !it.cleanTitle.isBlank() && !it.encryptedMediaUrl.isNullOrBlank() })
        } catch (e: Exception) {
            Log.e(TAG, "Error searching online songs for '$query'", e)
            Result.failure(e)
        }
    }

    /**
     * Resolves the direct 320kbps CloudFront streaming URL from the encrypted media token,
     * falling back to 160kbps if high bitrate is not provisioned for the track.
     */
    suspend fun resolveStreamUrl(encryptedMediaUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // First try 320 kbps high fidelity
            val response = apiService.generateAuthToken(encryptedMediaUrl = encryptedMediaUrl, bitrate = 320)
            val authUrl = response.authUrl
            if (!authUrl.isNullOrBlank()) {
                return@withContext Result.success(authUrl)
            }
            // Fallback to 160 kbps standard fidelity
            val fallbackResponse = apiService.generateAuthToken(encryptedMediaUrl = encryptedMediaUrl, bitrate = 160)
            val fallbackUrl = fallbackResponse.authUrl
            if (!fallbackUrl.isNullOrBlank()) {
                return@withContext Result.success(fallbackUrl)
            }
            Result.failure(Exception("Stream URL could not be generated for this track"))
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving stream URL", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches trending / popular songs for the initial screen state.
     */
    suspend fun getTrendingSongs(): Result<List<JioSaavnSongItem>> {
        return searchSongs("Trending Songs")
    }

    companion object {
        private const val TAG = "OnlineMusicRepo"
    }
}
