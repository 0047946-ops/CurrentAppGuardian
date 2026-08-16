package com.currentguardian.phase8

import com.currentguardian.blackbox.DualEvidenceVault
import com.currentguardian.model.GuardianCapabilities
import com.currentguardian.model.PerformanceBaseline

class Phase8AcceptanceEngine(
    private val evidence:
        DualEvidenceVault
) {

    private val results =
        mutableListOf<Phase8TestResult>()

    fun record(
        result:
            Phase8TestResult
    ) {

        results.add(
            result
        )

        evidence.recordGuardian(
            result.serialize()
        )
    }

    fun evaluateCapability(
        capabilities:
            GuardianCapabilities
    ) {

        val passed =
            capabilities.level()
                != "BASIC"

        record(
            Phase8TestResult(
                testCase =
                    Phase8TestCase
                        .DEVICE_COMPATIBILITY,

                passed =
                    passed,

                durationMs =
                    0L,

                details =
                    "capability=" +
                    capabilities.level()
            )
        )
    }

    fun verifyOptimization(
        before:
            PerformanceBaseline,

        after:
            PerformanceBaseline
    ) {

        val verification =
            com.currentguardian
                .optimization
                .OptimizationVerification
                .compare(
                    before,
                    after
                )

        record(
            Phase8TestResult(
                testCase =
                    Phase8TestCase
                        .OPTIMIZATION_VERIFICATION,

                passed =
                    verification.result !=
                        com.currentguardian
                            .optimization
                            .OptimizationVerification
                            .Result.DEGRADED,

                durationMs =
                    0L,

                details =
                    verification
                        .details
            )
        )
    }

    fun evaluateBlackBoxes(
        targetEvidenceExists:
            Boolean,

        guardianEvidenceExists:
            Boolean
    ) {

        val passed =
            targetEvidenceExists ||
                guardianEvidenceExists

        record(
            Phase8TestResult(
                testCase =
                    Phase8TestCase
                        .TARGET_BLACKBOX,

                passed =
                    passed,

                durationMs =
                    0L,

                details =
                    "target=$targetEvidenceExists;" +
                    "guardian=$guardianEvidenceExists"
            )
        )

        record(
            Phase8TestResult(
                testCase =
                    Phase8TestCase
                        .GUARDIAN_BLACKBOX,

                passed =
                    guardianEvidenceExists,

                durationMs =
                    0L,

                details =
                    "guardian_evidence=" +
                    guardianEvidenceExists
            )
        )
    }

    fun allResults():
        List<Phase8TestResult> {

        return results.toList()
    }

    fun passedCount():
        Int {

        return results.count {
            it.passed
        }
    }

    fun failedCount():
        Int {

        return results.count {
            !it.passed
        }
    }

    fun overallPassed():
        Boolean {

        if (results.isEmpty()) {
            return false
        }

        /*
         * P8 不允許「平均分數掩蓋致命問題」。
         *
         * 只要雙黑盒、管家自保或事故報告
         * 等核心能力失敗，就不能宣告正式通過。
         */

        val critical =
            results.filter {
                it.testCase ==
                    Phase8TestCase
                        .TARGET_BLACKBOX ||
                it.testCase ==
                    Phase8TestCase
                        .GUARDIAN_BLACKBOX ||
                it.testCase ==
                    Phase8TestCase
                        .APP_UNEXPECTED_EXIT ||
                it.testCase ==
                    Phase8TestCase
                        .GUARDIAN_ABNORMAL_EXIT
            }

        val criticalPassed =
            critical.all {
                it.passed
            }

        val normalPassed =
            passedCount() >=
                (
                    results.size * 0.90
                ).toInt()

        return criticalPassed &&
            normalPassed
    }
}
