package com.currentguardian.model

enum class AppLifecycleState {
    UNKNOWN,
    NOT_DETECTED,
    FOREGROUND,
    BACKGROUND,
    RETURNED_TO_FOREGROUND,
    DISAPPEARED,
    EXITED
}
