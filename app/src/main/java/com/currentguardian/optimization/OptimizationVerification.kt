package com.currentguardian.optimization

import com.currentguardian.model.PerformanceBaseline

data class OptimizationVerification(
    val result: Result,
    val cpuDelta: Int?,
    val ramDeltaMb: Long?,
    val temperatureDeltaC: Float?,
    val latencyDeltaMs: Long?,
    val jitterDeltaMs: Long?,
    val fpsDelta: Double?,
    val details: String
) {

    enum class Result {

        IMPROVED,

        NO_CLEAR_CHANGE,

        DEGRADED,

        INSUFFICIENT_DATA
    }

    companion object {

        fun compare(
            before:
                PerformanceBaseline,

            after:
                PerformanceBaseline
        ): OptimizationVerification {

            val cpuDelta =
                delta(
                    before.cpu,
                    after.cpu
                )

            val ramDelta =
                delta(
                    before.ramMb,
                    after.ramMb
                )

            val temperatureDelta =
                delta(
                    before.temperatureC,
                    after.temperatureC
                )

            val latencyDelta =
                delta(
                    before.latencyMs,
                    after.latencyMs
                )

            val jitterDelta =
                delta(
                    before.jitterMs,
                    after.jitterMs
                )

            val fpsDelta =
                delta(
                    before.fps,
                    after.fps
                )

            if (
                cpuDelta == null &&
                ramDelta == null &&
                temperatureDelta == null &&
                latencyDelta == null &&
                jitterDelta == null &&
                fpsDelta == null
            ) {

                return OptimizationVerification(
                    result =
                        Result.INSUFFICIENT_DATA,

                    cpuDelta = null,

                    ramDelta = null,

                    temperatureDeltaC = null,

                    latencyDeltaMs = null,

                    jitterDeltaMs = null,

                    fpsDelta = null,

                    details =
                        "沒有足夠資料進行前後比較。"
                )
            }

            var improvement = 0
            var degradation = 0

            /*
             * 這裡不把單一指標當作絕對答案。
             * FPS 上升通常是正向。
             * CPU、RAM、溫度、延遲、Jitter 下降通常是正向。
             */

            if (
                fpsDelta != null
            ) {

                if (fpsDelta > 2.0)
                    improvement++

                if (fpsDelta < -2.0)
                    degradation++
            }

            if (
                cpuDelta != null
            ) {

                if (cpuDelta < -5)
                    improvement++

                if (cpuDelta > 5)
                    degradation++
            }

            if (
                ramDelta != null
            ) {

                if (ramDelta < -100)
                    improvement++

                if (ramDelta > 100)
                    degradation++
            }

            if (
                temperatureDelta != null
            ) {

                if (temperatureDelta < -1f)
                    improvement++

                if (temperatureDelta > 1f)
                    degradation++
            }

            if (
                latencyDelta != null
            ) {

                if (latencyDelta < -5)
                    improvement++

                if (latencyDelta > 5)
                    degradation++
            }

            if (
                jitterDelta != null
            ) {

                if (jitterDelta < -3)
                    improvement++

                if (jitterDelta > 3)
                    degradation++
            }

            val result =
                when {

                    improvement >=
                        degradation + 2 ->
                        Result.IMPROVED

                    degradation >=
                        improvement + 2 ->
                        Result.DEGRADED

                    else ->
                        Result.NO_CLEAR_CHANGE
                }

            return OptimizationVerification(
                result =
                    result,

                cpuDelta =
                    cpuDelta,

                ramDeltaMb =
                    ramDelta,

                temperatureDeltaC =
                    temperatureDelta,

                latencyDeltaMs =
                    latencyDelta,

                jitterDeltaMs =
                    jitterDelta,

                fpsDelta =
                    fpsDelta,

                details =
                    "前後指標已完成比較。"
            )
        }

        private fun delta(
            before: Int?,
            after: Int?
        ): Int? {

            if (
                before == null ||
                after == null
            ) {
                return null
            }

            return after - before
        }

        private fun delta(
            before: Long?,
            after: Long?
        ): Long? {

            if (
                before == null ||
                after == null
            ) {
                return null
            }

            return after - before
        }

        private fun delta(
            before: Float?,
            after: Float?
        ): Float? {

            if (
                before == null ||
                after == null
            ) {
                return null
            }

            return after - before
        }

        private fun delta(
            before: Double?,
            after: Double?
        ): Double? {

            if (
                before == null ||
                after == null
            ) {
                return null
            }

            return after - before
        }
    }
}
