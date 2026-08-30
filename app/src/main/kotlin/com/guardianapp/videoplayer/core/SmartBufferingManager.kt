package com.guardianapp.videoplayer.core

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min

data class BufferingStats(
    val bufferDurationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val stutterCount: Int = 0,
    val currentBitrateMbps: Float = 0f,
    val recommendedQuality: String = "auto"
)

class SmartBufferingManager(private val player: ExoPlayer) {
    private val _bufferingStats = MutableStateFlow(BufferingStats())
    val bufferingStats: StateFlow<BufferingStats> = _bufferingStats.asStateFlow()

    private var stutterCount = 0
    private var lastBufferPos = 0L
    private val bufferedDurationMs: Long
        get() = player.bufferedPosition - player.currentPosition

    fun updateBufferingStrategy(networkBandwidthMbps: Float, videoQualityMbps: Float) {
        val targetBufferMs = calculateTargetBuffer(networkBandwidthMbps, videoQualityMbps)
        player.setBufferingParameters(
            androidx.media3.exoplayer.LoadControl.DEFAULT.bufferingParameters
                .buildUpon()
                .setTargetBufferBytes(targetBufferMs.toInt() * 1000)
                .build()
        )
    }

    private fun calculateTargetBuffer(networkMbps: Float, qualityMbps: Float): Long {
        return when {
            networkMbps < qualityMbps -> 10000 // 10 seconds
            networkMbps < qualityMbps * 1.5f -> 20000 // 20 seconds
            else -> 30000 // 30 seconds
        }
    }

    fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val isBuffering = playbackState == Player.STATE_BUFFERING
        if (isBuffering) {
            stutterCount++
        }
        updateStats(isBuffering)
    }

    private fun updateStats(isBuffering: Boolean) {
        _bufferingStats.value = BufferingStats(
            bufferDurationMs = bufferedDurationMs,
            isBuffering = isBuffering,
            stutterCount = stutterCount,
            currentBitrateMbps = 0f,
            recommendedQuality = getRecommendedQuality()
        )
    }

    private fun getRecommendedQuality(): String {
        return when (stutterCount) {
            0 -> "auto"
            1, 2 -> "720p"
            else -> "480p"
        }
    }

    fun resetStutterCount() {
        stutterCount = 0
    }
}
