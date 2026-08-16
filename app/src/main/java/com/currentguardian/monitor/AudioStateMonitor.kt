package com.currentguardian.monitor

import android.content.Context
import android.media.AudioManager
import com.currentguardian.blackbox.DualEvidenceVault

class AudioStateMonitor(
    context: Context,
    private val evidence:
        DualEvidenceVault
) {

    private val audioManager =
        context.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

    fun capture(): AudioState {

        val mode =
            audioManager.mode

        val musicActive =
            audioManager
                .isMusicActive

        val wired =
            audioManager
                .isWiredHeadsetOn

        val bluetooth =
            audioManager
                .isBluetoothA2dpOn

        val state =
            AudioState(
                timestamp =
                    System.currentTimeMillis(),

                mode =
                    mode,

                musicActive =
                    musicActive,

                wiredHeadset =
                    wired,

                bluetoothA2dp =
                    bluetooth
            )

        evidence.recordTarget(
            state.serialize()
        )

        return state
    }
}

data class AudioState(
    val timestamp: Long,
    val mode: Int,
    val musicActive: Boolean,
    val wiredHeadset: Boolean,
    val bluetoothA2dp: Boolean
) {

    fun serialize(): String {

        return buildString {

            append("AUDIO_STATE")

            append("|time=")
            append(timestamp)

            append("|mode=")
            append(mode)

            append("|music_active=")
            append(musicActive)

            append("|wired_headset=")
            append(wiredHeadset)

            append("|bluetooth_a2dp=")
            append(bluetoothA2dp)
        }
    }
}
