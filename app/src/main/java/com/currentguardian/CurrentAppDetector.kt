package com.currentguardian

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
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
                lookbackMs.coerceIn(
                    1_000L,
                    120_000L
                )

            val begin =
                now - safeLookback

            val excludedPackages =
                getExcludedPackages()

            val events =
                manager.queryEvents(
                    begin,
                    now
                )

            val event =
                UsageEvents.Event()

            /*
             * 不只記錄最後一個事件。
             *
             * 因為使用者從 Whoscall 回到 CurrentAppGuardian 時，
             * 最近事件可能是：
             *
             * Whoscall → Launcher → CurrentAppGuardian
             *
             * CurrentAppGuardian 本身已排除，
             * 但 Launcher 如果不排除，就會被誤判成目前 App。
             *
             * 因此所有「非目標環境」事件都先排除，
             * 再找最近真正可辨識的 App。
             */
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
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: continue

                if (
                    shouldExcludePackage(
                        packageName,
                        excludedPackages
                    )
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

            /*
             * UsageEvents 沒有合適候選時，
             * 再使用 UsageStats 做第二層 fallback。
             */
            fallbackFromStats(
                begin,
                now,
                excludedPackages
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
        end: Long,
        excludedPackages:
            Set<String>
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
                        it.packageName.isNotBlank()
                    }
                    .filter {
                        !shouldExcludePackage(
                            it.packageName,
                            excludedPackages
                        )
                    }
                    .filter {
                        it.lastTimeUsed > 0L
                    }
                    .maxByOrNull {
                        it.lastTimeUsed
                    }
                    ?: return null

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

    /**
     * 建立「不應該被當成目前目標 App」的 package 集合。
     *
     * 只排除：
     * 1. CG 自己
     * 2. 系統 Home / Launcher
     * 3. Android 設定
     *
     * 不會把所有系統 App 一次排除，
     * 以免違反「全應用」產品方向。
     */
    private fun getExcludedPackages():
        Set<String> {

        val excluded =
            linkedSetOf<String>()

        // 1. CurrentAppGuardian 自己
        excluded.add(
            context.packageName
        )

        // 2. 系統 Home / Launcher
        try {

            val homeIntent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_HOME
                    )

                    addCategory(
                        Intent.CATEGORY_DEFAULT
                    )
                }

            val homeActivities =
                context.packageManager
                    .queryIntentActivities(
                        homeIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )

            homeActivities.forEach {
                val packageName =
                    it.activityInfo
                        ?.packageName

                if (
                    !packageName.isNullOrBlank()
                ) {
                    excluded.add(
                        packageName
                    )
                }
            }

        } catch (_: Throwable) {
            // 無法取得 Launcher 時不影響主流程
        }

        // 3. Android 設定 App
        try {

            val settingsIntent =
                Intent(
                    Settings.ACTION_SETTINGS
                )

            val settingsInfo =
                context.packageManager
                    .resolveActivity(
                        settingsIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )

            val settingsPackage =
                settingsInfo
                    ?.activityInfo
                    ?.packageName

            if (
                !settingsPackage.isNullOrBlank()
            ) {
                excluded.add(
                    settingsPackage
                )
            }

        } catch (_: Throwable) {
            // 無法取得設定 App 時不影響主流程
        }

        return excluded
    }

    private fun shouldExcludePackage(
        packageName: String,
        excludedPackages:
            Set<String>
    ): Boolean {

        val normalized =
            packageName.trim()

        if (
            normalized.isBlank()
        ) {
            return true
        }

        return excludedPackages
            .contains(
                normalized
            )
    }

    private fun createInfo(
        packageName: String,
        detectedAt: Long,
        source:
            CurrentAppInfo.Source
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

                    /*
                     * Android 11 相容寫法。
                     */
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

                } catch (
                    _:
                        PackageManager
                            .NameNotFoundException
                ) {

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
