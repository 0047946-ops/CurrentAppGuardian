package com.currentguardian.blackbox

import com.currentguardian.model.IncidentContext
import com.currentguardian.model.RiskState
import java.io.File

class PreCrashRecorder(
    private val incidentManager:
        IncidentManager
) {

    private var sealed =
        false

    fun checkAndSeal(
        context:
            IncidentContext
    ): File? {

        if (sealed) {
            return null
        }

        if (
            context.riskLevel !=
                RiskState.Level.CRITICAL
        ) {
            return null
        }

        sealed = true

        /*
         * 先把完整 Context 寫入現有事件鏈，
         * 再進行封存。
         */
        incidentManager.recordRaw(
            "PRE_CRASH_CONTEXT|" +
                context.serialize()
        )

        return incidentManager
            .sealIncident(
                "PRE_CRASH_CRITICAL"
            )
    }

    fun reset() {
        sealed = false
    }
}
