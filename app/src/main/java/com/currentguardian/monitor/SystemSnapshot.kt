package com.currentguardian.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import java.io.RandomAccessFile

data class SystemSnapshot(
    val cpuLoad: Int,
    val availableRamMb: Long,
    val temperatureC: Float,
    val batteryPercent: Int,
    val networkType: String,
    val networkValidated: Boolean
) {

    companion object {

        fun collect(
            context: Context
        ): SystemSnapshot {

            val activityManager =
                context.getSystemService(
                    Context.ACTIVITY_SERVICE
                ) as android.app.ActivityManager

            val memoryInfo =
                android.app.ActivityManager.MemoryInfo()

            activityManager.getMemoryInfo(
                memoryInfo
            )

            val batteryManager =
                context.getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            val battery =
                batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

            val temperature =
                readTemperature(context)

            val network =
                readNetwork(context)

            return SystemSnapshot(
                cpuLoad = readCpuLoad(),
                availableRamMb =
                    memoryInfo.availMem / 1024 / 1024,
                temperatureC = temperature,
                batteryPercent = battery,
                networkType = network.first,
                networkValidated = network.second
            )
        }

        private fun readCpuLoad(): Int {

            return try {

                val reader =
                    RandomAccessFile(
                        "/proc/stat",
                        "r"
                    )

                val line =
                    reader.readLine()

                reader.close()

                val values =
                    line.trim()
                        .split("\\s+".toRegex())
                        .drop(1)
                        .mapNotNull {
                            it.toLongOrNull()
                        }

                if (values.size < 4) {
                    0
                } else {

                    val idle = values[3]

                    val total =
                        values.sum()

                    if (total <= 0) {
                        0
                    } else {
                        ((total - idle) * 100 / total)
                            .toInt()
                            .coerceIn(0, 100)
                    }
                }

            } catch (_: Exception) {
                0
            }
        }

        private fun readTemperature(
            context: Context
        ): Float {

            return try {

                val intent =
                    context.registerReceiver(
                        null,
                        android.content.IntentFilter(
                            android.content.Intent.ACTION_BATTERY_CHANGED
                        )
                    )

                val temp =
                    intent?.getIntExtra(
                        BatteryManager.EXTRA_TEMPERATURE,
                        0
                    ) ?: 0

                temp / 10f

            } catch (_: Exception) {
                -1f
            }
        }

        private fun readNetwork(
            context: Context
        ): Pair<String, Boolean> {

            val cm =
                context.getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager

            val network =
                cm.activeNetwork
                    ?: return Pair(
                        "NONE",
                        false
                    )

            val capabilities =
                cm.getNetworkCapabilities(
                    network
                ) ?: return Pair(
                    "UNKNOWN",
                    false
                )

            val type =
                when {

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    ) ->
                        "WIFI"

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    ) ->
                        "CELLULAR"

                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_ETHERNET
                    ) ->
                        "ETHERNET"

                    else ->
                        "OTHER"
                }

            val validated =
                capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )

            return Pair(
                type,
                validated
            )
        }
    }
}
