package com.currentguardian.session

import com.currentguardian.model.GuardianMode
import com.currentguardian.model.PerformanceBaseline

class AppSession(

    val packageName: String,

    val appLabel: String,

    val startedAt: Long =
        System.currentTimeMillis()
) {

    var mode:
        GuardianMode =
        GuardianMode.BALANCED

    var baseline:
        PerformanceBaseline? =
        null

    var optimizationApplied =
        false

    var crashCount =
        0

    var lastEvent:
        String =
        "SESSION_STARTED"

    fun markEvent(
        event: String
    ) {

        lastEvent =
            event
    }

    fun runtimeMs():
        Long {

        return System.currentTimeMillis() -
            startedAt
    }
}
