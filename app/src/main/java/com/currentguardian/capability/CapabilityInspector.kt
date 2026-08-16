package com.currentguardian.capability

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.os.PowerManager
import com.currentguardian.model.GuardianCapabilities

class CapabilityInspector(
    private val context: Context
) {

    fun inspect():
        GuardianCapabilities {

        return GuardianCapabilities(

            usageAccess =
                hasUsageAccess(),

            notifications =
                hasNotificationPermission(),

            foregroundServiceAvailable =
                true,

            backgroundRestricted =
                isBackgroundRestricted(),

            canObserveCurrentApp =
                hasUsageAccess(),

            canReadBatteryState =
                true,

            canReadNetworkState =
                true,

            canPersistStorage =
                context.filesDir.exists(),

            batteryOptimizationIgnored =
                isIgnoringBatteryOptimization()
        )
    }

    private fun hasUsageAccess():
        Boolean {

        return try {

            val appOps =
                context.getSystemService(
                    Context.APP_OPS_SERVICE
                ) as AppOpsManager

            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            ) ==
                AppOpsManager.MODE_ALLOWED

        } catch (_: Exception) {

            false
        }
    }

    private fun hasNotificationPermission():
        Boolean {

        return if (
            android.os.Build.VERSION.SDK_INT >= 33
        ) {

            context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) ==
                android.content.pm.PackageManager
                    .PERMISSION_GRANTED

        } else {

            val manager =
                context.getSystemService(
                    NotificationManager::class.java
                )

            manager.areNotificationsEnabled()
        }
    }

    private fun isBackgroundRestricted():
        Boolean {

        return try {

            val manager =
                context.getSystemService(
                    Context.ACTIVITY_SERVICE
                ) as android.app.ActivityManager

            manager.isBackgroundRestricted

        } catch (_: Exception) {

            false
        }
    }

    private fun isIgnoringBatteryOptimization():
        Boolean {

        return try {

            val power =
                context.getSystemService(
                    Context.POWER_SERVICE
                ) as PowerManager

            power.isIgnoringBatteryOptimizations(
                context.packageName
            )

        } catch (_: Exception) {

            false
        }
    }
}
