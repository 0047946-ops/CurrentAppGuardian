package com.currentguardian.recovery

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class RecoveryManager(
    private val context: Context
) {

    fun launchPackage(
        packageName: String
    ): Boolean {

        return try {

            val packageManager =
                context.packageManager

            val launchIntent =
                packageManager
                    .getLaunchIntentForPackage(
                        packageName
                    )
                    ?: return false

            launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(
                launchIntent
            )

            true

        } catch (
            _: SecurityException
        ) {

            false

        } catch (
            _: Exception
        ) {

            false
        }
    }
}
