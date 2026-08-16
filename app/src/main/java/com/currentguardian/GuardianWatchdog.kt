package com.currentguardian

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object GuardianWatchdog {

    fun restartGuardian(
        context: Context
    ) {

        try {

            val intent =
                Intent(
                    context,
                    GuardianService::class.java
                )

            ContextCompat.startForegroundService(
                context,
                intent
            )

        } catch (_: Exception) {
            /*
             * Android 若拒絕背景重新啟動，
             * 不假裝可以強制復活。
             */
        }
    }
}
