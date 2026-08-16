package com.currentguardian.monitor

import com.currentguardian.blackbox.IncidentManager
import com.currentguardian.model.AppLifecycleState
import com.currentguardian.model.GuardianEventType
import com.currentguardian.model.UniversalEvent

class UniversalEventTracker(
    private val incidentManager: IncidentManager
) {

    private var currentPackage: String? = null

    private var currentAppLabel: String? = null

    private var lifecycle =
        AppLifecycleState.UNKNOWN

    private var lastEvent =
        GuardianEventType.GUARDIAN_STARTED

    private var lastEventTime =
        System.currentTimeMillis()

    fun updateApp(
        packageName: String?,
        appLabel: String?
    ) {

        currentPackage =
            packageName

        currentAppLabel =
            appLabel

        emit(
            type =
                GuardianEventType.APP_DETECTED,

            detail =
                "Application detected"
        )
    }

    fun setLifecycle(
        newState: AppLifecycleState,
        detail: String = ""
    ) {

        if (lifecycle == newState) {
            return
        }

        lifecycle =
            newState

        emit(
            type =
                when (newState) {

                    AppLifecycleState.FOREGROUND ->
                        GuardianEventType.APP_FOREGROUND

                    AppLifecycleState.BACKGROUND ->
                        GuardianEventType.APP_BACKGROUND

                    AppLifecycleState.RETURNED_TO_FOREGROUND ->
                        GuardianEventType.APP_RETURNED_FOREGROUND

                    AppLifecycleState.DISAPPEARED ->
                        GuardianEventType.APP_DISAPPEARED

                    AppLifecycleState.EXITED ->
                        GuardianEventType.NORMAL_EXIT_SUSPECTED

                    else ->
                        GuardianEventType.APP_CHANGED
                },

            detail = detail
        )
    }

    fun markUserEvent(
        label: String,
        detail: String = ""
    ) {

        emit(
            type =
                GuardianEventType.USER_MARK,

            detail =
                "label=$label;detail=$detail",

            severity = 1
        )
    }

    fun screenChanged(
        name: String,
        detail: String = ""
    ) {

        emit(
            type =
                GuardianEventType.SCREEN_CHANGE,

            detail =
                "screen=$name;$detail"
        )
    }

    fun loadingStarted(
        detail: String = ""
    ) {

        emit(
            type =
                GuardianEventType.LOADING_STARTED,

            detail = detail
        )
    }

    fun loadingFinished(
        detail: String = ""
    ) {

        emit(
            type =
                GuardianEventType.LOADING_FINISHED,

            detail = detail
        )
    }

    fun networkChanged(
        network: String,
        detail: String = ""
    ) {

        emit(
            type =
                GuardianEventType.NETWORK_CHANGED,

            detail =
                "network=$network;$detail"
        )
    }

    fun networkDegraded(
        detail: String
    ) {

        emit(
            type =
                GuardianEventType.NETWORK_DEGRADED,

            detail = detail,

            severity = 1
        )
    }

    fun networkRecovered(
        detail: String
    ) {

        emit(
            type =
                GuardianEventType.NETWORK_RECOVERED,

            detail = detail
        )
    }

    fun resourceWarning(
        detail: String
    ) {

        emit(
            type =
                GuardianEventType.RESOURCE_WARNING,

            detail = detail,

            severity = 2
        )
    }

    fun resourceCritical(
        detail: String
    ) {

        emit(
            type =
                GuardianEventType.RESOURCE_CRITICAL,

            detail = detail,

            severity = 3
        )
    }

    fun riskChanged(
        score: Int,
        level: String,
        reasons: List<String>
    ) {

        emit(
            type =
                GuardianEventType.RISK_CHANGED,

            detail =
                "score=$score;" +
                "level=$level;" +
                "reasons=" +
                reasons.joinToString(","),

            severity =
                when (level) {

                    "CRITICAL" -> 3
                    "WARNING" -> 2
                    "WATCH" -> 1
                    else -> 0
                }
        )
    }

    private fun emit(
        type: GuardianEventType,
        detail: String,
        severity: Int = 0
    ) {

        val now =
            System.currentTimeMillis()

        val elapsed =
            now - lastEventTime

        val event =
            UniversalEvent.create(
                type =
                    type,

                packageName =
                    currentPackage,

                appLabel =
                    currentAppLabel,

                lifecycle =
                    lifecycle,

                detail =
                    "$detail;elapsed_since_previous_event_ms=$elapsed",

                severity =
                    severity
            )

        incidentManager.recordRaw(
            event.serialize()
        )

        lastEvent =
            type

        lastEventTime =
            now
    }

    fun getCurrentPackage():
        String? {

        return currentPackage
    }

    fun getCurrentAppLabel():
        String? {

        return currentAppLabel
    }

    fun getLifecycle():
        AppLifecycleState {

        return lifecycle
    }

    fun getLastEvent():
        GuardianEventType {

        return lastEvent
    }
}
