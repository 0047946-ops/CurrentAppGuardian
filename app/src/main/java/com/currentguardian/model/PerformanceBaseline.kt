package com.currentguardian.model

data class PerformanceBaseline(

    val timestamp: Long,

    val cpu: Int?,

    val ramMb: Long?,

    val temperatureC: Float?,

    val batteryPercent: Int?,

    val latencyMs: Long?,

    val jitterMs: Long?,

    val fps: Double?,

    val loadTimeMs: Long?
)
