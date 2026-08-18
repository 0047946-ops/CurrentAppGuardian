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
        ) as? UsageStatsManager

    fun hasUsageAccess(): Boolean {

        return try {

            val appOps =
                context.getSystemService(
                    Context.APP_OPS_SERVICE
                ) as? AppOpsManager
                    ?: return false

            val mode =
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )

            mode ==
                AppOpsManager.MODE_ALLOWED

        } catch (_: Throwable) {

            false
        }
    }

    fun detect(
        lookbackMs: Long = 15_000L
    ): CurrentAppInfo? {

        if (!hasUsageAccess()) {
            return null
        }

        val manager =
            usageStatsManager
                ?: return null

        return try {

            val now =
                System.currentTimeMillis()

            val safeLookback =
                lookbackMs
                    .coerceIn(
                        1_000L,
                        120_000L
                    )

            val begin =
                now -
                    safeLookback

            val events =
                manager.queryEvents(
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

                if (
                    !events.getNextEvent(
                        event
                    )
                ) {
                    break
                }

                if (
                    !isForegroundEvent(
                        event.eventType
                    )
                ) {
                    continue
                }

                val packageName =
                    event.packageName
                        ?.takeIf {
                            it.isNotBlank()
                        }
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

            if (
                packageName != null &&
                latestTime > 0L
            ) {

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

            fallbackFromStats(
                begin,
                now
            )

        } catch (_: SecurityException) {

            null

        } catch (_: IllegalArgumentException) {

            null

        } catch (_: Throwable) {

            null
        }
    }

    private fun fallbackFromStats(
        begin: Long,
        end: Long
    ): CurrentAppInfo? {

        val manager =
            usageStatsManager
                ?: return null

        return try {

            val stats =
                manager.queryAndAggregateUsageStats(
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
                    .filter {
                        it.packageName.isNotBlank()
                    }
                    .maxByOrNull {
                        it.lastTimeUsed
                    }
                    ?: return null

            if (
                candidate.lastTimeUsed <= 0L
            ) {
                return null
            }

            createInfo(
                packageName =
                    candidate.packageName,

                detectedAt =
                    candidate.lastTimeUsed,

                source =
                    CurrentAppInfo.Source
                        .USAGE_STAT
            )

        } catch (_: SecurityException) {

            null

        } catch (_: IllegalArgumentException) {

            null

        } catch (_: Throwable) {

            null
        }
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

        val safePackageName =
            packageName
                .trim()

        val label =
            if (
                safePackageName.isBlank()
            ) {

                "未知 App"

            } else {

                try {

                    // Android 11 相容寫法。
                    // 不使用 API 33+ 的 ApplicationInfoFlags。
                    val applicationInfo =
                        context.packageManager
                            .getApplicationInfo(
                                safePackageName,
                                0
                            )

                    context.packageManager
                        .getApplicationLabel(
                            applicationInfo
                        )
                        ?.toString()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: safePackageName

                } catch (_: PackageManager.NameNotFoundException) {

                    safePackageName

                } catch (_: Throwable) {

                    safePackageName
                }
            }

        return CurrentAppInfo(
            packageName =
                safePackageName,

            label =
                label,

            detectedAt =
                if (
                    detectedAt > 0L
                ) {
                    detectedAt
                } else {
                    System.currentTimeMillis()
                },

            source =
                source
        )
    }
}
