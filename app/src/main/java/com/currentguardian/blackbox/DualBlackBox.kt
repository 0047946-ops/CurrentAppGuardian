package com.currentguardian.blackbox

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class DualBlackBox private constructor(
    private val root: File,
    private val ringCapacity: Int
) {

    constructor(
        context: Context,
        ringCapacity: Int = 300
    ) : this(
        File(
            context.filesDir,
            "guardian_blackbox"
        ),
        ringCapacity
    )

    constructor(
        root: File,
        ringCapacity: Int = 300
    ) : this(
        root = root,
        ringCapacity = ringCapacity
    )

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

    init {
        root.mkdirs()

        if (!journal.exists()) {
            journal.createNewFile()
        }
    }

    fun record(
        line: String
    ) {

        synchronized(lock) {

            if (
                ring.size >=
                ringCapacity
            ) {
                ring.removeFirst()
            }

            ring.addLast(line)

            appendPersistent(line)
        }
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

        } catch (
            _: Exception
        ) {
            /*
             * 黑盒寫入失敗不能反過來
             * 把管家程序殺掉。
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
        classification: String,
        finalState: String
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
                        writer.appendLine(it)
                    }

                    writer.appendLine(
                        "END_INCIDENT"
                    )
                }

                file

            } catch (
                _: Exception
            ) {

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
