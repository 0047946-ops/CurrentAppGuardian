package com.currentguardian.model

data class UniversalEvent(
    val timestamp: Long,
    val type: GuardianEventType,
    val packageName: String?,
    val appLabel: String?,
    val lifecycle: AppLifecycleState,
    val detail: String,
    val severity: Int = 0
) {

    fun serialize(): String {

        return buildString {

            append("EVENT")

            append("|time=")
            append(timestamp)

            append("|type=")
            append(type.name)

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
            append(lifecycle.name)

            append("|severity=")
            append(severity)

            append("|detail=")
            append(detail.safe())
        }
    }

    private fun String.safe(): String {

        return replace("|", "/")
            .replace("\n", " ")
            .replace("\r", " ")
    }

    companion object {

        fun create(
            type: GuardianEventType,
            packageName: String?,
            appLabel: String?,
            lifecycle: AppLifecycleState,
            detail: String = "",
            severity: Int = 0
        ): UniversalEvent {

            return UniversalEvent(
                timestamp =
                    System.currentTimeMillis(),

                type = type,

                packageName =
                    packageName,

                appLabel =
                    appLabel,

                lifecycle =
                    lifecycle,

                detail =
                    detail,

                severity =
                    severity
            )
        }
    }
}
