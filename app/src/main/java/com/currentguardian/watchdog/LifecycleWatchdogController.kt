package com.currentguardian.watchdog

import android.content.Context
import com.currentguardian.blackbox.IncidentManager
import com.currentguardian.model.AppDeathState
import com.currentguardian.monitor.UniversalEventTracker

class LifecycleWatchdogController(
    context: Context,
    incidentManager: IncidentManager,
    eventTracker: UniversalEventTracker
) {

    private val presenceMonitor =
        AppPresenceMonitor(context)

    private val watchdog =
        AppDeathWatchdog(
            context =
                context,

            incidentManager =
                incidentManager,

            eventTracker =
                eventTracker
        )

    private val classifier =
        AppExitClassifier()

    private val poller =
        WatchdogPoller(
            intervalMs = 1000L
        ) {
            pollTarget()
        }

    fun start(
        packageName: String,
        appLabel: String?
    ) {

        watchdog.start(
            packageName =
                packageName,

            appLabel =
                appLabel
        )

        poller.start()
    }

    fun stop() {

        poller.stop()

        watchdog.stop()
    }

    private fun pollTarget() {

        val packageName =
            watchdog.getTargetPackage()
                ?: return

        val processObserved =
            presenceMonitor
                .isProcessObserved(
                    packageName
                )

        val foregroundObserved =
            presenceMonitor
                .isForegroundObserved(
                    packageName
                )

        if (processObserved) {

            watchdog.reportProcessObserved(
                foreground =
                    foregroundObserved
            )

            return
        }

        val currentState =
            watchdog.evaluateDisappearance(
                processObserved =
                    false,

                foregroundObserved =
                    false
            )

        if (
            currentState ==
                AppDeathState.SUSPECTED_EXIT
        ) {

            confirmAfterDelay()
        }
    }

    private fun confirmAfterDelay() {

        val packageName =
            watchdog.getTargetPackage()
                ?: return

        val stillRunning =
            presenceMonitor
                .isProcessObserved(
                    packageName
                )

        if (!stillRunning) {

            watchdog.confirmDisappearance(
                processObserved =
                    false
            )
        }
    }

    fun getCurrentState():
        AppDeathState {

        return watchdog.getState()
    }
}
