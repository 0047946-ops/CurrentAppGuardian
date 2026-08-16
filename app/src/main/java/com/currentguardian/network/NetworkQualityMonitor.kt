package com.currentguardian.network

import java.net.InetAddress
import kotlin.math.abs

class NetworkQualityMonitor {

    private var previousLatency =
        -1L

    fun measure(
        host: String = "1.1.1.1"
    ): Pair<Long, Long> {

        val latency =
            try {

                val start =
                    System.nanoTime()

                InetAddress
                    .getByName(host)
                    .isReachable(1000)

                val elapsed =
                    System.nanoTime() -
                        start

                elapsed / 1_000_000L

            } catch (
                _: Exception
            ) {

                -1L
            }

        val jitter =
            if (
                previousLatency >= 0 &&
                latency >= 0
            ) {

                abs(
                    latency -
                        previousLatency
                )

            } else {

                0L
            }

        if (latency >= 0) {

            previousLatency =
                latency
        }

        return Pair(
            latency,
            jitter
        )
    }

    fun reset() {

        previousLatency =
            -1L
    }
}
