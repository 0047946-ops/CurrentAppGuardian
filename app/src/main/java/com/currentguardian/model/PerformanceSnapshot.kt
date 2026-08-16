package com.currentguardian.model

data class PerformanceSnapshot(
    val timestamp: Long,
    val cpuLoadPercent: Int,
    val availableRamMb: Long,
    val totalRamMb: Long,
    val temperatureC: Float,
    val batteryPercent: Int,
    val batteryCharging: Boolean,
    val networkType: String,
    val networkValidated: Boolean,
    val estimatedLatencyMs: Long,
    val jitterMs: Long
) {

    fun serialize(): String {

        return buildString {

            append("PERFORMANCE")

            append("|time=")
            append(timestamp)

            append("|cpu=")
            append(cpuLoadPercent)

            append("|ram_available_mb=")
            append(availableRamMb)

            append("|ram_total_mb=")
            append(totalRamMb)

            append("|temperature_c=")
            append(temperatureC)

            append("|battery=")
            append(batteryPercent)

            append("|charging=")
            append(batteryCharging)

            append("|network=")
            append(networkType.safe())

            append("|network_validated=")
            append(networkValidated)

            append("|latency_ms=")
            append(estimatedLatencyMs)

            append("|jitter_ms=")
            append(jitterMs)
        }
    }

    private fun String.safe(): String {

        return replace("|", "/")
            .replace("\n", " ")
            .replace("\r", " ")
    }
}
