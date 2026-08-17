package com.currentguardian.model

data class CurrentAppInfo(
    val packageName: String,
    val label: String,
    val detectedAt: Long,
    val source: Source
) {

    enum class Source {

        USAGE_EVENT,

        USAGE_STAT,

        UNKNOWN
    }

    fun isKnown(): Boolean {
        return packageName.isNotBlank()
    }

    fun displayText(): String {

        return buildString {

            append(label)

            append("\n")

            append(packageName)

            append("\n")

            append("來源：")

            append(
                when (source) {

                    Source.USAGE_EVENT ->
                        "使用事件"

                    Source.USAGE_STAT ->
                        "使用統計"

                    Source.UNKNOWN ->
                        "未知"
                }
            )
        }
    }
}
