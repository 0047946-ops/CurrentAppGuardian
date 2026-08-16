package com.currentguardian.watchdog

import com.currentguardian.model.AppDeathState

class AppExitClassifier {

    fun classify(
        processObserved: Boolean,
        foregroundObserved: Boolean,
        elapsedSinceLastSeenMs: Long,
        wasBackgroundedRecently: Boolean,
        criticalRiskBeforeExit: Boolean
    ): AppDeathState {

        if (processObserved) {

            return if (foregroundObserved) {

                AppDeathState.ALIVE_FOREGROUND

            } else {

                AppDeathState.ALIVE_BACKGROUND
            }
        }

        if (
            wasBackgroundedRecently &&
            elapsedSinceLastSeenMs < 5000L
        ) {

            return AppDeathState.TEMPORARILY_NOT_VISIBLE
        }

        if (
            criticalRiskBeforeExit &&
            elapsedSinceLastSeenMs >= 1500L
        ) {

            return AppDeathState.SUSPECTED_CRASH
        }

        if (
            elapsedSinceLastSeenMs >= 5000L
        ) {

            return AppDeathState.CONFIRMED_DISAPPEARED
        }

        return AppDeathState.SUSPECTED_EXIT
    }
}
