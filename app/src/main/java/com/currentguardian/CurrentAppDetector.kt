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

    /**
     * 自動取得目前實際前景 App。
     *
     * 注意：
     * 這裡代表 Android 實際回報的前景 App，
     * 不代表使用者剛剛選擇的目標 App。
     */
    fun detect(
        lookbackMs: Long = 30_000L
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

            val events =
                manager.queryEvents(
                    begin,
                    now
                ) ?: return null

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
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: continue

                /*
                 * 不把 Guardian 自己當成目標 App。
                 */
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
                    ?: return null

            if (
                latestTime <= 0L
            ) {
                return null
            }

            createInfo(
                packageName =
                    packageName,

                detectedAt =
                    latestTime,

                source =
                    CurrentAppInfo.Source
                        .USAGE_EVENT
            )

        } catch (_: SecurityException) {

            null

        } catch (_: IllegalArgumentException) {

            null

        } catch (_: Throwable) {

            null
        }
    }

    /**
     * 驗證「指定的目標 App」是否已經真的成為前景 App。
     *
     * 這個方法非常重要。
     *
     * 例如使用者選 Facebook：
     *
     * selected = Facebook
     *
     * 但 Facebook 尚未真正進入前景，
     * 這裡不會硬說「目前就是 Facebook」。
     *
     * 必須等 Android Usage Event 真正回報 Facebook。
     */
    fun detectExpectedTarget(
        expectedPackageName: String,
        lookbackMs: Long = 30_000L
    ): CurrentAppInfo? {

        val expected =
            expectedPackageName
                .trim()

        if (
            expected.isBlank()
        ) {
            return null
        }

        val detected =
            detect(
                lookbackMs =
                    lookbackMs
            )
                ?: return null

        if (
            detected.packageName !=
            expected
        ) {
            return null
        }

        return detected
    }

    private fun isForegroundEvent(
        eventType: Int
    ): Boolean {

        return when {

            eventType ==
                UsageEvents.Event
                    .ACTIVITY_RESUMED ->
                true

            /*
             * 舊 Android 相容。
             */
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
                    _: PackageManager
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
