package com.currentguardian.monitor

class AdaptiveMonitor {

    enum class Level {
        SILENT,
        NORMAL,
        WARNING,
        CRITICAL
    }

    private var level =
        Level.SILENT

    fun evaluate(
        cpu: Int,
        ramMb: Long,
        temperature: Float,
        networkValidated: Boolean
    ): Level {

        var score = 0

        if (cpu >= 90) {
            score += 2
        } else if (cpu >= 75) {
            score += 1
        }

        if (ramMb <= 512) {
            score += 3
        } else if (ramMb <= 1024) {
            score += 1
        }

        if (temperature >= 45f) {
            score += 3
        } else if (temperature >= 42f) {
            score += 1
        }

        if (!networkValidated) {
            score += 1
        }

        level =
            when {
                score >= 7 ->
                    Level.CRITICAL

                score >= 4 ->
                    Level.WARNING

                score >= 1 ->
                    Level.NORMAL

                else ->
                    Level.SILENT
            }

        return level
    }

    fun intervalMs(): Long {

        return when (level) {

            Level.SILENT ->
                5000L

            Level.NORMAL ->
                3000L

            Level.WARNING ->
                750L

            Level.CRITICAL ->
                250L
        }
    }

    fun currentLevel(): Level {
        return level
    }
}
