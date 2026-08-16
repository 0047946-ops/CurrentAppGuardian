package com.currentguardian.blackbox

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlackBox(
    context: Context
) {

    private val directory =
        File(
            context.filesDir,
            "blackbox"
        ).apply {
            mkdirs()
        }

    private val journal =
        File(
            directory,
            "current_blackbox.log"
        )

    private val lock = Any()

    private var counter = 0

    fun record(data: String) {

        synchronized(lock) {

            try {

                journal.appendText(
                    data +
                    "\n---\n"
                )

                counter++

                /*
                 * 不讓黑盒無限增長。
                 * 第一階段採用簡單輪替。
                 */
                if (
                    journal.length() >
                    2L * 1024L * 1024L
                ) {
                    rotate()
                }

            } catch (_: Exception) {
                /*
                 * 黑盒寫入失敗不能讓管家本身崩潰。
                 */
            }
        }
    }

    private fun rotate() {

        val stamp =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(
                Date()
            )

        val backup =
            File(
                directory,
                "blackbox_$stamp.log"
            )

        journal.renameTo(backup)

        journal.createNewFile()
    }

    fun flush() {

        synchronized(lock) {

            try {

                journal.appendText(
                    "BLACKBOX_FLUSH\n"
                )

            } catch (_: Exception) {
            }
        }
    }
}
