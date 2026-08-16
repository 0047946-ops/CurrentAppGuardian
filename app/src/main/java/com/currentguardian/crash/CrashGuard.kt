package com.currentguardian.crash

import com.currentguardian.blackbox.DualBlackBox

class CrashGuard(
    private val blackBox:
        DualBlackBox
) {

    private var targetRunning =
        false

    private var targetPid =
        -1

    private var lastSeen =
        System.currentTimeMillis()

    private var lastEvidence:
        FinalEvidence? = null

    fun onTargetStarted(
        packageName: String,
        pid: Int
    ) {

        targetRunning =
            true

        targetPid =
            pid

        lastSeen =
            System.currentTimeMillis()

        blackBox.record(
            "GAME_START" +
                "|package=" +
                packageName +
                "|pid=" +
                pid
        )
    }

    fun updateHeartbeat(
        evidence:
            FinalEvidence
    ) {

        if (!targetRunning) {
            return
        }

        lastSeen =
            System.currentTimeMillis()

        lastEvidence =
            evidence

        blackBox.record(
            "TARGET_HEARTBEAT|" +
                evidence.serialize()
        )
    }

    fun markNormalExit() {

        if (!targetRunning) {
            return
        }

        targetRunning =
            false

        blackBox.record(
            "TARGET_NORMAL_EXIT"
        )
    }

    fun handleUnexpectedExit(
        classification:
            ExitClassification
    ): java.io.File? {

        if (!targetRunning) {
            return null
        }

        targetRunning =
            false

        blackBox.record(
            "TARGET_UNEXPECTED_EXIT" +
                "|classification=" +
                classification.name +
                "|pid=" +
                targetPid
        )

        val evidence =
            lastEvidence?.serialize()
                ?: "NO_FINAL_EVIDENCE"

        return blackBox
            .preserveIncident(
                classification =
                    classification.name,

                finalState =
                    evidence
            )
    }

    fun isRunning():
        Boolean {

        return targetRunning
    }

    fun getLastSeen():
        Long {

        return lastSeen
    }

    fun getTargetPid():
        Int {

        return targetPid
    }

    fun reset() {

        targetRunning =
            false

        targetPid =
            -1

        lastEvidence =
            null
    }
}
