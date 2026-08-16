package com.currentguardian

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

data class CurrentApp(
    val packageName: String?,
    val label: String?
)

class CurrentAppDetector(
    private val context: Context
) {

    fun getCurrentApp(): CurrentApp {

        val manager =
            context.getSystemService(
                Context.USAGE_STATS_SERVICE
            ) as UsageStatsManager

        val end = System.currentTimeMillis()
        val begin = end - 10_000

        val events = manager.queryEvents(
            begin,
            end
        )

        val event = UsageEvents.Event()

        var latestPackage: String? = null
        var latestTime = 0L

        while (events.hasNextEvent()) {

            events.getNextEvent(event)

            if (
                event.eventType ==
                UsageEvents.Event.ACTIVITY_RESUMED
            ) {

                if (event.timeStamp > latestTime) {

                    latestTime = event.timeStamp
                    latestPackage =
                        event.packageName
                }
            }
        }

        val label =
            latestPackage?.let {
                try {
                    val info =
                        context.packageManager
                            .getApplicationInfo(it, 0)

                    context.packageManager
                        .getApplicationLabel(info)
                        .toString()

                } catch (_: Exception) {
                    null
                }
            }

        return CurrentApp(
            latestPackage,
            label
        )
    }
}
