package com.currentguardian.phase8

import com.currentguardian.blackbox.DualEvidenceVault
import com.currentguardian.model.SystemSnapshot
import java.util.concurrent.CopyOnWriteArrayList

class StabilitySession(
    private val evidence:
        DualEvidenceVault
) {

    private val startedAt =
        System.currentTimeMillis()

    private val samples =
        CopyOnWriteArrayList<SystemSnapshot>()

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
            buildRecord(snapshot)
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
                passed = false,
                reason =
                    "沒有取得任何穩定性樣本。"
            )
        }

        val first =
            samples.first()

        val last =
            samples.last()

        val ramGrowth =
            first.availableRamMb -
                last.availableRamMb

        val temperatureRise =
            last.temperatureC -
                first.temperatureC

        val healthy =
            ramGrowth < 1024 &&
            temperatureRise < 15f

        return StabilityResult(
            passed = healthy,
            reason =
                buildString {

                    append(
                        "samples="
                    )

                    append(
                        samples.size
                    )

                    append(
                        ";ram_delta="
                    )

                    append(
                        ramGrowth
                    )

                    append(
                        ";temperature_delta="
                    )

                    append(
                        temperatureRise
                    )
                }
        )
    }

    fun finish() {

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
