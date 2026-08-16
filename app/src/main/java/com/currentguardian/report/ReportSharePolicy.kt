package com.currentguardian.report

data class ReportSharePolicy(
    val includeDeviceInfo: Boolean = true,
    val includeNetworkInfo: Boolean = true,
    val includeTimeline: Boolean = true,
    val includeBlackBox: Boolean = true,
    val includePersonalNotes: Boolean = false
)
