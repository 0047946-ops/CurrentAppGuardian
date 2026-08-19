package com.currentguardian

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.currentguardian.model.AppBaseline
import com.currentguardian.model.AppTargetInfo
import com.currentguardian.model.CurrentAppInfo
import com.currentguardian.session.AppSession

class MainActivity : Activity() {

    private lateinit var engine:
        CurrentAppEngine

    private var currentSession:
        AppSession? = null

    private var currentBaseline:
        AppBaseline? = null

    private var currentTarget:
        AppTargetInfo? = null

    private lateinit var statusText:
        TextView

    private lateinit var currentAppText:
        TextView

    private lateinit var sessionText:
        TextView

    private lateinit var baselineText:
        TextView

    override fun onCreate(
        savedInstanceState:
            Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        engine =
            CurrentAppEngine(
                this
            )

        buildUi()
    }

    override fun onResume() {

        super.onResume()

        refreshStatus()
    }

    private fun buildUi() {

        val scroll =
            ScrollView(
                this
            )

        val root =
            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    32,
                    32,
                    32,
                    32
                )
            }

        val title =
            TextView(
                this
            ).apply {

                text =
                    "Current App Guardian"

                textSize =
                    28f

                gravity =
                    Gravity.CENTER
            }

        statusText =
            createInfoText(
                "P2：初始化中"
            )

        currentAppText =
            createInfoText(
                "目前 App：尚未建立"
            )

        sessionText =
            createInfoText(
                "Session：尚未建立"
            )

        baselineText =
            createInfoText(
                "Baseline：尚未建立"
            )

        val permissionButton =
            Button(
                this
            ).apply {

                text =
                    "開啟 Usage Access 設定"

                setOnClickListener {

                    startActivity(
                        Intent(
                            Settings
                                .ACTION_USAGE_ACCESS_SETTINGS
                        )
                    )
                }
            }

        /*
         * 工程測試入口：
         * 真正查詢 Android 回報的前景 App。
         */
        val autoButton =
            Button(
                this
            ).apply {

                text =
                    "工程測試：自動辨識目前前景 App"

                setOnClickListener {

                    detectAutomatically()
                }
            }

        /*
         * 工程測試入口：
         * 讓使用者選擇目標 App。
         */
        val launcherButton =
            Button(
                this
            ).apply {

                text =
                    "工程測試：選擇要啟動的 App"

                setOnClickListener {

                    showTargetList()
                }
            }

        /*
         * 工程測試入口：
         * 啟動目前選定的 App。
         */
        val launchButton =
            Button(
                this
            ).apply {

                text =
                    "工程測試：啟動目前選定 App"

                setOnClickListener {

                    launchSelected()
                }
            }

        /*
         * 工程測試入口：
         * 驗證使用者選定的 App
         * 是否已經真的成為 Android 前景。
         */
        val verifyButton =
            Button(
                this
            ).apply {

                text =
                    "工程測試：辨識目前選定 App"

                setOnClickListener {

                    detectSelectedTarget()
                }
            }

        val endButton =
            Button(
                this
            ).apply {

                text =
                    "工程測試：結束目前 Session"

                setOnClickListener {

                    endSession()
                }
            }

        root.addView(
            title
        )

        root.addView(
            statusText
        )

        root.addView(
            permissionButton
        )

        root.addView(
            autoButton
        )

        root.addView(
            launcherButton
        )

        root.addView(
            launchButton
        )

        root.addView(
            verifyButton
        )

        root.addView(
            endButton
        )

        root.addView(
            currentAppText
        )

        root.addView(
            sessionText
        )

        root.addView(
            baselineText
        )

        scroll.addView(
            root
        )

        setContentView(
            scroll
        )

        refreshStatus()
    }

    private fun createInfoText(
        initial:
            String
    ): TextView {

        return TextView(
            this
        ).apply {

            text =
                initial

            textSize =
                16f

            setPadding(
                0,
                18,
                0,
                0
            )
        }
    }

    private fun refreshStatus() {

        val mode =
            engine.recommendedMode()

        statusText.text =
            when (
                mode
            ) {

                CurrentAppEngine.Mode
                    .AUTO_USAGE ->

                    "P2：Usage Access 可用／自動辨識可用"

                CurrentAppEngine.Mode
                    .LAUNCHER ->

                    "P2：啟動器模式"

                CurrentAppEngine.Mode
                    .MANUAL ->

                    "P2：手動目標模式"
            }
    }

    /**
     * 真正的「目前前景 App」測試。
     */
    private fun detectAutomatically() {

        if (
            !engine.usageAccessAvailable()
        ) {

            statusText.text =
                "Usage Access 尚未允許。"

            return
        }

        val app =
            engine.detectAutomatically()

        if (
            app == null
        ) {

            statusText.text =
                "目前無法從 Usage Access 取得可確認的前景 App。"

            return
        }

        startSession(
            app
        )

        statusText.text =
            "自動辨識成功：" +
                app.label
    }

    /**
     * 讓使用者選擇任何可見的 App。
     *
     * 不可啟動的 App 仍然顯示，
     * 但不允許選擇。
     */
    private fun showTargetList() {

        val apps =
            engine.availableTargets()

        if (
            apps.isEmpty()
        ) {

            statusText.text =
                "目前沒有取得可顯示的 App。"

            return
        }

        val labels =
            apps.map { app ->

                if (
                    app.launchable &&
                    !app.protectedSystemApp
                ) {

                    app.label +
                        "  [可啟動]"

                } else {

                    app.label +
                        "  [" +
                        app.statusText() +
                        "]"
                }

            }.toTypedArray()

        android.app.AlertDialog.Builder(
            this
        )
            .setTitle(
                "選擇目標 App"
            )
            .setItems(
                labels
            ) { _, which ->

                val target =
                    apps[which]

                if (
                    !target.isEligibleTarget(
                        packageName
                    )
                ) {

                    statusText.text =
                        target.label +
                            "：" +
                            target.statusText()

                    return@setItems
                }

                if (
                    engine.selectTarget(
                        target
                    )
                ) {

                    currentTarget =
                        target

                    statusText.text =
                        "已選擇目標：" +
                            target.label

                    currentAppText.text =
                        buildString {

                            appendLine(
                                "目標 App："
                            )

                            appendLine(
                                target.label
                            )

                            appendLine(
                                target.packageName
                            )

                            append(
                                "狀態：" +
                                    target.statusText()
                            )
                        }
                }
            }
            .show()
    }

    /**
     * 啟動使用者選定的 App。
     */
    private fun launchSelected() {

        val target =
            currentTarget

        if (
            target == null
        ) {

            statusText.text =
                "請先選擇目標 App。"

            return
        }

        if (
            !target.isEligibleTarget(
                packageName
            )
        ) {

            statusText.text =
                target.label +
                    "：" +
                    target.statusText()

            return
        }

        val result =
            engine.launchSelected()

        if (
            !result.success
        ) {

            statusText.text =
                result.reason

            return
        }

        statusText.text =
            "已要求啟動：" +
                target.label +
                "。"

        /*
         * 注意：
         *
         * 這裡不立即假設「目前前景就是 target」。
         *
         * 必須等 Android Usage Event
         * 真正回報後才建立 Launcher Session。
         *
         * 這正是本次修正的核心。
         */
        currentAppText.text =
            buildString {

                appendLine(
                    "目標 App："
                )

                appendLine(
                    target.label
                )

                appendLine(
                    target.packageName
                )

                append(
                    "等待 Android 回報實際前景狀態……"
                )
            }
    }

    /**
     * 驗證目前選定的 App 是否真的已經進入前景。
     */
    private fun detectSelectedTarget() {

        val target =
            currentTarget

        if (
            target == null
        ) {

            statusText.text =
                "目前沒有選定目標 App。"

            return
        }

        if (
            !engine.usageAccessAvailable()
        ) {

            statusText.text =
                "Usage Access 尚未允許。"

            return
        }

        val detected =
            engine.detectSelectedTarget()

        if (
            detected == null
        ) {

            val actual =
                engine.lastDetectedApp()

            statusText.text =
                if (
                    actual != null
                ) {

                    "目標是 " +
                        target.label +
                        "，但 Android 目前回報前景為 " +
                        actual.label +
                        "。"

                } else {

                    "目前尚未確認 " +
                        target.label +
                        " 已進入前景。"
                }

            return
        }

        startSession(
            detected
        )

        statusText.text =
            "智慧辨識成功：" +
                detected.label
    }

    private fun startSession(
        app:
            CurrentAppInfo
    ) {

        currentSession =
            AppSession(
                app =
                    app
            )

        currentBaseline =
            AppBaseline(
                packageName =
                    app.packageName,

                label =
                    app.label,

                createdAt =
                    System.currentTimeMillis(),

                sessionStartedAt =
                    currentSession!!
                        .startedAt,

                detectionSource =
                    app.source
            )

        currentAppText.text =
            app.displayText()

        sessionText.text =
            buildString {

                appendLine(
                    "Session：運行中"
                )

                append(
                    "開始：" +
                        currentSession!!
                            .startedAt
                )
            }

        baselineText.text =
            currentBaseline!!
                .summary()
    }

    private fun endSession() {

        val session =
            currentSession

        if (
            session == null
        ) {

            statusText.text =
                "目前沒有 Session。"

            return
        }

        session.end()

        sessionText.text =
            buildString {

                appendLine(
                    "Session：已結束"
                )

                append(
                    "最後事件：" +
                        session.lastEvent()
                )
            }

        statusText.text =
            "Session 已結束"

        currentSession =
            null

        currentBaseline =
            null
    }
}
