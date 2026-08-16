package com.currentguardian.watchdog

import android.content.Context
import android.os.SystemClock
import com.currentguardian.blackbox.IncidentManager
import com.currentguardian.model.AppDeathState
import com.currentguardian.model.AppLifecycleState
import com.currentguardian.model.GuardianEventType
import com.currentguardian.model.LifecycleSnapshot
import com.currentguardian.monitor.UniversalEventTracker

class AppDeathWatchdog(
    private val context: Context,
    private val incidentManager: IncidentManager,
    private val eventTracker: UniversalEventTracker
) {

    private var targetPackage: String? = null
    private var targetAppLabel: String? = null

    private var lastSeenElapsed =
        SystemClock.elapsedRealtime()

    private var lastForegroundElapsed =
        SystemClock.elapsedRealtime()

    private var lastKnownState =
        AppDeathState.UNKNOWN

    private var monitoring =
        false

    fun start(
        packageName: String,
        appLabel: String?
    ) {

        targetPackage =
            packageName

        targetAppLabel =
            appLabel

        lastSeenElapsed =
            SystemClock.elapsedRealtime()

        lastForegroundElapsed =
            SystemClock.elapsedRealtime()

        lastKnownState =
            AppDeathState.ALIVE_FOREGROUND

        monitoring =
            true

        record(
            state =
                AppDeathState.ALIVE_FOREGROUND,

            lifecycle =
                AppLifecycleState.FOREGROUND,

            processObserved = true,

            foregroundObserved = true,

            reason =
                "watchdog_started"
        )
    }

    fun stop() {

        monitoring =
            false
    }

    fun reportForeground() {

        if (!monitoring) {
            return
        }

        lastSeenElapsed =
            SystemClock.elapsedRealtime()

        lastForegroundElapsed =
            lastSeenElapsed

        lastKnownState =
            AppDeathState.ALIVE_FOREGROUND

        record(
            state =
                AppDeathState.ALIVE_FOREGROUND,

            lifecycle =
                AppLifecycleState.FOREGROUND,

            processObserved = true,

            foregroundObserved = true,

            reason =
                "foreground_observed"
        )
    }

    fun reportBackground() {

        if (!monitoring) {
            return
        }

        lastSeenElapsed =
            SystemClock.elapsedRealtime()

        lastKnownState =
            AppDeathState.ALIVE_BACKGROUND

        record(
            state =
                AppDeathState.ALIVE_BACKGROUND,

            lifecycle =
                AppLifecycleState.BACKGROUND,

            processObserved = true,

            foregroundObserved = false,

            reason =
                "background_observed"
        )
    }

    fun reportTemporarilyHidden() {

        if (!monitoring) {
            return
        }

        val now =
            SystemClock.elapsedRealtime()

        val elapsed =
            now - lastSeenElapsed

        lastKnownState =
            AppDeathState.TEMPORARILY_NOT_VISIBLE

        record(
            state =
                AppDeathState.TEMPORARILY_NOT_VISIBLE,

            lifecycle =
                AppLifecycleState.BACKGROUND,

            processObserved = true,

            foregroundObserved = false,

            reason =
                "temporarily_not_visible"
        )

        lastSeenElapsed =
            now
    }

    fun reportProcessObserved(
        foreground: Boolean
    ) {

        if (!monitoring) {
            return
        }

        val now =
            SystemClock.elapsedRealtime()

        lastSeenElapsed =
            now

        if (foreground) {

            lastForegroundElapsed =
                now

            lastKnownState =
                AppDeathState.ALIVE_FOREGROUND

        } else {

            lastKnownState =
                AppDeathState.ALIVE_BACKGROUND
        }

        record(
            state =
                lastKnownState,

            lifecycle =
                if (foreground) {
                    AppLifecycleState.FOREGROUND
                } else {
                    AppLifecycleState.BACKGROUND
                },

            processObserved = true,

            foregroundObserved = foreground,

            reason =
                "process_observed"
        )
    }

    fun evaluateDisappearance(
        processObserved: Boolean,
        foregroundObserved: Boolean
    ): AppDeathState {

        if (!monitoring) {
            return AppDeathState.UNKNOWN
        }

        val now =
            SystemClock.elapsedRealtime()

        val elapsed =
            now - lastSeenElapsed

        if (processObserved) {

            if (foregroundObserved) {

                lastKnownState =
                    AppDeathState.ALIVE_FOREGROUND

            } else {

                lastKnownState =
                    AppDeathState.ALIVE_BACKGROUND
            }

            return lastKnownState
        }

        if (elapsed < 1500L) {

            lastKnownState =
                AppDeathState.TEMPORARILY_NOT_VISIBLE

            return lastKnownState
        }

        lastKnownState =
            AppDeathState.SUSPECTED_EXIT

        record(
            state =
                AppDeathState.SUSPECTED_EXIT,

            lifecycle =
                AppLifecycleState.DISAPPEARED,

            processObserved = false,

            foregroundObserved = false,

            reason =
                "application_temporarily_disappeared"
        )

        return lastKnownState
    }

    fun confirmDisappearance(
        processObserved: Boolean
    ): AppDeathState {

        if (!monitoring) {
            return AppDeathState.UNKNOWN
        }

        if (processObserved) {

            lastKnownState =
                AppDeathState.ALIVE_BACKGROUND

            return lastKnownState
        }

        lastKnownState =
            AppDeathState.CONFIRMED_DISAPPEARED

        eventTracker.markUserEvent(
            label =
                "APP_DISAPPEARED_CONFIRMED",

            detail =
                "package=$targetPackage"
        )

        record(
            state =
                AppDeathState.CONFIRMED_DISAPPEARED,

            lifecycle =
                AppLifecycleState.DISAPPEARED,

            processObserved = false,

            foregroundObserved = false,

            reason =
                "application_disappearance_confirmed"
        )

        incidentManager.recordRaw(
            "APP_DEATH_CONFIRMED" +
                "|package=" +
                (targetPackage ?: "unknown") +
                "|time=" +
                System.currentTimeMillis()
        )

        return lastKnownState
    }

    fun markSuspectedCrash(
        reason: String
    ) {

        if (!monitoring) {
            return
        }

        lastKnownState =
            AppDeathState.SUSPECTED_CRASH

        record(
            state =
                AppDeathState.SUSPECTED_CRASH,

            lifecycle =
                AppLifecycleState.DISAPPEARED,

            processObserved = false,

            foregroundObserved = false,

            reason = reason
        )
    }

    fun markSystemKill(
        reason: String
    ) {

        if (!monitoring) {
            return
        }

        lastKnownState =
            AppDeathState.SUSPECTED_SYSTEM_KILL

        record(
            state =
                AppDeathState.SUSPECTED_SYSTEM_KILL,

            lifecycle =
                AppLifecycleState.DISAPPEARED,

            processObserved = false,

            foregroundObserved = false,

            reason = reason
        )
    }

    private fun record(
        state: AppDeathState,
        lifecycle: AppLifecycleState,
        processObserved: Boolean,
        foregroundObserved: Boolean,
        reason: String
    ) {

        val now =
            SystemClock.elapsedRealtime()

        val elapsed =
            now - lastSeenElapsed

        val snapshot =
            LifecycleSnapshot(
                timestamp =
                    System.currentTimeMillis(),

                packageName =
                    targetPackage,

                appLabel =
                    targetAppLabel,

                lifecycleState =
                    lifecycle,

                deathState =
                    state,

                processObserved =
                    processObserved,

                foregroundObserved =
                    foregroundObserved,

                elapsedSinceLastSeenMs =
                    elapsed,

                reason =
                    reason
            )

        incidentManager.recordRaw(
            snapshot.serialize()
        )
    }

    fun getState():
        AppDeathState {

        return lastKnownState
    }

    fun getTargetPackage():
        String? {

        return targetPackage
    }

    fun getLastSeenElapsed():
        Long {

        return lastSeenElapsed
    }
}
