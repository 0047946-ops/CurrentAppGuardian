package com.currentguardian.crash

import android.content.Context
import com.currentguardian.blackbox.DualBlackBox
import com.currentguardian.recovery.CrashLoopGuard
import com.currentguardian.recovery.RecoveryManager
import com.currentguardian.report.IncidentReportBuilder
import com.currentguardian.watchdog.GuardianSelfGuard
import com.currentguardian.watchdog.GuardianSessionGuard

class Phase6Controller(
    context: Context
) {

    private val blackBox =
        DualBlackBox(context)

    private val crashGuard =
        CrashGuard(
            blackBox
        )

    private val crashLoopGuard =
        CrashLoopGuard(
            maxAttempts = 3
        )

    private val recoveryManager =
        RecoveryManager(
            context
        )

    private val selfGuard =
        GuardianSelfGuard(
            blackBox
        )

    private val sessionGuard =
        GuardianSessionGuard(
            context,
            blackBox
        )

    private val reportBuilder =
        IncidentReportBuilder(
            blackBox
        )

    fun startGuardianSession() {

        sessionGuard.begin()

        if (
            sessionGuard
                .previousSessionAbnormal()
        ) {

            blackBox.record(
                "GUARDIAN_PREVIOUS_SESSION_ABNORMAL"
            )
        }
    }

    fun heartbeat(
        targetPackage: String?,
        state: String
    ) {

        selfGuard.heartbeat(
            targetPackage =
                targetPackage,

            state =
                state
        )
    }

    fun startTarget(
        packageName: String,
        pid: Int
    ) {

        crashGuard.onTargetStarted(
            packageName =
                packageName,

            pid =
                pid
        )
    }

    fun updateTargetEvidence(
        evidence:
            FinalEvidence
    ) {

        crashGuard.updateHeartbeat(
            evidence
        )
    }

    fun normalExit() {

        crashGuard.markNormalExit()

        crashLoopGuard
            .registerStableRun()
    }

    fun unexpectedExit(
        packageName: String?,
        appLabel: String?,
        classification:
            ExitClassification,
        crashLogAvailable:
            Boolean,
        systemTerminationEvidence:
            Boolean,
        anrEvidence:
            Boolean
    ): Boolean {

        val incidentFile =
            crashGuard
                .handleUnexpectedExit(
                    classification
                )

        val evidence =
            if (
                incidentFile != null
            ) {

                "IncidentFile=" +
                    incidentFile.absolutePath

            } else {

                "NO_INCIDENT_FILE"
            }

        val allowedRecovery =
            crashLoopGuard
                .registerCrash()

        val recoveryAttempted =
            if (
                allowedRecovery &&
                packageName != null
            ) {

                recoveryManager
                    .launchPackage(
                        packageName
                    )

            } else {

                false
            }

        return reportBuilder
            .build(
                packageName =
                    packageName,

                appLabel =
                    appLabel,

                classification =
                    classification,

                finalEvidence =
                    evidence,

                crashLogAvailable =
                    crashLogAvailable,

                systemTerminationEvidence =
                    systemTerminationEvidence,

                anrEvidence =
                    anrEvidence,

                recoveryAttempted =
                    allowedRecovery,

                recoverySucceeded =
                    recoveryAttempted
            ) != null
    }

    fun endGuardianSession() {

        sessionGuard
            .endNormally()
    }

    fun incidentDirectory():
        java.io.File {

        return blackBox.rootDirectory()
    }
}
