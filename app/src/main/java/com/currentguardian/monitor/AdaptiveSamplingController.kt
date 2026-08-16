package com.currentguardian.monitor

import com.currentguardian.model.RiskState

class AdaptiveSamplingController {

    enum class Mode {
        SILENT,
        NORMAL,
        ACTIVE,
        CRITICAL
    }

    var mode =
        Mode.SILENT
        private set

    fun update(
        riskState: RiskState,
        appInForeground: Boolean
    ) {

        mode = when {

            riskState.level ==
                RiskState.Level.CRITICAL ->
                Mode.CRITICAL

            riskState.level ==
                RiskState.Level.WARNING ->
                Mode.ACTIVE

            appInForeground ->
                Mode.NORMAL

            else ->
                Mode.SILENT
        }
    }

    fun intervalMs(): Long {

        return when (mode) {

            Mode.SILENT ->
                5000L

            Mode.NORMAL ->
                2000L

            Mode.ACTIVE ->
                1000L

            Mode.CRITICAL ->
                500L
        }
    }

    fun isAggressive():
        Boolean {

        return mode ==
            Mode.CRITICAL
    }
}
