package com.currentguardian.monitor

import android.content.Context
import com.currentguardian.blackbox.IncidentManager
import com.currentguardian.model.RiskState
import com.currentguardian.network.NetworkQualityMonitor
import com.currentguardian.network.NetworkTransitionDetector

class PerformanceMonitorCoordinator(
    context: Context,
    incidentManager: IncidentManager
) {

    private val sampler =
        PerformanceSampler(
            context =
                context,

            incidentManager =
                incidentManager
        )

    private val networkQuality =
        NetworkQualityMonitor()

    private val networkTransition =
        NetworkTransitionDetector()

    private val sampling =
        AdaptiveSamplingController()

    private val riskAnalyzer =
        UniversalRiskAnalyzer()

    private var currentNetwork =
        "UNKNOWN"

    private var networkValidated =
        false

    fun updateNetwork(
        type: String,
        validated: Boolean
    ) {

        currentNetwork =
            type

        networkValidated =
            validated

        val transition =
            networkTransition.update(
                type
            )

        when (
            transition
        ) {

            is NetworkTransitionDetector
                .Transition.CHANGED -> {

                incidentManager.recordRaw(
                    "NETWORK_TRANSITION" +
                        "|from=" +
                        transition.from +
                        "|to=" +
                        transition.to +
                        "|time=" +
                        System.currentTimeMillis()
                )
            }

            NetworkTransitionDetector
                .Transition.NONE -> {
            }
        }
    }

    fun sample(
        appInForeground: Boolean
    ) {

        val latencyData =
            networkQuality.measure()

        val latency =
            latencyData.first

        val jitter =
            latencyData.second

        val snapshot =
            sampler.sample(
                networkType =
                    currentNetwork,

                networkValidated =
                    networkValidated,

                latencyMs =
                    latency,

                jitterMs =
                    jitter
            )

        val risk =
            riskAnalyzer.analyze(
                cpu =
                    snapshot.cpuLoadPercent,

                availableRamMb =
                    snapshot.availableRamMb,

                temperatureC =
                    snapshot.temperatureC,

                batteryPercent =
                    snapshot.batteryPercent,

                networkType =
                    snapshot.networkType,

                networkValidated =
                    snapshot.networkValidated
            )

        sampling.update(
            riskState =
                risk,

            appInForeground =
                appInForeground
        )

        incidentManager.recordRaw(
            "RISK_RESULT" +
                "|score=" +
                risk.score +
                "|level=" +
                risk.level.name +
                "|reasons=" +
                risk.reasons.joinToString(",")
        )
    }

    fun currentIntervalMs():
        Long {

        return sampling.intervalMs()
    }

    fun currentMode():
        AdaptiveSamplingController.Mode {

        return sampling.mode
    }

    fun reset() {

        networkQuality.reset()

        networkTransition.reset()

        riskAnalyzer.reset()
    }
}
