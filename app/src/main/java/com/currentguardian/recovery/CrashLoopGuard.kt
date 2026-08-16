package com.currentguardian.recovery

class CrashLoopGuard(
    private val maxAttempts: Int = 3,
    private val cooldownMs: Long = 15_000L
) {

    private var attempts =
        0

    private var lastCrashTime =
        0L

    fun registerCrash():
        Boolean {

        val now =
            System.currentTimeMillis()

        if (
            lastCrashTime > 0L &&
            now - lastCrashTime >
                cooldownMs
        ) {

            attempts = 0
        }

        attempts++

        lastCrashTime =
            now

        return attempts <=
            maxAttempts
    }

    fun registerStableRun() {

        attempts = 0
        lastCrashTime = 0L
    }

    fun attempts():
        Int {

        return attempts
    }

    fun blocked():
        Boolean {

        return attempts >=
            maxAttempts
    }

    fun reset() {

        attempts = 0
        lastCrashTime = 0L
    }
}
