package com.currentguardian.blackbox

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class PersistentJournal(
    context: Context
) {

    private val directory =
        File(
            context.filesDir,
            "blackbox"
        ).apply {
            mkdirs()
        }

    private val activeFile =
        File(
            directory,
            "guardian_live.journal"
        )

    private val lock = Any()

    fun append(line: String) {

        synchronized(lock) {

            try {

                FileOutputStream(
                    activeFile,
                    true
                ).use { output ->

                    output.write(
                        (line + "\n")
                            .toByteArray(
                                StandardCharsets.UTF_8
                            )
                    )

                    output.flush()

                    /*
                     * Phase 2：
                     * 每次寫入立即 flush，
                     * 優先確保事故資料存在。
                     *
                     * 後續版本可加入
                     * 批次寫入與 fsync 節流。
                     */
                }

            } catch (_: Exception) {
                /*
                 * 不讓 I/O 例外反過來殺死管家。
                 */
            }
        }
    }

    fun rotate(
        reason: String
    ) {

        synchronized(lock) {

            try {

                if (!activeFile.exists()) {
                    return
                }

                val target =
                    File(
                        directory,
                        "incident_" +
                            System.currentTimeMillis() +
                            ".journal"
                    )

                activeFile.renameTo(target)

                File(
                    directory,
                    "rotation_reason.txt"
                ).appendText(
                    "${System.currentTimeMillis()}|$reason\n"
                )

            } catch (_: Exception) {
            }
        }
    }

    fun getActiveFile(): File {
        return activeFile
    }
}
