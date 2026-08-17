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

        val intent =
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
                    intent,
                    PackageManager
                        .MATCH_DEFAULT_ONLY
                )

        val result =
            mutableMapOf<String, AppTargetInfo>()

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

            if (
                packageName ==
                context.packageName
            ) {
                continue
            }

            val protectedSystemApp =
                isProtectedSystemApp(
                    applicationInfo
                )

            if (
                protectedSystemApp
            ) {
                continue
            }

            val label =
                try {

                    packageManager
                        .getApplicationLabel(
                            applicationInfo
                        )
                        .toString()

                } catch (_: Exception) {

                    packageName
                }

            result[
                packageName
            ] =
                AppTargetInfo(
                    packageName =
                        packageName,

                    label =
                        label,

                    launchable =
                        true,

                    protectedSystemApp =
                        false
                )
        }

        return result
            .values
            .sortedBy {
                it.label
                    .lowercase()
            }
    }

    private fun isProtectedSystemApp(
        applicationInfo:
            ApplicationInfo
    ):
        Boolean {

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
