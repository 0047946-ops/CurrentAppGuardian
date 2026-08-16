package com.currentguardian.monitor

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import com.currentguardian.blackbox.IncidentManager
import com.currentguardian.model.PerformanceSnapshot

class PerformanceSampler(
    private val context: Context,
    private val incidentManager: IncidentManager
) {

    private val activityManager =
        context.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager

    fun sample(
        networkType: String,
        networkValidated: Boolean,
        latencyMs: Long,
        jitterMs: Long
    ): PerformanceSnapshot {

        val memoryInfo =
            ActivityManager.MemoryInfo()

        activityManager
            .getMemoryInfo(memoryInfo)

        val availableRam =
            memoryInfo.availMem /
                (1024L * 1024L)

        val totalRam =
            memoryInfo.totalMem /
                (1024L * 1024L)

        val batteryManager =
            context.getSystemService(
                Context.BATTERY_SERVICE
            ) as BatteryManager

        val battery =
            batteryManager.getIntProperty(
                BatteryManager
                    .BATTERY_PROPERTY_CAPACITY
            )

        val charging =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M
            ) {

                batteryManager
                    .isCharging

            } else {

                false
            }

        val snapshot =
            PerformanceSnapshot(

                timestamp =
                    System.currentTimeMillis(),

                cpuLoadPercent =
                    estimateCpuLoad(),

                availableRamMb =
                    availableRam,

                totalRamMb =
                    totalRam,

                temperatureC =
                    readBatteryTemperature(),

                batteryPercent =
                    battery,

                batteryCharging =
                    charging,

                networkType =
                    networkType,

                networkValidated =
                    networkValidated,

                estimatedLatencyMs =
                    latencyMs,

                jitterMs =
                    jitterMs
            )

        incidentManager.recordRaw(
            snapshot.serialize()
        )

        return snapshot
    }

    private fun estimateCpuLoad():
        Int {

        return try {

            val stats =
                Debug.MemoryInfo()

            Debug.getMemoryInfo(
                stats
            )

            0

        } catch (
            _: Exception
        ) {

            -1
        }
    }

    private fun readBatteryTemperature():
        Float {

        return try {

            val intent =
                context.registerReceiver(
                    null,
                    android.content.IntentFilter(
                        android.content.Intent
                            .ACTION_BATTERY_CHANGED
                    )
                )

            val raw =
                intent?.getIntExtra(
                    BatteryManager
                        .EXTRA_TEMPERATURE,
                    -1
                ) ?: -1

            if (raw >= 0) {

                raw / 10f

            } else {

                -1f
            }

        } catch (
            _: Exception
        ) {

            -1f
        }
    }
}
