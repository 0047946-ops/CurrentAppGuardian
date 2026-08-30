package com.guardianapp.videoplayer.core

import android.content.Context
import android.view.PiPParams
import androidx.media3.exoplayer.ExoPlayer

data class PiPState(
    val isPiPActive: Boolean = false,
    val pipWidth: Int = 400,
    val pipHeight: Int = 225
)

class PictureInPictureManager(private val context: Context, private val player: ExoPlayer) {
    private var currentPiPState = PiPState()

    fun enablePiP(): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                currentPiPState = PiPState(isPiPActive = true)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun disablePiP() {
        currentPiPState = PiPState(isPiPActive = false)
    }

    fun isPiPActive(): Boolean = currentPiPState.isPiPActive

    fun getPiPState(): PiPState = currentPiPState

    fun setPiPSize(width: Int, height: Int) {
        currentPiPState = currentPiPState.copy(pipWidth = width, pipHeight = height)
    }
}
