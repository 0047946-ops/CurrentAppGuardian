package com.currentguardian.optimization

import com.currentguardian.model.GuardianMode
import com.currentguardian.model.RiskState

data class OptimizationDecision(

    val mode: GuardianMode,

    val monitoringIntervalMs: Long,

    val enableAggressiveSampling: Boolean,

    val enableCrashProtection: Boolean,

    val enableRecovery: Boolean,

    val reason: String
) {

    companion object {

        fun decide(
            requestedMode: GuardianMode,
            risk: RiskState,
            temperatureC: Float,
            availableRamMb: Long,
            appForeground: Boolean
        ): OptimizationDecision {

            if (
                temperatureC >= 47f
            ) {

                return OptimizationDecision(

                    mode =
                        GuardianMode.PROTECTION,

                    monitoringIntervalMs =
                        5000L,

                    enableAggressiveSampling =
                        false,

                    enableCrashProtection =
                        true,

                    enableRecovery =
                        false,

                    reason =
                        "高溫，優先降低管家自身負載"
                )
            }

            if (
                availableRamMb <= 512
            ) {

                return OptimizationDecision(

                    mode =
                        GuardianMode.PROTECTION,

                    monitoringIntervalMs =
                        3000L,

                    enableAggressiveSampling =
                        false,

                    enableCrashProtection =
                        true,

                    enableRecovery =
                        false,

                    reason =
                        "可用 RAM 偏低"
                )
            }

            if (
                risk.level ==
                    RiskState.Level.CRITICAL
            ) {

                return OptimizationDecision(

                    mode =
                        GuardianMode.PROTECTION,

                    monitoringIntervalMs =
                        500L,

                    enableAggressiveSampling =
                        true,

                    enableCrashProtection =
                        true,

                    enableRecovery =
                        false,

                    reason =
                        "事故風險 CRITICAL"
                )
            }

            if (
                !appForeground
            ) {

                return OptimizationDecision(

                    mode =
                        GuardianMode.LOW_POWER,

                    monitoringIntervalMs =
                        5000L,

                    enableAggressiveSampling =
                        false,

                    enableCrashProtection =
                        true,

                    enableRecovery =
                        false,

                    reason =
                        "目標 App 不在前景"
                )
            }

            return when (requestedMode) {

                GuardianMode.LOW_POWER ->
                    OptimizationDecision(
                        mode =
                            GuardianMode.LOW_POWER,

                        monitoringIntervalMs =
                            5000L,

                        enableAggressiveSampling =
                            false,

                        enableCrashProtection =
                            true,

                        enableRecovery =
                            false,

                        reason =
                            "使用者低功耗模式"
                    )

                GuardianMode.BALANCED ->
                    OptimizationDecision(
                        mode =
                            GuardianMode.BALANCED,

                        monitoringIntervalMs =
                            2000L,

                        enableAggressiveSampling =
                            false,

                        enableCrashProtection =
                            true,

                        enableRecovery =
                            true,

                        reason =
                            "使用者均衡模式"
                    )

                GuardianMode.PERFORMANCE ->
                    OptimizationDecision(
                        mode =
                            GuardianMode.PERFORMANCE,

                        monitoringIntervalMs =
                            1000L,

                        enableAggressiveSampling =
                            true,

                        enableCrashProtection =
                            true,

                        enableRecovery =
                            true,

                        reason =
                            "使用者效能模式"
                    )

                GuardianMode.PROTECTION ->
                    OptimizationDecision(
                        mode =
                            GuardianMode.PROTECTION,

                        monitoringIntervalMs =
                            5000L,

                        enableAggressiveSampling =
                            false,

                        enableCrashProtection =
                            true,

                        enableRecovery =
                            false,

                        reason =
                            "保護模式"
                    )
            }
        }
    }
}
