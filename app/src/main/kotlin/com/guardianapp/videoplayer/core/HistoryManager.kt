package com.guardianapp.videoplayer.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class HistoryItem(
    val videoUrl: String,
    val videoTitle: String,
    val lastPlayedTime: Long,
    val watchedDuration: Long
)

class HistoryManager(private val context: Context) {
    private object PreferenceKeys {
        val HISTORY_PREFIX = "history_"
        fun historyKey(url: String) = stringPreferencesKey("${HISTORY_PREFIX}${url.hashCode()}")
    }

    suspend fun addToHistory(videoUrl: String, videoTitle: String, watchedDuration: Long) {
        context.dataStore.edit { preferences ->
            val historyData = "$videoUrl|$videoTitle|${System.currentTimeMillis()}|$watchedDuration"
            preferences[PreferenceKeys.historyKey(videoUrl)] = historyData
        }
    }

    fun getHistory(): Flow<List<HistoryItem>> =
        context.dataStore.data.map { preferences ->
            preferences.asMap()
                .filter { (key, _) -> key.name.startsWith(PreferenceKeys.HISTORY_PREFIX) }
                .mapNotNull { (_, value) ->
                    val parts = (value as? String)?.split("|") ?: return@mapNotNull null
                    if (parts.size >= 4) {
                        HistoryItem(
                            videoUrl = parts[0],
                            videoTitle = parts[1],
                            lastPlayedTime = parts[2].toLongOrNull() ?: 0L,
                            watchedDuration = parts[3].toLongOrNull() ?: 0L
                        )
                    } else null
                }
                .sortedByDescending { it.lastPlayedTime }
        }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.asMap()
                .filter { (key, _) -> key.name.startsWith(PreferenceKeys.HISTORY_PREFIX) }
                .forEach { (key, _) -> preferences.remove(key) }
        }
    }
}
