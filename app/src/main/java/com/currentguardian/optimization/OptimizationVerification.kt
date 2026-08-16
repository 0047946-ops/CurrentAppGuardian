package com.currentguardian.optimization

import com.currentguardian.model.PerformanceBaseline

data class OptimizationVerification(

    val result: Result,

    val cpuDelta:
        Int?,

    val ramDeltaMb:
        Long?,

    val temperatureDeltaC:
        Float?,

    val latencyDeltaMs:
        Long?,

    val jitterDeltaMs:
        Long?,

    val fpsDelta:
        Double?,

    val details:
        String
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
        ):
            OptimizationVerification {

            val cpuDelta =
                delta(
                    before.cpu,
                    after.cpu
                )

            val ramDeltaMb =
                delta(
                    before.ramMb,
                    after.ramMb
                )

            val temperatureDeltaC =
                delta(
                    before.temperatureC,
                    after.temperatureC
                )

            val latencyDeltaMs =
                delta(
                    before.latencyMs,
                    after.latencyMs
                )

            val jitterDeltaMs =
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
                ramDeltaMb == null &&
                temperatureDeltaC == null &&
                latencyDeltaMs == null &&
                jitterDeltaMs == null &&
                fpsDelta == null
            ) {

                return OptimizationVerification(

                    result =
                        Result.INSUFFICIENT_DATA,

                    cpuDelta =
                        null,

                    ramDeltaMb =
                        null,

                    temperatureDeltaC =
                        null,

                    latencyDeltaMs =
                        null,

                    jitterDeltaMs =
                        null,

                    fpsDelta =
                        null,

                    details =
                        "沒有足夠資料進行最佳化前後比較。"
                )
            }

            var improvementCount =
                0

            var degradationCount =
                0

            /*
             * FPS：
             * 上升通常是正向。
             * 明顯下降通常是負向。
             */
            fpsDelta?.let {

                when {

                    it > 2.0 ->
                        improvementCount++

                    it < -2.0 ->
                        degradationCount++
                }
            }

            /*
             * CPU：
             * 相同工作量下，下降通常較有利。
             */
            cpuDelta?.let {

                when {

                    it < -5 ->
                        improvementCount++

                    it > 5 ->
                        degradationCount++
                }
            }

            /*
             * RAM：
             * 這裡只比較「觀測結果」，
             * 不代表 RAM 越低一定越好。
             */
            ramDeltaMb?.let {

                when {

                    it < -100 ->
                        improvementCount++

                    it > 100 ->
                        degradationCount++
                }
            }

            /*
             * 溫度：
             * 降低通常較有利。
             */
            temperatureDeltaC?.let {

                when {

                    it < -1f ->
                        improvementCount++

                    it > 1f ->
                        degradationCount++
                }
            }

            /*
             * RTT：
             * 降低通常較有利。
             */
            latencyDeltaMs?.let {

                when {

                    it < -5L ->
                        improvementCount++

                    it > 5L ->
                        degradationCount++
                }
            }

            /*
             * Jitter：
             * 降低通常較有利。
             */
            jitterDeltaMs?.let {

                when {

                    it < -3L ->
                        improvementCount++

                    it > 3L ->
                        degradationCount++
                }
            }

            val result =
                when {

                    improvementCount >=
                        degradationCount + 2 ->

                        Result.IMPROVED

                    degradationCount >=
                        improvementCount + 2 ->

                        Result.DEGRADED

                    else ->

                        Result.NO_CLEAR_CHANGE
                }

            val details =
                buildString {

                    append(
                        "optimization_verification"
                    )

                    append(
                        "|improvement_count="
                    )

                    append(
                        improvementCount
                    )

                    append(
                        "|degradation_count="
                    )

                    append(
                        degradationCount
                    )

                    append(
                        "|cpu_delta="
                    )

                    append(
                        cpuDelta
                            ?: "UNKNOWN"
                    )

                    append(
                        "|ram_delta_mb="
                    )

                    append(
                        ramDeltaMb
                            ?: "UNKNOWN"
                    )

                    append(
                        "|temperature_delta_c="
                    )

                    append(
                        temperatureDeltaC
                            ?: "UNKNOWN"
                    )

                    append(
                        "|latency_delta_ms="
                    )

                    append(
                        latencyDeltaMs
                            ?: "UNKNOWN"
                    )

                    append(
                        "|jitter_delta_ms="
                    )

                    append(
                        jitterDeltaMs
                            ?: "UNKNOWN"
                    )

                    append(
                        "|fps_delta="
                    )

                    append(
                        fpsDelta
                            ?: "UNKNOWN"
                    )
                }

            return OptimizationVerification(

                result =
                    result,

                cpuDelta =
                    cpuDelta,

                ramDeltaMb =
                    ramDeltaMb,

                temperatureDeltaC =
                    temperatureDeltaC,

                latencyDeltaMs =
                    latencyDeltaMs,

                jitterDeltaMs =
                    jitterDeltaMs,

                fpsDelta =
                    fpsDelta,

                details =
                    details
            )
        }

        private fun delta(
            before:
                Int?,

            after:
                Int?
        ):
            Int? {

            if (
                before == null ||
                after == null
            ) {

                return null
            }

            return after - before
        }

        private fun delta(
            before:
                Long?,

            after:
                Long?
        ):
            Long? {

            if (
                before == null ||
                after == null
            ) {

                return null
            }

            return after - before
        }

        private fun delta(
            before:
                Float?,

            after:
                Float?
        ):
            Float? {

            if (
                before == null ||
                after == null
            ) {

                return null
            }

            return after - before
        }

        private fun delta(
            before:
                Double?,

            after:
                Double?
        ):
            Double? {

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
