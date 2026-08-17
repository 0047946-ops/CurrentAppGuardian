package com.currentguardian.model

data class CurrentAppInfo(
    val packageName:
        String,

    val label:
        String,

    val detectedAt:
        Long,

    val source:
        Source
) {

    enum class Source {

        USAGE_EVENT,

        USAGE_STAT,

        LAUNCHER,

        MANUAL,

        UNKNOWN
    }

    fun isKnown():
        Boolean {

        return packageName.isNotBlank()
    }

    fun displayText():
        String {

        return buildString {

            appendLine(
                label
            )

            appendLine(
                packageName
            )

            append(
                "來源："
            )

            append(
                when (
                    source
                ) {

                    Source.USAGE_EVENT ->
                        "Usage Event"

                    Source.USAGE_STAT ->
                        "Usage Statistics"

                    Source.LAUNCHER ->
                        "管家啟動"

                    Source.MANUAL ->
                        "手動選擇"

                    Source.UNKNOWN ->
                        "未知"
                }
            )
        }
    }
}
