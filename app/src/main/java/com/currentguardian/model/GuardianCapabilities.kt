package com.currentguardian.model

data class GuardianCapabilities(

    val usageAccess: Boolean,

    val notifications: Boolean,

    val foregroundServiceAvailable: Boolean,

    val backgroundRestricted: Boolean,

    val canObserveCurrentApp: Boolean,

    val canReadBatteryState: Boolean,

    val canReadNetworkState: Boolean,

    val canPersistStorage: Boolean,

    val batteryOptimizationIgnored: Boolean

) {

    fun score(): Int {

        var total = 0

        if (usageAccess) total++

        if (notifications) total++

        if (foregroundServiceAvailable) total++

        if (!backgroundRestricted) total++

        if (canObserveCurrentApp) total++

        if (canReadBatteryState) total++

        if (canReadNetworkState) total++

        if (canPersistStorage) total++

        if (batteryOptimizationIgnored) total++

        return total
    }

    fun level(): String {

        val score = score()

        return when {

            score >= 8 ->
                "ADVANCED"

            score >= 5 ->
                "STANDARD"

            score >= 2 ->
                "LIMITED"

            else ->
                "BASIC"
        }
    }
}
