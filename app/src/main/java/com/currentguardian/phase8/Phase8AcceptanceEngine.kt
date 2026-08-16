package com.currentguardian.phase8

import com.currentguardian.blackbox.DualEvidenceVault
import com.currentguardian.model.GuardianCapabilities
import com.currentguardian.model.PerformanceBaseline
import com.currentguardian.optimization.OptimizationVerification

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
            capabilities.level() !=
                "BASIC"

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
            OptimizationVerification
                .compare(
                    before,
                    after
                )

        val passed =
            verification.result !=
                OptimizationVerification
                    .Result.DEGRADED

        record(
            Phase8TestResult(
                testCase =
                    Phase8TestCase
                        .OPTIMIZATION_VERIFICATION,

                passed =
                    passed,

                durationMs =
                    0L,

                details =
                    verification.details
            )
        )
    }

    fun evaluateBlackBoxes(
        targetEvidenceExists:
            Boolean,

        guardianEvidenceExists:
            Boolean
    ) {

        /*
         * 雙黑盒必須分別成功。
         * 不能用 guardian || target
         * 掩蓋其中一個失敗。
         */

        record(
            Phase8TestResult(
                testCase =
                    Phase8TestCase
                        .TARGET_BLACKBOX,

                passed =
                    targetEvidenceExists,

                durationMs =
                    0L,

                details =
                    "target=" +
                    targetEvidenceExists
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
                    "guardian=" +
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

        if (
            results.isEmpty()
        ) {
            return false
        }

        val criticalResults =
            results.filter { result ->

                when (
                    result.testCase
                ) {

                    Phase8TestCase
                        .TARGET_BLACKBOX,

                    Phase8TestCase
                        .GUARDIAN_BLACKBOX,

                    Phase8TestCase
                        .APP_UNEXPECTED_EXIT,

                    Phase8TestCase
                        .GUARDIAN_ABNORMAL_EXIT ->

                        true

                    else ->
                        false
                }
            }

        val criticalPassed =
            criticalResults.all {
                it.passed
            }

        /*
         * 至少 90% 的全部測試案例通過。
         *
         * ceil：
         * 例如 11 個案例至少要 10 個，
         * 而不是 9 個。
         */
        val required =
            kotlin.math.ceil(
                results.size * 0.90
            ).toInt()

        val normalPassed =
            passedCount() >=
                required

        return criticalPassed &&
            normalPassed
    }
}
