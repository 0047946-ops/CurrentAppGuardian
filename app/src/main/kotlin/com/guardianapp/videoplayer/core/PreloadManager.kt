package com.guardianapp.videoplayer.core

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PreloadState(
    val videoUrl: String = "",
    val isPreloading: Boolean = false,
    val preloadProgress: Float = 0f
)

class PreloadManager(private val player: ExoPlayer) {
    private val _preloadState = MutableStateFlow(PreloadState())
    val preloadState: StateFlow<PreloadState> = _preloadState.asStateFlow()

    private val preloadCache = mutableMapOf<String, MediaItem>()

    fun preloadVideo(videoUrl: String) {
        if (preloadCache.containsKey(videoUrl)) {
            return
        }

        _preloadState.value = PreloadState(
            videoUrl = videoUrl,
            isPreloading = true,
            preloadProgress = 0f
        )

        val mediaItem = MediaItem.fromUri(videoUrl)
        preloadCache[videoUrl] = mediaItem

        _preloadState.value = PreloadState(
            videoUrl = videoUrl,
            isPreloading = false,
            preloadProgress = 100f
        )
    }

    fun getCachedVideo(videoUrl: String): MediaItem? {
        return preloadCache[videoUrl]
    }

    fun clearCache() {
        preloadCache.clear()
    }

    fun getCacheSize(): Int = preloadCache.size
}
