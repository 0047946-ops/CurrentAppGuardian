package com.currentguardian.watchdog

import android.os.Handler
import android.os.Looper

class WatchdogPoller(
    private val intervalMs: Long = 1000L,
    private val check: () -> Unit
) {

    private val handler =
        Handler(Looper.getMainLooper())

    private var running =
        false

    private val runnable =
        object : Runnable {

            override fun run() {

                if (!running) {
                    return
                }

                try {

                    check()

                } finally {

                    handler.postDelayed(
                        this,
                        intervalMs
                    )
                }
            }
        }

    fun start() {

        if (running) {
            return
        }

        running =
            true

        handler.post(runnable)
    }

    fun stop() {

        running =
            false

        handler.removeCallbacks(
            runnable
        )
    }
}
