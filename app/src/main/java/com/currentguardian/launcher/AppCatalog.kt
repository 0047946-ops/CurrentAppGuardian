package com.currentguardian.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.currentguardian.model.AppTargetInfo

class AppCatalog(
    private val context:
        Context
) {

    private val packageManager:
        PackageManager =
        context.packageManager

    fun getEligibleApps():
        List<AppTargetInfo> {

        val result =
            mutableMapOf<String, AppTargetInfo>()

        /*
         * 取得具有 Launcher 入口的 App。
         */
        val launcherIntent =
            Intent(
                Intent.ACTION_MAIN
            ).apply {

                addCategory(
                    Intent.CATEGORY_LAUNCHER
                )
            }

        val activities =
            packageManager
                .queryIntentActivities(
                    launcherIntent,
                    PackageManager
                        .MATCH_ALL
                )

        for (
            resolveInfo in activities
        ) {

            val activityInfo =
                resolveInfo.activityInfo
                    ?: continue

            val applicationInfo =
                activityInfo.applicationInfo
                    ?: continue

            val packageName =
                applicationInfo.packageName
                    ?.trim()
                    ?: continue

            if (
                packageName.isBlank()
            ) {
                continue
            }

            if (
                packageName ==
                context.packageName
            ) {
                continue
            }

            val label =
                getLabel(
                    applicationInfo,
                    packageName
                )

            val launchable =
                canLaunch(
                    packageName
                )

            val protectedSystemApp =
                isProtectedSystemApp(
                    applicationInfo
                )

            result[
                packageName
            ] =
                AppTargetInfo(
                    packageName =
                        packageName,

                    label =
                        label,

                    launchable =
                        launchable,

                    protectedSystemApp =
                        protectedSystemApp
                )
        }

        return result
            .values
            .sortedWith(
                compareBy<AppTargetInfo> {
                    !it.launchable
                }.thenBy {
                    it.label
                        .lowercase()
                }
            )
    }

    private fun getLabel(
        applicationInfo:
            ApplicationInfo,
        packageName:
            String
    ): String {

        return try {

            packageManager
                .getApplicationLabel(
                    applicationInfo
                )
                .toString()
                .takeIf {
                    it.isNotBlank()
                }
                ?: packageName

        } catch (_: Throwable) {

            packageName
        }
    }

    private fun canLaunch(
        packageName:
            String
    ): Boolean {

        return try {

            val launchIntent =
                packageManager
                    .getLaunchIntentForPackage(
                        packageName
                    )

            launchIntent != null

        } catch (_: Throwable) {

            false
        }
    }

    private fun isProtectedSystemApp(
        applicationInfo:
            ApplicationInfo
    ): Boolean {

        val flags =
            applicationInfo.flags

        val systemApp =
            (
                flags and
                    ApplicationInfo.FLAG_SYSTEM
            ) != 0

        val updatedSystemApp =
            (
                flags and
                    ApplicationInfo
                        .FLAG_UPDATED_SYSTEM_APP
            ) != 0

        return systemApp ||
            updatedSystemApp
    }
}
