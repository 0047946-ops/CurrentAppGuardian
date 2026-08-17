package com.currentguardian.model

data class AppBaseline(
    val packageName: String,
    val label: String,
    val createdAt: Long,
    val sessionStartedAt: Long,
    val detectionSource: CurrentAppInfo.Source
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
                "偵測來源："
            )

            append(
                when (
                    detectionSource
                ) {

                    CurrentAppInfo.Source
                        .USAGE_EVENT ->
                        "使用事件"

                    CurrentAppInfo.Source
                        .USAGE_STAT ->
                        "使用統計"

                    CurrentAppInfo.Source
                        .UNKNOWN ->
                        "未知"
                }
            )
        }
    }
}
