package com.currentguardian.monitor

import android.app.ActivityManager
import android.content.Context
import com.currentguardian.blackbox.IncidentManager

class EventMonitor(
    private val context: Context,
    private val incidentManager: IncidentManager
) {

    private var lastPackage: String? = null

    fun checkCurrentApp(
        packageName: String?
    ) {

        if (
            packageName.isNullOrBlank()
        ) {
            return
        }

        if (
            lastPackage != null &&
            lastPackage != packageName
        ) {

            incidentManager.mark(
                type = "APP_CHANGED",
                detail =
                    "$lastPackage -> $packageName",
                severity = 1
            )
        }

        lastPackage = packageName
    }

    fun markForeground() {

        incidentManager.mark(
            type = "GUARDIAN_FOREGROUND",
            detail = "Guardian monitoring active"
        )
    }

    fun markBackground() {

        incidentManager.mark(
            type = "GUARDIAN_BACKGROUND",
            detail = "Guardian lifecycle changed",
            severity = 1
        )
    }
}
