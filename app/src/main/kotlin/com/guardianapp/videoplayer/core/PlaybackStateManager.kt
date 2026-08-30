package com.guardianapp.videoplayer.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class PlaybackState(
    val videoUrl: String = "",
    val playbackPositionMs: Long = 0L,
    val playbackSpeed: Float = 1f,
    val selectedQuality: String = "auto",
    val volume: Float = 1f,
    val brightness: Float = 1f,
    val isFullScreen: Boolean = false
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "playback_prefs")

class PlaybackStateManager(private val context: Context) {
    private object PreferenceKeys {
        val VIDEO_URL = stringPreferencesKey("video_url")
        val PLAYBACK_POSITION = longPreferencesKey("playback_position")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val SELECTED_QUALITY = stringPreferencesKey("selected_quality")
        val VOLUME = floatPreferencesKey("volume")
        val BRIGHTNESS = floatPreferencesKey("brightness")
        val IS_FULLSCREEN = booleanPreferencesKey("is_fullscreen")
    }

    suspend fun savePlaybackState(state: PlaybackState) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.VIDEO_URL] = state.videoUrl
            preferences[PreferenceKeys.PLAYBACK_POSITION] = state.playbackPositionMs
            preferences[PreferenceKeys.PLAYBACK_SPEED] = state.playbackSpeed
            preferences[PreferenceKeys.SELECTED_QUALITY] = state.selectedQuality
            preferences[PreferenceKeys.VOLUME] = state.volume
            preferences[PreferenceKeys.BRIGHTNESS] = state.brightness
            preferences[PreferenceKeys.IS_FULLSCREEN] = state.isFullScreen
        }
    }

    fun getPlaybackState(): Flow<PlaybackState> =
        context.dataStore.data.map { preferences ->
            PlaybackState(
                videoUrl = preferences[PreferenceKeys.VIDEO_URL] ?: "",
                playbackPositionMs = preferences[PreferenceKeys.PLAYBACK_POSITION] ?: 0L,
                playbackSpeed = preferences[PreferenceKeys.PLAYBACK_SPEED] ?: 1f,
                selectedQuality = preferences[PreferenceKeys.SELECTED_QUALITY] ?: "auto",
                volume = preferences[PreferenceKeys.VOLUME] ?: 1f,
                brightness = preferences[PreferenceKeys.BRIGHTNESS] ?: 1f,
                isFullScreen = preferences[PreferenceKeys.IS_FULLSCREEN] ?: false
            )
        }

    suspend fun updatePlaybackPosition(positionMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.PLAYBACK_POSITION] = positionMs
        }
    }
}
