package com.currentguardian

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.currentguardian.model.CurrentAppInfo

class CurrentAppDetector(
    private val context: Context
) {

    private val usageStatsManager =
        context.getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

    fun hasUsageAccess(): Boolean {

        return try {

            val appOps =
                context.getSystemService(
                    Context.APP_OPS_SERVICE
                ) as AppOpsManager

            val mode =
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )

            mode ==
                AppOpsManager.MODE_ALLOWED

        } catch (_: Exception) {

            false
        }
    }

    fun detect(
        lookbackMs: Long = 15_000L
    ): CurrentAppInfo? {

        if (!hasUsageAccess()) {
            return null
        }

        val now =
            System.currentTimeMillis()

        val begin =
            now -
                lookbackMs

        val events =
            usageStatsManager.queryEvents(
                begin,
                now
            )

        val event =
            UsageEvents.Event()

        var latestPackage:
            String? = null

        var latestTime =
            0L

        while (
            events.hasNextEvent()
        ) {

            events.getNextEvent(
                event
            )

            val isForegroundEvent =
                isForegroundEvent(
                    event.eventType
                )

            if (!isForegroundEvent) {
                continue
            }

            val packageName =
                event.packageName
                    ?: continue

            if (
                packageName ==
                context.packageName
            ) {
                continue
            }

            if (
                event.timeStamp >=
                latestTime
            ) {

                latestTime =
                    event.timeStamp

                latestPackage =
                    packageName
            }
        }

        val packageName =
            latestPackage
                ?: return fallbackFromStats(
                    begin,
                    now
                )

        return createInfo(
            packageName =
                packageName,

            detectedAt =
                latestTime,

            source =
                CurrentAppInfo.Source
                    .USAGE_EVENT
        )
    }

    private fun fallbackFromStats(
        begin: Long,
        end: Long
    ): CurrentAppInfo? {

        val stats =
            usageStatsManager
                .queryAndAggregateUsageStats(
                    begin,
                    end
                )

        val candidate =
            stats.values
                .asSequence()
                .filter {
                    it.packageName !=
                        context.packageName
                }
                .maxByOrNull {
                    it.lastTimeUsed
                }
                ?: return null

        if (
            candidate.packageName.isBlank()
        ) {
            return null
        }

        return createInfo(
            packageName =
                candidate.packageName,

            detectedAt =
                candidate.lastTimeUsed,

            source =
                CurrentAppInfo.Source
                    .USAGE_STAT
        )
    }

    private fun isForegroundEvent(
        eventType: Int
    ): Boolean {

        return when {

            eventType ==
                UsageEvents.Event
                    .ACTIVITY_RESUMED ->
                true

            eventType ==
                UsageEvents.Event
                    .MOVE_TO_FOREGROUND ->
                true

            else ->
                false
        }
    }

    private fun createInfo(
        packageName: String,
        detectedAt: Long,
        source: CurrentAppInfo.Source
    ): CurrentAppInfo {

        val label =
            try {

                val applicationInfo =
                    context.packageManager
                        .getApplicationInfo(
                            packageName,
                            PackageManager
                                .ApplicationInfoFlags
                                .of(0)
                        )

                context.packageManager
                    .getApplicationLabel(
                        applicationInfo
                    )
                    .toString()

            } catch (_: Exception) {

                packageName
            }

        return CurrentAppInfo(
            packageName =
                packageName,

            label =
                label,

            detectedAt =
                detectedAt,

            source =
                source
        )
    }
}
