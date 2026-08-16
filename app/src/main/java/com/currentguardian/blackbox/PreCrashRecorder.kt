package com.currentguardian.blackbox

import com.currentguardian.model.IncidentContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreCrashRecorder(
    private val incidentManager: IncidentManager
) {

    private var sealed =
        false

    fun checkAndSeal(
        context: IncidentContext
    ): File? {

        if (sealed) {
            return null
        }

        if (
            !context.riskLevel.isCritical()
        ) {
            return null
        }

        sealed = true

        val file =
            incidentManager
                .sealIncident(
                    "PRE_CRASH_CRITICAL"
                )

        incidentManager.recordRaw(
            "PRE_CRASH_CONTEXT|" +
                context.serialize()
        )

        return file
    }

    fun reset() {

        sealed = false
    }
}
