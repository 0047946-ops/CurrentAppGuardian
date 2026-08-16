package com.currentguardian.model

data class IncidentContext(
    val timestamp: Long,
    val packageName: String?,
    val appLabel: String?,
    val lifecycle: AppLifecycleState,
    val lastEvent: GuardianEventType,
    val riskScore: Int,
    val riskLevel: RiskState.Level,
    val cpuLoad: Int,
    val availableRamMb: Long,
    val temperatureC: Float,
    val batteryPercent: Int,
    val networkType: String,
    val networkValidated: Boolean
) {

    fun serialize(): String {

        return buildString {

            append("INCIDENT_CONTEXT")

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
            append(lifecycle.name)

            append("|last_event=")
            append(lastEvent.name)

            append("|risk_score=")
            append(riskScore)

            append("|risk_level=")
            append(riskLevel.name)

            append("|cpu=")
            append(cpuLoad)

            append("|ram=")
            append(availableRamMb)

            append("|temperature=")
            append(temperatureC)

            append("|battery=")
            append(batteryPercent)

            append("|network=")
            append(
                networkType.safe()
            )

            append("|network_validated=")
            append(networkValidated)
        }
    }

    private fun String.safe():
        String {

        return replace("|", "/")
            .replace("\n", " ")
            .replace("\r", " ")
    }
}
