package com.currentguardian.launcher

import android.content.Context
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

        val applications =
            try {

                packageManager
                    .getInstalledApplications(
                        PackageManager.GET_META_DATA
                    )

            } catch (_: Throwable) {

                emptyList()
            }

        val result =
            mutableMapOf<String, AppTargetInfo>()

        for (
            applicationInfo in applications
        ) {

            if (
                applicationInfo == null
            ) {
                continue
            }

            val packageName =
                applicationInfo.packageName
                    ?.trim()
                    ?: continue

            if (
                packageName.isBlank()
            ) {
                continue
            }

            /*
             * 不把 CurrentAppGuardian 自己列入可選目標。
             */
            if (
                packageName ==
                context.packageName
            ) {
                continue
            }

            val label =
                try {

                    packageManager
                        .getApplicationLabel(
                            applicationInfo
                        )
                        ?.toString()
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: packageName

                } catch (_: Throwable) {

                    packageName
                }

            /*
             * 有 Launch Intent = 一般使用者可直接啟動的 App。
             *
             * 沒有 Launch Intent 的程式仍然保留在清單，
             * 但 launchable=false。
             */
            val launchIntent =
                try {

                    packageManager
                        .getLaunchIntentForPackage(
                            packageName
                        )

                } catch (_: Throwable) {

                    null
                }

            val launchable =
                launchIntent != null

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

        /*
         * 可啟動的一般 App 優先。
         * 不可啟動的項目仍然保留，讓使用者知道它存在。
         */
        return result
            .values
            .sortedWith(
                compareBy<AppTargetInfo> {
                    if (it.launchable) 0 else 1
                }.thenBy {
                    it.label.lowercase()
                }
            )
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
