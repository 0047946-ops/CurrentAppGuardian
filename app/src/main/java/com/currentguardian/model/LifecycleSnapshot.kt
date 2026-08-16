package com.currentguardian.model

data class LifecycleSnapshot(
    val timestamp: Long,
    val packageName: String?,
    val appLabel: String?,
    val lifecycleState: AppLifecycleState,
    val deathState: AppDeathState,
    val processObserved: Boolean,
    val foregroundObserved: Boolean,
    val elapsedSinceLastSeenMs: Long,
    val reason: String
) {

    fun serialize(): String {

        return buildString {

            append("LIFECYCLE_SNAPSHOT")

            append("|time=")
            append(timestamp)

            append("|package=")
            append(
                packageName
                    ?.safe()
                    ?: "unknown"
            )

            append("|app=")
            append(
                appLabel
                    ?.safe()
                    ?: "unknown"
            )

            append("|lifecycle=")
            append(lifecycleState.name)

            append("|death=")
            append(deathState.name)

            append("|process=")
            append(processObserved)

            append("|foreground=")
            append(foregroundObserved)

            append("|elapsed_since_last_seen_ms=")
            append(elapsedSinceLastSeenMs)

            append("|reason=")
            append(reason.safe())
        }
    }

    private fun String.safe(): String {

        return replace("|", "/")
            .replace("\n", " ")
            .replace("\r", " ")
    }
}
