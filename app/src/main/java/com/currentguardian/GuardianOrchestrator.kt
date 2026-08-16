package com.currentguardian

import android.content.Context
import com.currentguardian.capability.CapabilityInspector
import com.currentguardian.model.GuardianMode
import com.currentguardian.model.RiskState
import com.currentguardian.monitor.PerformanceMonitorCoordinator
import com.currentguardian.monitor.SystemSnapshot
import com.currentguardian.monitor.UniversalEventTracker
import com.currentguardian.blackbox.DualEvidenceVault
import com.currentguardian.session.AppSession
import com.currentguardian.optimization.OptimizationDecision

class GuardianOrchestrator(
    private val context: Context
) {

    private val capabilityInspector =
        CapabilityInspector(
            context
        )

    private val eventTracker =
        UniversalEventTracker(
            createPhaseBlackBoxAdapter()
        )

    private val performanceCoordinator =
        PerformanceMonitorCoordinator(
            context,
            createPhaseBlackBoxAdapter()
        )

    private val evidence =
        DualEvidenceVault(
            context
        )

    private var session:
        AppSession? =
        null

    private var requestedMode =
        GuardianMode.BALANCED

    fun setRequestedMode(
        mode: GuardianMode
    ) {

        requestedMode =
            mode

        evidence.recordGuardian(
            "MODE_REQUESTED|" +
                mode.name
        )
    }

    fun startApp(
        packageName: String,
        appLabel: String
    ) {

        session =
            AppSession(
                packageName =
                    packageName,

                appLabel =
                    appLabel
            )

        eventTracker.updateApp(
            packageName =
                packageName,

            appLabel =
                appLabel
        )

        eventTracker.markUserEvent(
            label =
                "SESSION_STARTED",

            detail =
                "package=$packageName"
        )

        evidence.recordGuardian(
            "SESSION_STARTED" +
                "|package=" +
                packageName +
                "|time=" +
                System.currentTimeMillis()
        )
    }

    fun tick() {

        val current =
            session
                ?: return

        val currentApp =
            CurrentAppDetector(
                context
            ).getCurrentApp()

        if (
            currentApp.packageName !=
                current.packageName
        ) {

            evidence.recordGuardian(
                "TARGET_CHANGED" +
                    "|from=" +
                    current.packageName +
                    "|to=" +
                    (
                        currentApp.packageName
                            ?: "UNKNOWN"
                    )
            )

            return
        }

        val state =
            SystemSnapshot.collect(
                context
            )

        performanceCoordinator.updateNetwork(
            type =
                state.networkType,

            validated =
                state.networkValidated
        )

        performanceCoordinator.sample(
            appInForeground = true
        )

        evidence.recordTarget(
            buildTargetSnapshot(
                current,
                state
            )
        )

        evidence.recordGuardian(
            "GUARDIAN_TICK" +
                "|package=" +
                current.packageName +
                "|time=" +
                System.currentTimeMillis()
        )

        applyAdaptiveDecision(
            current,
            state
        )
    }

    private fun applyAdaptiveDecision(
        app:
            AppSession,

        snapshot:
            SystemSnapshot
    ) {

        val risk =
            estimateRisk(snapshot)

        val decision =
            OptimizationDecision.decide(
                requestedMode =
                    requestedMode,

                risk =
                    risk,

                temperatureC =
                    snapshot.temperatureC,

                availableRamMb =
                    snapshot.availableRamMb,

                appForeground =
                    true
            )

        app.mode =
            decision.mode

        app.markEvent(
            "MODE=" +
                decision.mode.name
        )

        evidence.recordGuardian(
            "OPTIMIZATION_DECISION" +
                "|mode=" +
                decision.mode.name +
                "|interval=" +
                decision.monitoringIntervalMs +
                "|crash_guard=" +
                decision.enableCrashProtection +
                "|recovery=" +
                decision.enableRecovery +
                "|reason=" +
                decision.reason
        )
    }

    private fun estimateRisk(
        snapshot:
            SystemSnapshot
    ): RiskState {

        var score = 0
        val reasons =
            mutableListOf<String>()

        if (
            snapshot.availableRamMb <=
                512
        ) {

            score += 3
            reasons.add(
                "LOW_RAM"
            )
        }

        if (
            snapshot.temperatureC >=
                45f
        ) {

            score += 3
            reasons.add(
                "HIGH_TEMPERATURE"
            )
        }

        if (
            !snapshot.networkValidated
        ) {

            score += 1
            reasons.add(
                "NETWORK_UNVALIDATED"
            )
        }

        val level =
            when {

                score >= 6 ->
                    RiskState.Level.CRITICAL

                score >= 3 ->
                    RiskState.Level.WARNING

                score >= 1 ->
                    RiskState.Level.WATCH

                else ->
                    RiskState.Level.NORMAL
            }

        return RiskState(
            score =
                score,

            level =
                level,

            reasons =
                reasons
        )
    }

    private fun buildTargetSnapshot(
        session:
            AppSession,

        snapshot:
            SystemSnapshot
    ): String {

        return "TARGET_SNAPSHOT" +
            "|package=" +
            session.packageName +
            "|app=" +
            session.appLabel +
            "|mode=" +
            session.mode.name +
            "|cpu=" +
            snapshot.cpuLoad +
            "|ram=" +
            snapshot.availableRamMb +
            "|temperature=" +
            snapshot.temperatureC +
            "|battery=" +
            snapshot.batteryPercent +
            "|network=" +
            snapshot.networkType +
            "|validated=" +
            snapshot.networkValidated +
            "|time=" +
            System.currentTimeMillis()
    }

    fun stopApp(
        normal: Boolean
    ) {

        val current =
            session
                ?: return

        evidence.recordGuardian(
            "SESSION_END" +
                "|package=" +
                current.packageName +
                "|normal=" +
                normal +
                "|runtime_ms=" +
                current.runtimeMs()
        )

        session =
            null
    }

    fun capabilities():
        com.currentguardian.model
            .GuardianCapabilities {

        return capabilityInspector
            .inspect()
    }

    private fun createPhaseBlackBoxAdapter():
        com.currentguardian.blackbox
            .IncidentManager {

        /*
         * 目前 P1-P6 使用的 IncidentManager
         * 是舊核心相容層。
         *
         * P7 最終整合時，正式版本應逐步
         * 將事件寫入 DualEvidenceVault，
         * 避免同一份資料維護兩套黑盒。
         */

        return com.currentguardian
            .blackbox.IncidentManager(
                context
            )
    }
}
