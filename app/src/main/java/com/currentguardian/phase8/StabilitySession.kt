package com.currentguardian.phase8

import com.currentguardian.blackbox.DualEvidenceVault
import com.currentguardian.monitor.SystemSnapshot

class StabilitySession(
    private val evidence:
        DualEvidenceVault
) {

    private val startedAt =
        System.currentTimeMillis()

    private val samples =
        mutableListOf<SystemSnapshot>()

    private var completed =
        false

    fun record(
        snapshot:
            SystemSnapshot
    ) {

        if (completed) {
            return
        }

        samples.add(
            snapshot
        )

        evidence.recordGuardian(
            buildRecord(
                snapshot
            )
        )
    }

    private fun buildRecord(
        snapshot:
            SystemSnapshot
    ): String {

        return "P8_STABILITY_SAMPLE" +
            "|time=" +
            System.currentTimeMillis() +
            "|cpu=" +
            snapshot.cpuLoad +
            "|ram=" +
            snapshot.availableRamMb +
            "|temperature=" +
            snapshot.temperatureC +
            "|battery=" +
            snapshot.batteryPercent +
            "|network=" +
            snapshot.networkType
    }

    fun durationMs():
        Long {

        return System.currentTimeMillis() -
            startedAt
    }

    fun sampleCount():
        Int {

        return samples.size
    }

    fun evaluate():
        StabilityResult {

        if (samples.isEmpty()) {

            return StabilityResult(
                passed =
                    false,

                reason =
                    "沒有取得任何穩定性樣本。"
            )
        }

        val first =
            samples.first()

        val last =
            samples.last()

        /*
         * availableRamMb 是「可用 RAM」。
         * 因此：
         *
         * first - last > 0
         * 表示可用 RAM 下降。
         *
         * 這裡不把它直接稱為 memory leak，
         * 只能稱為「可用 RAM 下降」。
         */
        val availableRamDropMb =
            (
                first.availableRamMb -
                    last.availableRamMb
            )
                .coerceAtLeast(0L)

        val temperatureRise =
            (
                last.temperatureC -
                    first.temperatureC
            )
                .coerceAtLeast(0f)

        /*
         * 這只是 P8 初篩，
         * 不是最終硬體健康判定。
         */
        val healthy =
            availableRamDropMb <
                1024L &&
            temperatureRise <
                15f

        return StabilityResult(
            passed =
                healthy,

            reason =
                buildString {

                    append(
                        "samples="
                    )

                    append(
                        samples.size
                    )

                    append(
                        ";available_ram_drop_mb="
                    )

                    append(
                        availableRamDropMb
                    )

                    append(
                        ";temperature_rise="
                    )

                    append(
                        temperatureRise
                    )
                }
        )
    }

    fun finish() {

        if (completed) {
            return
        }

        completed =
            true

        evidence.recordGuardian(
            "P8_STABILITY_FINISHED" +
                "|duration_ms=" +
                durationMs() +
                "|samples=" +
                samples.size
        )
    }
}

data class StabilityResult(
    val passed: Boolean,
    val reason: String
)
