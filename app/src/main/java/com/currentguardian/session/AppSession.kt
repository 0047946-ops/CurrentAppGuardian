package com.currentguardian.session

import com.currentguardian.model.CurrentAppInfo

class AppSession(
    val app: CurrentAppInfo,
    val startedAt: Long =
        System.currentTimeMillis()
) {

    private var active =
        true

    private var lastEvent =
        "SESSION_STARTED"

    fun markEvent(
        event: String
    ) {

        if (
            event.isBlank()
        ) {
            return
        }

        lastEvent =
            event
    }

    fun isActive(): Boolean {
        return active
    }

    fun end() {

        active =
            false

        lastEvent =
            "SESSION_ENDED"
    }

    fun lastEvent():
        String {

        return lastEvent
    }

    fun durationMs():
        Long {

        return System.currentTimeMillis() -
            startedAt
    }
}
