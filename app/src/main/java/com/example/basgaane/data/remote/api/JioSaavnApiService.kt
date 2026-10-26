package com.example.basgaane.data.remote.api

import com.example.basgaane.data.remote.model.JioSaavnAuthTokenResponse
import com.example.basgaane.data.remote.model.JioSaavnSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for JioSaavn open search and stream generation endpoints.
 */
interface JioSaavnApiService {

    /**
     * Search songs by query keyword (song title, artist, or album).
     */
    @GET("api.php")
    suspend fun searchSongs(
        @Query("__call") call: String = "search.getResults",
        @Query("_format") format: String = "json",
        @Query("api_version") apiVersion: Int = 4,
        @Query("ctx") ctx: String = "android",
        @Query("_marker") marker: Int = 0,
        @Query("q") query: String,
        @Query("n") count: Int = 30,
        @Query("p") page: Int = 1
    ): JioSaavnSearchResponse

    /**
     * Generate 320kbps CloudFront streaming authentication URL for a given encrypted media token.
     */
    @GET("api.php")
    suspend fun generateAuthToken(
        @Query("__call") call: String = "song.generateAuthToken",
        @Query("_format") format: String = "json",
        @Query("api_version") apiVersion: Int = 4,
        @Query("ctx") ctx: String = "android",
        @Query("_marker") marker: Int = 0,
        @Query("url") encryptedMediaUrl: String,
        @Query("bitrate") bitrate: Int = 320
    ): JioSaavnAuthTokenResponse
}
