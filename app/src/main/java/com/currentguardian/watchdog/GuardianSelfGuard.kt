package com.currentguardian.watchdog

import com.currentguardian.blackbox.DualBlackBox

class GuardianSelfGuard(
    private val blackBox:
        DualBlackBox
) {

    private var lastHeartbeat =
        0L

    fun heartbeat(
        targetPackage:
            String?,
        state:
            String
    ) {

        lastHeartbeat =
            System.currentTimeMillis()

        blackBox.writeHeartbeat(
            buildString {

                append(
                    "time="
                )

                append(
                    lastHeartbeat
                )

                append(
                    "|target="
                )

                append(
                    targetPackage
                        ?.replace("|", "/")
                        ?: "UNKNOWN"
                )

                append(
                    "|state="
                )

                append(
                    state
                        .replace("|", "/")
                )
            }
        )
    }

    fun lastHeartbeat():
        Long {

        return lastHeartbeat
    }
}
