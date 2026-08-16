package com.currentguardian.monitor

class UserEventMarker(
    private val tracker: UniversalEventTracker
) {

    fun mark(
        label: String
    ) {

        tracker.markUserEvent(
            label = label
        )
    }

    fun mark(
        label: String,
        detail: String
    ) {

        tracker.markUserEvent(
            label = label,
            detail = detail
        )
    }
}
