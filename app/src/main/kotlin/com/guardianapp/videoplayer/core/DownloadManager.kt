package com.guardianapp.videoplayer.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DownloadTask(
    val videoUrl: String,
    val fileName: String,
    val downloadProgress: Float = 0f,
    val isCompleted: Boolean = false
)

class DownloadManager(private val context: Context) {
    private object PreferenceKeys {
        val DOWNLOADED_VIDEOS = stringSetPreferencesKey("downloaded_videos")
    }

    suspend fun startDownload(videoUrl: String, fileName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferenceKeys.DOWNLOADED_VIDEOS] ?: emptySet()
            preferences[PreferenceKeys.DOWNLOADED_VIDEOS] = current + "$fileName:$videoUrl"
        }
    }

    fun getDownloadedVideos(): Flow<Set<String>> =
        context.dataStore.data.map { preferences ->
            preferences[PreferenceKeys.DOWNLOADED_VIDEOS] ?: emptySet()
        }

    suspend fun removeDownload(videoUrl: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferenceKeys.DOWNLOADED_VIDEOS] ?: emptySet()
            preferences[PreferenceKeys.DOWNLOADED_VIDEOS] =
                current.filter { !it.contains(videoUrl) }.toSet()
        }
    }
}
