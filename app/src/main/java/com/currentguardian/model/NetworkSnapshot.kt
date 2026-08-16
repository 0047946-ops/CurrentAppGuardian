package com.currentguardian.model

data class NetworkSnapshot(
    val timestamp: Long,
    val type: String,
    val connected: Boolean,
    val validated: Boolean,
    val latencyMs: Long,
    val jitterMs: Long
) {

    fun serialize(): String {

        return buildString {

            append("NETWORK")

            append("|time=")
            append(timestamp)

            append("|type=")
            append(type.safe())

            append("|connected=")
            append(connected)

            append("|validated=")
            append(validated)

            append("|latency_ms=")
            append(latencyMs)

            append("|jitter_ms=")
            append(jitterMs)
        }
    }

    private fun String.safe(): String {

        return replace("|", "/")
    }
}
