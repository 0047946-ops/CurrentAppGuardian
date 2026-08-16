package com.currentguardian.monitor

import android.content.Context
import com.currentguardian.blackbox.IncidentManager
import com.currentguardian.model.PerformanceSnapshot
import com.currentguardian.model.SystemSnapshot

class PerformanceMonitorCoordinator(
    private val context: Context,
    private val incidentManager: IncidentManager
) {

    private val sampler =
        PerformanceSampler(context)

    private val stallMonitor =
        StallMonitor()

    private val adaptiveMonitor =
        AdaptiveMonitor()

    private val adaptiveSamplingController =
        AdaptiveSamplingController()

    private var lastSnapshot:
        PerformanceSnapshot? = null

    fun sample(
        appInForeground: Boolean
    ): PerformanceSnapshot {

        val snapshot =
            sampler.sample()

        lastSnapshot =
            snapshot

        val stall =
            stallMonitor.evaluate(
                snapshot
            )

        adaptiveMonitor.update(
            snapshot =
                snapshot,

            stall =
                stall,

            appInForeground =
                appInForeground
        )

        adaptiveSamplingController.update(
            snapshot =
                snapshot,

            stall =
                stall,

            appInForeground =
                appInForeground
        )

        incidentManager.recordRaw(
            buildPerformanceRecord(
                snapshot =
                    snapshot,

                stall =
                    stall
            )
        )

        return snapshot
    }

    fun collectSystemSnapshot():
        SystemSnapshot {

        val snapshot =
            SystemSnapshot.collect(
                context
            )

        incidentManager.recordRaw(
            buildSystemRecord(
                snapshot
            )
        )

        return snapshot
    }

    fun updateNetwork(
        type: String,
        validated: Boolean
    ) {

        incidentManager.recordRaw(
            "NETWORK_STATE" +
                "|type=" +
                type.safe() +
                "|validated=" +
                validated +
                "|time=" +
                System.currentTimeMillis()
        )
    }

    fun currentSnapshot():
        PerformanceSnapshot? {

        return lastSnapshot
    }

    fun monitoringIntervalMs():
        Long {

        return adaptiveSamplingController
            .currentIntervalMs()
    }

    private fun buildPerformanceRecord(
        snapshot:
            PerformanceSnapshot,

        stall:
            StallState
    ): String {

        return buildString {

            append(
                "PERFORMANCE_SAMPLE"
            )

            append(
                "|time="
            )

            append(
                System.currentTimeMillis()
            )

            append(
                "|cpu="
            )

            append(
                snapshot.cpuUsagePercent
            )

            append(
                "|ram_mb="
            )

            append(
                snapshot.ramUsedMb
            )

            append(
                "|fps="
            )

            append(
                snapshot.fps
            )

            append(
                "|frame_time="
            )

            append(
                snapshot.frameTimeMs
            )

            append(
                "|stall="
            )

            append(
                stall.name
            )
        }
    }

    private fun buildSystemRecord(
        snapshot:
            SystemSnapshot
    ): String {

        return buildString {

            append(
                "SYSTEM_SAMPLE"
            )

            append(
                "|time="
            )

            append(
                System.currentTimeMillis()
            )

            append(
                "|cpu="
            )

            append(
                snapshot.cpuLoad
            )

            append(
                "|available_ram_mb="
            )

            append(
                snapshot.availableRamMb
            )

            append(
                "|temperature_c="
            )

            append(
                snapshot.temperatureC
            )

            append(
                "|battery_percent="
            )

            append(
                snapshot.batteryPercent
            )

            append(
                "|network="
            )

            append(
                snapshot.networkType.safe()
            )

            append(
                "|network_validated="
            )

            append(
                snapshot.networkValidated
            )
        }
    }

    private fun String.safe():
        String {

        return replace(
            "|",
            "/"
        )
            .replace(
                "\n",
                " "
            )
            .replace(
                "\r",
                " "
            )
    }
}
