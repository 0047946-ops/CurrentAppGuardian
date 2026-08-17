package com.currentguardian.model

data class AppTargetInfo(
    val packageName: String,
    val label: String,
    val launchable: Boolean,
    val protectedSystemApp: Boolean
) {

    fun isEligibleTarget(
        guardianPackage:
            String
    ): Boolean {

        if (
            packageName.isBlank()
        ) {
            return false
        }

        if (
            packageName ==
            guardianPackage
        ) {
            return false
        }

        if (
            !launchable
        ) {
            return false
        }

        /*
         * 系統／廠商核心 App：
         * 不作為一般第三方最佳化目標。
         */
        if (
            protectedSystemApp
        ) {
            return false
        }

        return true
    }

    fun displayName():
        String {

        return "$label\n$packageName"
    }
}
