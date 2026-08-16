package com.currentguardian.model

data class StallSnapshot(
    val timestamp: Long,
    val state: StallState,
    val durationMs: Long,
    val source: String,
    val detail: String
) {

    fun serialize(): String {

        return buildString {

            append("STALL")

            append("|time=")
            append(timestamp)

            append("|state=")
            append(state.name)

            append("|duration_ms=")
            append(durationMs)

            append("|source=")
            append(source.safe())

            append("|detail=")
            append(detail.safe())
        }
    }

    private fun String.safe(): String {

        return replace("|", "/")
            .replace("\n", " ")
            .replace("\r", " ")
    }
}
