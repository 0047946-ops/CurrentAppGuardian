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
        AppSession? =
        null

    private var currentBaseline:
        AppBaseline? =
        null

    private var currentTarget:
        AppTargetInfo? =
        null

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

        val autoButton =
            Button(
                this
            ).apply {

                text =
                    "自動辨識目前 App"

                setOnClickListener {

                    detectAutomatically()
                }
            }

        val launcherButton =
            Button(
                this
            ).apply {

                text =
                    "選擇要啟動的 App"

                setOnClickListener {

                    showTargetList()
                }
            }

        val launchButton =
            Button(
                this
            ).apply {

                text =
                    "啟動目前選定 App"

                setOnClickListener {

                    launchSelected()
                }
            }

        val endButton =
            Button(
                this
            ).apply {

                text =
                    "結束目前 Session"

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
    ):
        TextView {

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

                    "P2：自動辨識模式可用"

                CurrentAppEngine.Mode
                    .LAUNCHER ->

                    "P2：啟動器模式"

                CurrentAppEngine.Mode
                    .MANUAL ->

                    "P2：手動目標模式"
            }
    }

    private fun detectAutomatically() {

        if (
            !engine.usageAccessAvailable()
        ) {

            statusText.text =
                "Usage Access 不可用，改用「選擇要啟動的 App」。"

            return
        }

        val app =
            engine.detectAutomatically()

        if (
            app == null
        ) {

            statusText.text =
                "目前沒有取得可確認的前景 App。"

            return
        }

        startSession(
            app
        )
    }

    private fun showTargetList() {

        val apps =
            engine.availableTargets()

        if (
            apps.isEmpty()
        ) {

            statusText.text =
                "目前沒有找到可啟動的第三方 App。"

            return
        }

        val labels =
            apps.map {
                it.label
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
                    engine.selectTarget(
                        target
                    )
                ) {

                    currentTarget =
                        target

                    statusText.text =
                        "已選擇：" +
                            target.label

                    currentAppText.text =
                        buildString {

                            appendLine(
                                "已選定目標："
                            )

                            appendLine(
                                target.label
                            )

                            append(
                                target.packageName
                            )
                        }
                }
            }
            .show()
    }

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

        val result =
            engine.launchSelected()

        if (
            !result.success
        ) {

            statusText.text =
                result.reason

            return
        }

        val app =
            CurrentAppInfo(
                packageName =
                    target.packageName,

                label =
                    target.label,

                detectedAt =
                    System.currentTimeMillis(),

                source =
                    CurrentAppInfo.Source
                        .LAUNCHER
            )

        startSession(
            app
        )

        statusText.text =
            "已啟動：" +
                target.label
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
