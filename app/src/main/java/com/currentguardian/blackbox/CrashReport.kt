package com.currentguardian.blackbox

import java.io.File

object CrashReport {

    fun createSummary(
        incidentFile: File?,
        targetPackage: String?,
        lastKnownState: String?
    ): String {

        return buildString {

            appendLine(
                "當前應用效能管家事故報告"
            )

            appendLine(
                "================================"
            )

            appendLine(
                "Target=$targetPackage"
            )

            appendLine(
                "Time=${System.currentTimeMillis()}"
            )

            appendLine(
                "LastKnownState=$lastKnownState"
            )

            appendLine(
                "IncidentFile=" +
                    (incidentFile?.absolutePath
                        ?: "NONE")
            )

            appendLine(
                "================================"
            )

            appendLine(
                "注意：本報告只描述可觀測資料。"
            )

            appendLine(
                "不得僅依據監控資料直接判定"
            )

            appendLine(
                "第三方程式的實際 Crash 根因。"
            )
        }
    }
}
