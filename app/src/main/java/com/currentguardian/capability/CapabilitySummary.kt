package com.currentguardian.capability

import com.currentguardian.model.GuardianCapabilities

data class CapabilitySummary(
    val level: String,
    val score: Int,
    val canObserveCurrentApp: Boolean,
    val canReadNetwork: Boolean,
    val canReadBattery: Boolean,
    val canPersistEvidence: Boolean,
    val backgroundRestricted: Boolean,
    val batteryOptimizationIgnored: Boolean
) {

    companion object {

        fun from(
            capabilities:
                GuardianCapabilities
        ): CapabilitySummary {

            return CapabilitySummary(
                level =
                    capabilities.level(),

                score =
                    capabilities.score(),

                canObserveCurrentApp =
                    capabilities
                        .canObserveCurrentApp,

                canReadNetwork =
                    capabilities
                        .canReadNetworkState,

                canReadBattery =
                    capabilities
                        .canReadBatteryState,

                canPersistEvidence =
                    capabilities
                        .canPersistStorage,

                backgroundRestricted =
                    capabilities
                        .backgroundRestricted,

                batteryOptimizationIgnored =
                    capabilities
                        .batteryOptimizationIgnored
            )
        }
    }

    fun displayText(): String {

        return buildString {

            appendLine(
                "管家能力：$level"
            )

            appendLine(
                "能力分數：$score"
            )

            appendLine(
                "Current App："
            )

            appendLine(
                if (canObserveCurrentApp) {
                    "可觀測"
                } else {
                    "受限制"
                }
            )

            appendLine(
                "網路："
            )

            appendLine(
                if (canReadNetwork) {
                    "可取得"
                } else {
                    "受限制"
                }
            )

            appendLine(
                "電池："
            )

            appendLine(
                if (canReadBattery) {
                    "可取得"
                } else {
                    "受限制"
                }
            )

            appendLine(
                "事故保存："
            )

            appendLine(
                if (canPersistEvidence) {
                    "可使用"
                } else {
                    "受限制"
                }
            )

            appendLine(
                "背景限制："
            )

            appendLine(
                if (backgroundRestricted) {
                    "目前受限制"
                } else {
                    "未偵測到背景限制"
                }
            )
        }
    }
}
