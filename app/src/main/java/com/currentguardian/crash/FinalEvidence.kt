package com.currentguardian.crash

data class FinalEvidence(
    val timestamp: Long,
    val packageName: String?,
    val appLabel: String?,
    val pid: Int?,
    val lifecycle: String,
    val cpu: Int?,
    val availableRamMb: Long?,
    val temperatureC: Float?,
    val batteryPercent: Int?,
    val networkType: String?,
    val latencyMs: Long?,
    val jitterMs: Long?,
    val riskLevel: String?,
    val lastEvent: String?,
    val lastHeartbeat: Long?
) {

    fun serialize():
        String {

        return buildString {

            append("FINAL_EVIDENCE")

            append("|time=")
            append(timestamp)

            append("|package=")
            append(
                packageName
                    ?: "UNKNOWN"
            )

            append("|app=")
            append(
                appLabel
                    ?: "UNKNOWN"
            )

            append("|pid=")
            append(
                pid ?: -1
            )

            append("|lifecycle=")
            append(
                lifecycle
            )

            append("|cpu=")
            append(
                cpu ?: -1
            )

            append("|ram_mb=")
            append(
                availableRamMb ?: -1
            )

            append("|temperature=")
            append(
                temperatureC ?: -1f
            )

            append("|battery=")
            append(
                batteryPercent ?: -1
            )

            append("|network=")
            append(
                networkType
                    ?: "UNKNOWN"
            )

            append("|latency_ms=")
            append(
                latencyMs ?: -1
            )

            append("|jitter_ms=")
            append(
                jitterMs ?: -1
            )

            append("|risk=")
            append(
                riskLevel
                    ?: "UNKNOWN"
            )

            append("|last_event=")
            append(
                lastEvent
                    ?: "UNKNOWN"
            )

            append("|last_heartbeat=")
            append(
                lastHeartbeat ?: -1
            )
        }
    }
}
