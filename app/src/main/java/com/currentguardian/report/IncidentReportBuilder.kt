package com.currentguardian.report

import com.currentguardian.blackbox.DualBlackBox
import com.currentguardian.crash.ExitClassification
import java.io.File

class IncidentReportBuilder(
    private val blackBox:
        DualBlackBox
) {

    fun build(
        packageName: String?,
        appLabel: String?,
        classification:
            ExitClassification,
        finalEvidence: String,
        crashLogAvailable: Boolean,
        systemTerminationEvidence:
            Boolean,
        anrEvidence: Boolean,
        recoveryAttempted: Boolean,
        recoverySucceeded: Boolean
    ): File? {

        return try {

            val file =
                blackBox
                    .preserveIncident(
                        classification =
                            classification.name,

                        finalState =
                            finalEvidence
                    )
                    ?: return null

            val report =
                File(
                    file.parentFile,
                    file.nameWithoutExtension +
                        "_report.txt"
                )

            report.bufferedWriter().use {

                writer ->

                writer.appendLine(
                    "當前應用效能管家"
                )

                writer.appendLine(
                    "事故報告"
                )

                writer.appendLine(
                    "======================"
                )

                writer.appendLine(
                    "App=" +
                        (
                            appLabel
                                ?: "UNKNOWN"
                        )
                )

                writer.appendLine(
                    "Package=" +
                        (
                            packageName
                                ?: "UNKNOWN"
                        )
                )

                writer.appendLine(
                    "Classification=" +
                        classification.name
                )

                writer.appendLine(
                    "CrashLogAvailable=" +
                        crashLogAvailable
                )

                writer.appendLine(
                    "SystemTerminationEvidence=" +
                        systemTerminationEvidence
                )

                writer.appendLine(
                    "ANREvidence=" +
                        anrEvidence
                )

                writer.appendLine(
                    "RecoveryAttempted=" +
                        recoveryAttempted
                )

                writer.appendLine(
                    "RecoverySucceeded=" +
                        recoverySucceeded
                )

                writer.appendLine(
                    "EvidenceConfidence=" +
                        confidence(
                            classification,
                            crashLogAvailable,
                            systemTerminationEvidence,
                            anrEvidence
                        )
                )

                writer.appendLine()

                writer.appendLine(
                    "FINAL_EVIDENCE"
                )

                writer.appendLine(
                    finalEvidence
                )

                writer.appendLine()

                writer.appendLine(
                    "結論"
                )

                writer.appendLine(
                    conclusion(
                        classification
                    )
                )

                writer.appendLine(
                    "======================"
                )
            }

            report

        } catch (_: Exception) {

            null
        }
    }

    private fun confidence(
        classification:
            ExitClassification,

        crashLogAvailable:
            Boolean,

        systemTerminationEvidence:
            Boolean,

        anrEvidence:
            Boolean
    ): String {

        return when {

            classification ==
                ExitClassification.CRASH_CONFIRMED &&
                crashLogAvailable ->
                "HIGH"

            anrEvidence ->
                "MEDIUM"

            systemTerminationEvidence ->
                "MEDIUM"

            classification ==
                ExitClassification.UNKNOWN ->
                "LOW"

            else ->
                "MEDIUM"
        }
    }

    private fun conclusion(
        classification:
            ExitClassification
    ): String {

        return when (classification) {

            ExitClassification.NORMAL_EXIT ->
                "已觀測到正常退出。"

            ExitClassification.CRASH_CONFIRMED ->
                "已取得足夠證據確認 Crash。"

            ExitClassification.ANR_SUSPECTED ->
                "疑似 ANR，仍需更多證據確認。"

            ExitClassification.SYSTEM_TERMINATION_SUSPECTED ->
                "疑似系統／OEM 終止，但不能僅依此判定。"

            ExitClassification.LOW_MEMORY_SUSPECTED ->
                "事故前存在低記憶體相關跡象，不能直接視為根因。"

            ExitClassification.THERMAL_SUSPECTED ->
                "事故前存在高溫／熱壓力跡象，不能直接視為根因。"

            ExitClassification.NETWORK_RELATED_SUSPECTED ->
                "事故前存在網路異常，不能直接視為根因。"

            ExitClassification.PROCESS_DISAPPEARED ->
                "偵測到程序消失，但事故終止原因尚未確認。"

            ExitClassification.ACTIVITY_DISAPPEARED ->
                "偵測到 Activity 消失，但無法單獨據此確認 Crash。"

            ExitClassification.UNEXPECTED_EXIT ->
                "偵測到非預期退出。"

            ExitClassification.UNKNOWN ->
                "事故終止瞬間無法取得足夠證據。"
        }
    }
}
