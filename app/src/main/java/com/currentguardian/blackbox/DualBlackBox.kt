package com.currentguardian.blackbox

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class DualBlackBox(
    context: Context,
    private val ringCapacity: Int = 300
) {

    private val root =
        File(
            context.filesDir,
            "guardian_blackbox"
        ).apply {
            mkdirs()
        }

    private val incidentDirectory =
        File(
            root,
            "incidents"
        ).apply {
            mkdirs()
        }

    private val ring =
        ArrayDeque<String>(
            ringCapacity
        )

    private val journal =
        File(
            root,
            "persistent.journal"
        )

    private val lock =
        Any()

    fun record(
        line: String
    ) {

        synchronized(lock) {

            addToRing(line)

            appendPersistent(line)
        }
    }

    private fun addToRing(
        line: String
    ) {

        if (
            ring.size >=
            ringCapacity
        ) {
            ring.removeFirst()
        }

        ring.addLast(line)
    }

    private fun appendPersistent(
        line: String
    ) {

        try {

            FileOutputStream(
                journal,
                true
            ).use { output ->

                output.write(
                    (
                        line + "\n"
                    ).toByteArray(
                        StandardCharsets.UTF_8
                    )
                )

                output.flush()
            }

        } catch (_: Exception) {
            /*
             * 黑盒 I/O 失敗不能反殺管家。
             */
        }
    }

    fun snapshotRing():
        List<String> {

        synchronized(lock) {
            return ring.toList()
        }
    }

    fun preserveIncident(
        classification:
            String,
        finalState:
            String
    ): File? {

        synchronized(lock) {

            return try {

                val timestamp =
                    System.currentTimeMillis()

                val file =
                    File(
                        incidentDirectory,
                        "incident_" +
                            timestamp +
                            ".txt"
                    )

                file.bufferedWriter().use {

                    writer ->

                    writer.appendLine(
                        "CURRENT APPLICATION GUARDIAN"
                    )

                    writer.appendLine(
                        "INCIDENT"
                    )

                    writer.appendLine(
                        "TIME=$timestamp"
                    )

                    writer.appendLine(
                        "CLASSIFICATION=" +
                            classification
                    )

                    writer.appendLine(
                        "FINAL_STATE"
                    )

                    writer.appendLine(
                        finalState
                    )

                    writer.appendLine(
                        "ROLLING_BLACK_BOX"
                    )

                    ring.forEach {

                        writer.appendLine(
                            it
                        )
                    }

                    writer.appendLine(
                        "END_INCIDENT"
                    )
                }

                file

            } catch (_: Exception) {

                null
            }
        }
    }

    fun writeHeartbeat(
        heartbeat: String
    ) {

        record(
            "WATCHDOG_HEARTBEAT|" +
                heartbeat
        )
    }

    fun rootDirectory():
        File {

        return root
    }

    fun persistentFile():
        File {

        return journal
    }
}
