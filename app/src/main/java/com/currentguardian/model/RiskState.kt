package com.currentguardian.model

data class RiskState(
    val score: Int,
    val level: Level,
    val reasons: List<String>
) {

    enum class Level {
        NORMAL,
        WATCH,
        WARNING,
        CRITICAL
    }

    fun isElevated(): Boolean {

        return level != Level.NORMAL
    }

    fun isCritical(): Boolean {

        return level == Level.CRITICAL
    }
}
