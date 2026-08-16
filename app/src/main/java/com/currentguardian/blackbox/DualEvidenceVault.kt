package com.currentguardian.blackbox

import android.content.Context
import java.io.File

class DualEvidenceVault(
    context: Context
) {

    private val root =
        File(
            context.filesDir,
            "guardian_evidence"
        ).apply {
            mkdirs()
        }

    private val targetBlackBox =
        DualBlackBox(
            File(
                root,
                "target_app"
            )
        )

    private val guardianBlackBox =
        DualBlackBox(
            File(
                root,
                "guardian"
            )
        )

    fun recordTarget(
        record: String
    ) {

        targetBlackBox.record(
            record
        )
    }

    fun recordGuardian(
        record: String
    ) {

        guardianBlackBox.record(
            record
        )
    }

    fun preserveTargetIncident(
        classification: String,
        finalState: String
    ): File? {

        return targetBlackBox
            .preserveIncident(
                classification,
                finalState
            )
    }

    fun preserveGuardianIncident(
        reason: String,
        finalState: String
    ): File? {

        return guardianBlackBox
            .preserveIncident(
                reason,
                finalState
            )
    }

    fun targetRoot():
        File {

        return File(
            root,
            "target_app"
        )
    }

    fun guardianRoot():
        File {

        return File(
            root,
            "guardian"
        )
    }
}
