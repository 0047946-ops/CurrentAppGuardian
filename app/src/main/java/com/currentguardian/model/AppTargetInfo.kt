package com.currentguardian.model

data class AppTargetInfo(
    val packageName: String,
    val label: String,
    val launchable: Boolean,
    val protectedSystemApp: Boolean
) {

    fun isEligibleTarget(
        ownPackageName: String
    ): Boolean {

        if (
            packageName.isBlank()
        ) {
            return false
        }

        if (
            packageName ==
            ownPackageName
        ) {
            return false
        }

        return launchable &&
            !protectedSystemApp
    }

    fun statusText():
        String {

        return when {

            protectedSystemApp ->
                "系統程式：無法由管家選擇與啟動"

            !launchable ->
                "目前無法選擇與啟動"

            else ->
                "可以選擇與啟動"
        }
    }
}
