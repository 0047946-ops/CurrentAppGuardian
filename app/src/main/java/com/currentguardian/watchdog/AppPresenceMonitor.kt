package com.currentguardian.watchdog

import android.app.ActivityManager
import android.content.Context

class AppPresenceMonitor(
    context: Context
) {

    private val activityManager =
        context.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager

    fun isProcessObserved(
        packageName: String
    ): Boolean {

        return try {

            val processes =
                activityManager
                    .runningAppProcesses
                    ?: return false

            processes.any { process ->

                process.pkgList?.contains(
                    packageName
                ) == true
            }

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

    fun isForegroundObserved(
        packageName: String
    ): Boolean {

        return try {

            val processes =
                activityManager
                    .runningAppProcesses
                    ?: return false

            processes.any { process ->

                process.pkgList?.contains(
                    packageName
                ) == true &&
                process.importance ==
                    ActivityManager.RunningAppProcessInfo
                        .IMPORTANCE_FOREGROUND
            }

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
