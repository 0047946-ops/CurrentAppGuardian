package com.currentguardian

import android.app.*
import android.content.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.currentguardian.blackbox.IncidentManager
import com.currentguardian.monitor.AdaptiveMonitor
import com.currentguardian.monitor.EventMonitor
import com.currentguardian.monitor.SystemSnapshot
import kotlinx.coroutines.*

class GuardianService : Service() {

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Default
        )

    private lateinit var detector:
        CurrentAppDetector

    private lateinit var incidentManager:
        IncidentManager

    private lateinit var eventMonitor:
        EventMonitor

    private lateinit var adaptiveMonitor:
        AdaptiveMonitor

    override fun onCreate() {

        super.onCreate()

        detector =
            CurrentAppDetector(this)

        incidentManager =
            IncidentManager(this)

        eventMonitor =
            EventMonitor(
                this,
                incidentManager
            )

        adaptiveMonitor =
            AdaptiveMonitor()

        createNotificationChannel()

        startForeground(
            1001,
            buildNotification(
                "黑盒管家監測中"
            )
        )

        incidentManager.mark(
            type = "GUARDIAN_START",
            detail =
                "GuardianService started"
        )

        startMonitoring()
    }

    private fun startMonitoring() {

        scope.launch {

            while (isActive) {

                val currentApp =
                    detector.getCurrentApp()

                val snapshot =
                    SystemSnapshot.collect(
                        this@GuardianService
                    )

                eventMonitor.checkCurrentApp(
                    currentApp.packageName
                )

                val level =
                    adaptiveMonitor.evaluate(
                        cpu =
                            snapshot.cpuLoad,

                        ramMb =
                            snapshot.availableRamMb,

                        temperature =
                            snapshot.temperatureC,

                        networkValidated =
                            snapshot.networkValidated
                    )

                incidentManager.recordRaw(
                    buildString {

                        append(
                            "SNAPSHOT|"
                        )

                        append(
                            "time=" +
                                System.currentTimeMillis()
                        )

                        append(
                            "|package=" +
                                (
                                    currentApp.packageName
                                        ?: "unknown"
                                )
                        )

                        append(
                            "|label=" +
                                (
                                    currentApp.label
                                        ?: "unknown"
                                )
                        )

                        append(
                            "|cpu=" +
                                snapshot.cpuLoad
                        )

                        append(
                            "|ram=" +
                                snapshot.availableRamMb
                        )

                        append(
                            "|temp=" +
                                snapshot.temperatureC
                        )

                        append(
                            "|battery=" +
                                snapshot.batteryPercent
                        )

                        append(
                            "|network=" +
                                snapshot.networkType
                        )

                        append(
                            "|validated=" +
                                snapshot.networkValidated
                        )

                        append(
                            "|level=" +
                                level.name
                        )
                    }
                )

                delay(
                    adaptiveMonitor.intervalMs()
                )
            }
        }
    }

    private fun buildNotification(
        text: String
    ): Notification {

        return NotificationCompat.Builder(
            this,
            "guardian"
        )
            .setContentTitle(
                "當前應用效能管家"
            )
            .setContentText(text)
            .setSmallIcon(
                android.R.drawable
                    .ic_menu_info_details
            )
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >= 26
        ) {

            val channel =
                NotificationChannel(
                    "guardian",
                    "效能管家監測",
                    NotificationManager
                        .IMPORTANCE_LOW
                )

            getSystemService(
                NotificationManager::class.java
            ).createNotificationChannel(
                channel
            )
        }
    }

    override fun onTaskRemoved(
        rootIntent: Intent?
    ) {

        incidentManager.mark(
            type = "GUARDIAN_TASK_REMOVED",
            detail =
                "Task removed by system/user",
            severity = 2
        )

        super.onTaskRemoved(
            rootIntent
        )
    }

    override fun onDestroy() {

        incidentManager.mark(
            type = "GUARDIAN_DESTROY",
            detail =
                "GuardianService onDestroy"
        )

        incidentManager
            .recentEvents()

        scope.cancel()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}
