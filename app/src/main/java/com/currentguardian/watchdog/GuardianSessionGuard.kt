package com.currentguardian.watchdog

import android.content.Context
import com.currentguardian.blackbox.DualBlackBox

class GuardianSessionGuard(
    context: Context,
    private val blackBox:
        DualBlackBox
) {

    private val stateFile =
        java.io.File(
            context.filesDir,
            "guardian_session.state"
        )

    fun begin() {

        try {

            stateFile.writeText(
                buildString {

                    append(
                        "STARTED\n"
                    )

                    append(
                        "TIME="
                    )

                    append(
                        System.currentTimeMillis()
                    )
                }
            )

            blackBox.record(
                "GUARDIAN_SESSION_STARTED"
            )

        } catch (_: Exception) {
        }
    }

    fun endNormally() {

        try {

            stateFile.writeText(
                buildString {

                    append(
                        "ENDED_NORMALLY\n"
                    )

                    append(
                        "TIME="
                    )

                    append(
                        System.currentTimeMillis()
                    )
                }
            )

            blackBox.record(
                "GUARDIAN_SESSION_ENDED"
            )

        } catch (_: Exception) {
        }
    }

    fun previousSessionAbnormal():
        Boolean {

        return try {

            if (!stateFile.exists()) {
                false

            } else {

                val text =
                    stateFile.readText()

                text.contains(
                    "STARTED"
                ) &&
                !text.contains(
                    "ENDED_NORMALLY"
                )
            }

        } catch (_: Exception) {

            false
        }
    }
}
