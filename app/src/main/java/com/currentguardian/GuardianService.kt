package com.currentguardian

import android.app.*
import android.content.*
import android.net.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.currentguardian.blackbox.BlackBox
import com.currentguardian.monitor.SystemSnapshot
import kotlinx.coroutines.*
import kotlin.math.max

class GuardianService : Service() {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    private lateinit var blackBox: BlackBox
    private lateinit var detector: CurrentAppDetector

    private var abnormalScore = 0

    override fun onCreate() {
        super.onCreate()

        blackBox = BlackBox(this)
        detector = CurrentAppDetector(this)

        createNotificationChannel()

        startForeground(
            1001,
            buildNotification("管家監測中")
        )

        startMonitoring()
    }

    private fun startMonitoring() {

        scope.launch {

            while (isActive) {

                val app = detector.getCurrentApp()
                val snapshot = SystemSnapshot.collect(
                    this@GuardianService
                )

                val record = buildString {

                    append("time=")
                    append(System.currentTimeMillis())
                    append('\n')

                    append("current_app=")
                    append(app.packageName ?: "unknown")
                    append('\n')

                    append("app_label=")
                    append(app.label ?: "unknown")
                    append('\n')

                    append("cpu_load=")
                    append(snapshot.cpuLoad)
                    append('\n')

                    append("available_ram_mb=")
                    append(snapshot.availableRamMb)
                    append('\n')

                    append("temperature_c=")
                    append(snapshot.temperatureC)
                    append('\n')

                    append("battery_percent=")
                    append(snapshot.batteryPercent)
                    append('\n')

                    append("network=")
                    append(snapshot.networkType)
                    append('\n')

                    append("network_validated=")
                    append(snapshot.networkValidated)
                    append('\n')
                }

                blackBox.record(record)

                abnormalScore =
                    calculateAbnormalScore(snapshot)

                val interval =
                    when {
                        abnormalScore >= 7 -> 250L
                        abnormalScore >= 4 -> 750L
                        else -> 3000L
                    }

                delay(interval)
            }
        }
    }

    private fun calculateAbnormalScore(
        snapshot: SystemSnapshot
    ): Int {

        var score = 0

        if (snapshot.cpuLoad >= 90) {
            score += 2
        }

        if (snapshot.availableRamMb <= 512) {
            score += 3
        }

        if (snapshot.temperatureC >= 43f) {
            score += 2
        }

        if (!snapshot.networkValidated) {
            score += 1
        }

        return score
    }

    private fun buildNotification(
        text: String
    ): Notification {

        return NotificationCompat.Builder(
            this,
            "guardian"
        )
            .setContentTitle("當前應用效能管家")
            .setContentText(text)
            .setSmallIcon(
                android.R.drawable.ic_menu_info_details
            )
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= 26) {

            val channel = NotificationChannel(
                "guardian",
                "效能管家監測",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(
                NotificationManager::class.java
            ).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {

        blackBox.flush()

        scope.cancel()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
