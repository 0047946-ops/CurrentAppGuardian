package com.currentguardian.model

data class GuardianEvent(
    val timestamp: Long,
    val type: String,
    val detail: String,
    val severity: Int = 0
) {

    fun serialize(): String {
        return buildString {
            append(timestamp)
            append("|")
            append(type.safe())
            append("|")
            append(detail.safe())
            append("|")
            append(severity)
        }
    }

    private fun String.safe(): String {
        return replace("|", "/")
            .replace("\n", " ")
            .replace("\r", " ")
    }

    companion object {

        fun create(
            type: String,
            detail: String,
            severity: Int = 0
        ): GuardianEvent {

            return GuardianEvent(
                timestamp = System.currentTimeMillis(),
                type = type,
                detail = detail,
                severity = severity
            )
        }
    }
}
