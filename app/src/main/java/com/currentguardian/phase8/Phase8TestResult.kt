package com.currentguardian.phase8

data class Phase8TestResult(
    val testCase: Phase8TestCase,
    val passed: Boolean,
    val durationMs: Long,
    val details: String,
    val evidencePath: String? = null
) {

    fun serialize(): String {

        return buildString {

            append("P8_TEST")

            append("|case=")
            append(testCase.name)

            append("|passed=")
            append(passed)

            append("|duration_ms=")
            append(durationMs)

            append("|details=")
            append(
                details
                    .replace("|", "/")
                    .replace("\n", " ")
                    .replace("\r", " ")
            )

            append("|evidence=")
            append(
                evidencePath
                    ?: "NONE"
            )
        }
    }
}
