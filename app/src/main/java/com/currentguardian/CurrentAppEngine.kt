package com.currentguardian

import android.content.Context
import android.os.SystemClock
import com.currentguardian.launcher.AppCatalog
import com.currentguardian.launcher.AppLauncher
import com.currentguardian.launcher.LaunchResult
import com.currentguardian.model.AppTargetInfo
import com.currentguardian.model.CurrentAppInfo

class CurrentAppEngine(
    private val context: Context
) {

    enum class Mode {

        AUTO_USAGE,

        LAUNCHER,

        MANUAL
    }

    private val usageDetector =
        CurrentAppDetector(
            context
        )

    private val catalog =
        AppCatalog(
            context
        )

    private val launcher =
        AppLauncher(
            context
        )

    private var selectedTarget:
        AppTargetInfo? = null

    private var lastDetectedApp:
        CurrentAppInfo? = null

    private var lastLaunchRequest:
        Long = 0L

    fun usageAccessAvailable():
        Boolean {

        return usageDetector
            .hasUsageAccess()
    }

    /**
     * 自動辨識 Android 實際前景 App。
     */
    fun detectAutomatically():
        CurrentAppInfo? {

        if (
            !usageAccessAvailable()
        ) {
            return null
        }

        val detected =
            usageDetector.detect()
                ?: return null

        lastDetectedApp =
            detected

        return detected
    }

    /**
     * 驗證使用者選擇的目標 App
     * 是否已經真的進入前景。
     */
    fun detectSelectedTarget():
        CurrentAppInfo? {

        val target =
            selectedTarget
                ?: return null

        val detected =
            usageDetector
                .detectExpectedTarget(
                    expectedPackageName =
                        target.packageName
                )
                ?: return null

        lastDetectedApp =
            detected

        return detected
    }

    fun lastDetectedApp():
        CurrentAppInfo? {

        return lastDetectedApp
    }

    fun availableTargets():
        List<AppTargetInfo> {

        return catalog
            .getEligibleApps()
    }

    fun selectTarget(
        target: AppTargetInfo
    ): Boolean {

        if (
            !target.isEligibleTarget(
                context.packageName
            )
        ) {
            return false
        }

        selectedTarget =
            target

        return true
    }

    fun selectedTarget():
        AppTargetInfo? {

        return selectedTarget
    }

    fun launchSelected():
        LaunchResult {

        val target =
            selectedTarget
                ?: return LaunchResult(
                    success = false,
                    reason =
                        "尚未選擇目標 App。"
                )

        val result =
            launcher.launch(
                target.packageName
            )

        if (
            result.success
        ) {

            lastLaunchRequest =
                SystemClock
                    .elapsedRealtime()
        }

        return result
    }

    fun lastLaunchRequestTime():
        Long {

        return lastLaunchRequest
    }

    fun recommendedMode():
        Mode {

        return when {

            usageAccessAvailable() ->
                Mode.AUTO_USAGE

            selectedTarget != null ->
                Mode.LAUNCHER

            else ->
                Mode.MANUAL
        }
    }
}
