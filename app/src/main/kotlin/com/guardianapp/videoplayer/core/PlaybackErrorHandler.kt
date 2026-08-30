package com.guardianapp.videoplayer.core

import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaybackErrorHandler(
    private val player: ExoPlayer,
    private val networkMonitor: NetworkMonitor,
    private val scope: CoroutineScope
) {
    private var retryCount = 0
    private val maxRetries = 4
    private var lastErrorTime = 0L

    fun handlePlaybackError(error: PlaybackException) {
        if (!shouldRetry(error)) {
            return
        }

        scope.launch {
            val backoffMs = calculateBackoff(retryCount)
            delay(backoffMs)

            if (networkMonitor.isConnected()) {
                retryCount++
                player.prepare()
            }
        }
    }

    private fun shouldRetry(error: PlaybackException): Boolean {
        return retryCount < maxRetries && isRetryableError(error)
    }

    private fun isRetryableError(error: PlaybackException): Boolean {
        return when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> true
            else -> false
        }
    }

    private fun calculateBackoff(retryCount: Int): Long {
        return when (retryCount) {
            0 -> 1000L   // 1 second
            1 -> 2000L   // 2 seconds
            2 -> 4000L   // 4 seconds
            else -> 8000L // 8 seconds
        }
    }

    fun resetRetryCount() {
        retryCount = 0
    }
}
