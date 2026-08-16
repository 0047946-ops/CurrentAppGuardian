package com.currentguardian.launcher

import android.content.Context
import android.content.Intent

class AppLauncher(
    private val context: Context
) {

    fun canLaunch(
        packageName: String
    ): Boolean {

        return try {

            context.packageManager
                .getLaunchIntentForPackage(
                    packageName
                ) != null

        } catch (_: Exception) {

            false
        }
    }

    fun launch(
        packageName: String
    ): LaunchResult {

        return try {

            val intent =
                context.packageManager
                    .getLaunchIntentForPackage(
                        packageName
                    )

            if (intent == null) {

                return LaunchResult(
                    success = false,
                    reason =
                        "找不到可啟動的 App。"
                )
            }

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(
                intent
            )

            LaunchResult(
                success = true,
                reason =
                    "已發出啟動請求。"
            )

        } catch (
            error: SecurityException
        ) {

            LaunchResult(
                success = false,
                reason =
                    "系統拒絕啟動：" +
                    (
                        error.message
                            ?: "SecurityException"
                    )
            )

        } catch (
            error: Exception
        ) {

            LaunchResult(
                success = false,
                reason =
                    "啟動失敗：" +
                    (
                        error.message
                            ?: "Unknown error"
                    )
            )
        }
    }
}

data class LaunchResult(
    val success: Boolean,
    val reason: String
)
