package com.currentguardian.phase8

import android.os.Build
import com.currentguardian.model.GuardianCapabilities

data class DeviceTestProfile(
    val manufacturer: String,
    val model: String,
    val sdk: Int,
    val capabilities:
        GuardianCapabilities,
    val notes: String = ""
) {

    fun serialize(): String {

        return buildString {

            append("DEVICE_PROFILE")

            append("|manufacturer=")
            append(
                manufacturer.safe()
            )

            append("|model=")
            append(
                model.safe()
            )

            append("|sdk=")
            append(sdk)

            append("|capability_level=")
            append(
                capabilities.level()
            )

            append("|capability_score=")
            append(
                capabilities.score()
            )

            append("|notes=")
            append(
                notes.safe()
            )
        }
    }

    companion object {

        fun create(
            capabilities:
                GuardianCapabilities,
            notes: String = ""
        ): DeviceTestProfile {

            return DeviceTestProfile(
                manufacturer =
                    Build.MANUFACTURER,

                model =
                    Build.MODEL,

                sdk =
                    Build.VERSION.SDK_INT,

                capabilities =
                    capabilities,

                notes =
                    notes
            )
        }
    }

    private fun String.safe():
        String {

        return replace("|", "/")
            .replace("\n", " ")
            .replace("\r", " ")
    }
}
