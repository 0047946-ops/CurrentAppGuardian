package com.currentguardian.launcher

import android.content.Context
import android.content.Intent

class AppLauncher(
    private val context:
        Context
) {

    fun launch(
        packageName:
            String
    ):
        LaunchResult {

        if (
            packageName.isBlank()
        ) {

            return LaunchResult(
                success =
                    false,

                reason =
                    "Package 不可為空。"
            )
        }

        return try {

            val launchIntent =
                context
                    .packageManager
                    .getLaunchIntentForPackage(
                        packageName
                    )

            if (
                launchIntent == null
            ) {

                LaunchResult(
                    success =
                        false,

                    reason =
                        "找不到可啟動入口。"
                )

            } else {

                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(
                    launchIntent
                )

                LaunchResult(
                    success =
                        true,

                    reason =
                        "已發出啟動請求。"
                )
            }

        } catch (
            error: Exception
        ) {

            LaunchResult(
                success =
                    false,

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
    val success:
        Boolean,

    val reason:
        String
)
