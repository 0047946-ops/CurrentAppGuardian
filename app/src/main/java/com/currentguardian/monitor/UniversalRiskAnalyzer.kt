package com.currentguardian.monitor

import com.currentguardian.model.RiskState

class UniversalRiskAnalyzer {

    private var previousRam =
        -1L

    private var previousCpu =
        -1

    private var previousTemperature =
        -1f

    private var previousBattery =
        -1

    private var previousNetwork =
        ""

    fun analyze(
        cpu: Int,
        availableRamMb: Long,
        temperatureC: Float,
        batteryPercent: Int,
        networkType: String,
        networkValidated: Boolean
    ): RiskState {

        var score = 0

        val reasons =
            mutableListOf<String>()

        if (previousRam >= 0) {

            val drop =
                previousRam -
                    availableRamMb

            if (drop >= 256) {

                score += 3

                reasons.add(
                    "可用 RAM 快速下降=${drop}MB"
                )
            }
        }

        if (previousCpu >= 0) {

            val rise =
                cpu - previousCpu

            if (rise >= 30) {

                score += 2

                reasons.add(
                    "CPU 負載快速上升=${rise}%"
                )
            }
        }

        if (previousTemperature >= 0f) {

            val rise =
                temperatureC -
                    previousTemperature

            if (rise >= 2f) {

                score += 2

                reasons.add(
                    "溫度快速上升=${rise}C"
                )
            }
        }

        if (
            batteryPercent in 0..10
        ) {

            score += 1

            reasons.add(
                "電池電量偏低"
            )
        }

        if (
            previousNetwork.isNotEmpty() &&
            previousNetwork != networkType
        ) {

            score += 1

            reasons.add(
                "網路類型發生切換"
            )
        }

        if (!networkValidated) {

            score += 1

            reasons.add(
                "目前網路未通過連線驗證"
            )
        }

        previousRam =
            availableRamMb

        previousCpu =
            cpu

        previousTemperature =
            temperatureC

        previousBattery =
            batteryPercent

        previousNetwork =
            networkType

        val level =
            when {

                score >= 8 ->
                    RiskState.Level.CRITICAL

                score >= 5 ->
                    RiskState.Level.WARNING

                score >= 2 ->
                    RiskState.Level.WATCH

                else ->
                    RiskState.Level.NORMAL
            }

        return RiskState(
            score = score,
            level = level,
            reasons = reasons
        )
    }

    fun reset() {

        previousRam = -1L
        previousCpu = -1
        previousTemperature = -1f
        previousBattery = -1
        previousNetwork = ""
    }
}
