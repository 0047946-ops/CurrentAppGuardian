package com.currentguardian.model

data class AppBaseline(
    val packageName:
        String,

    val label:
        String,

    val createdAt:
        Long,

    val sessionStartedAt:
        Long,

    val detectionSource:
        CurrentAppInfo.Source
) {

    fun summary():
        String {

        return buildString {

            appendLine(
                "Baseline 已建立"
            )

            appendLine(
                "App：$label"
            )

            appendLine(
                "Package：$packageName"
            )

            appendLine(
                "建立時間：$createdAt"
            )

            appendLine(
                "Session：$sessionStartedAt"
            )

            append(
                "來源："
            )

            append(
                when (
                    detectionSource
                ) {

                    CurrentAppInfo.Source.USAGE_EVENT ->
                        "Usage Event"

                    CurrentAppInfo.Source.USAGE_STAT ->
                        "Usage Statistics"

                    CurrentAppInfo.Source.LAUNCHER ->
                        "管家啟動"

                    CurrentAppInfo.Source.MANUAL ->
                        "手動選擇"

                    CurrentAppInfo.Source.UNKNOWN ->
                        "未知"
                }
            )
        }
    }
}
