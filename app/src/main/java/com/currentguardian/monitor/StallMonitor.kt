package com.currentguardian.monitor

import android.os.SystemClock
import com.currentguardian.blackbox.IncidentManager
import com.currentguardian.model.StallSnapshot
import com.currentguardian.model.StallState

class StallMonitor(
    private val incidentManager: IncidentManager
) {

    private var lastHeartbeat =
        SystemClock.elapsedRealtime()

    private var stallStart =
        -1L

    fun heartbeat() {

        lastHeartbeat =
            SystemClock.elapsedRealtime()

        if (stallStart >= 0) {

            val duration =
                lastHeartbeat -
                    stallStart

            if (duration < 1000L) {

                stallStart = -1L
            }
        }
    }

    fun check(
        heartbeatExpected: Boolean
    ): StallState {

        if (!heartbeatExpected) {

            return StallState.UNKNOWN
        }

        val now =
            SystemClock.elapsedRealtime()

        val silence =
            now -
                lastHeartbeat

        if (silence < 1000L) {

            return StallState.NORMAL
        }

        if (stallStart < 0) {

            stallStart =
                now
        }

        val duration =
            now -
                stallStart

        val state =
            when {

                duration >= 5000L ->
                    StallState.STALL_CRITICAL

                duration >= 3000L ->
                    StallState.STALL_SUSPECTED

                duration >= 1500L ->
                    StallState.RESPONSIVENESS_DEGRADED

                else ->
                    StallState.POSSIBLE_FRAME_DROP
            }

        incidentManager.recordRaw(

            StallSnapshot(
                timestamp =
                    System.currentTimeMillis(),

                state =
                    state,

                durationMs =
                    duration,

                source =
                    "guardian_observer",

                detail =
                    "heartbeat_silence"
            ).serialize()
        )

        return state
    }

    fun reset() {

        stallStart =
            -1L

        lastHeartbeat =
            SystemClock.elapsedRealtime()
    }
}
