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
import com.currentguardian.session.AppSession

class MainActivity : Activity() {

    private lateinit var detector:
        CurrentAppDetector

    private var currentSession:
        AppSession? = null

    private var currentBaseline:
        AppBaseline? = null

    private lateinit var statusText:
        TextView

    private lateinit var currentAppText:
        TextView

    private lateinit var sessionText:
        TextView

    private lateinit var baselineText:
        TextView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        detector =
            CurrentAppDetector(
                this
            )

        buildUi()
    }

    override fun onResume() {
        super.onResume()

        updatePermissionStatus()
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
            TextView(
                this
            ).apply {

                textSize =
                    17f

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    20,
                    0,
                    12
                )
            }

        currentAppText =
            createInfoText(
                "目前 App：尚未偵測"
            )

        sessionText =
            createInfoText(
                "Session：尚未建立"
            )

        baselineText =
            createInfoText(
                "Baseline：尚未建立"
            )

        val usageButton =
            Button(
                this
            ).apply {

                text =
                    "開啟 Current App Guardian 使用狀態存取"

                setOnClickListener {
                    openUsageAccessSettings()
                }
            }

        val detectButton =
            Button(
                this
            ).apply {

                text =
                    "偵測目前 App"

                setOnClickListener {
                    detectCurrentApp()
                }
            }

        val endButton =
            Button(
                this
            ).apply {

                text =
                    "結束目前 Session"

                setOnClickListener {
                    endCurrentSession()
                }
            }

        root.addView(
            title
        )

        root.addView(
            statusText
        )

        root.addView(
            usageButton
        )

        root.addView(
            detectButton
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

        updatePermissionStatus()
    }

    private fun createInfoText(
        initial: String
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

    private fun openUsageAccessSettings() {

        try {

            if (
                android.os.Build.VERSION.SDK_INT >= 29
            ) {

                val intent =
                    Intent(
                        Settings.ACTION_APP_USAGE_SETTINGS
                    ).apply {

                        putExtra(
                            Intent.EXTRA_PACKAGE_NAME,
                            packageName
                        )
                    }

                startActivity(
                    intent
                )

            } else {

                val intent =
                    Intent(
                        Settings.ACTION_USAGE_ACCESS_SETTINGS
                    )

                startActivity(
                    intent
                )
            }

        } catch (_: Exception) {

            try {

                val fallback =
                    Intent(
                        Settings.ACTION_USAGE_ACCESS_SETTINGS
                    )

                startActivity(
                    fallback
                )

            } catch (_: Exception) {

                statusText.text =
                    "無法開啟使用狀態存取設定。"
            }
        }
    }

    private fun updatePermissionStatus() {

        statusText.text =
            if (
                detector.hasUsageAccess()
            ) {

                "使用狀態存取：已授權"

            } else {

                "使用狀態存取：尚未授權"
            }
    }

    private fun detectCurrentApp() {

        if (
            !detector.hasUsageAccess()
        ) {

            statusText.text =
                "請先授權 Current App Guardian 的使用狀態存取。"

            return
        }

        val app =
            detector.detect()

        if (
            app == null
        ) {

            currentAppText.text =
                "目前 App：無法取得"

            sessionText.text =
                "Session：未建立"

            baselineText.text =
                "Baseline：未建立"

            statusText.text =
                "目前沒有取得可確認的前景 App。"

            return
        }

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
                    currentSession!!.startedAt,

                detectionSource =
                    app.source
            )

        currentAppText.text =
            buildString {

                appendLine(
                    "目前 App："
                )

                appendLine(
                    app.label
                )

                append(
                    app.packageName
                )
            }

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

        statusText.text =
            "P2：Current App 已辨識"
    }

    private fun endCurrentSession() {

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
            "P2：Session 已結束"
    }
}
