package com.currentguardian.blackbox

import android.content.Context
import com.currentguardian.model.GuardianEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidentManager(
    context: Context
) {

    private val directory =
        File(
            context.filesDir,
            "blackbox/incidents"
        ).apply {
            mkdirs()
        }

    private val ring =
        RingBuffer<String>(
            capacity = 240
        )

    private val journal =
        PersistentJournal(context)

    fun record(
        event: GuardianEvent
    ) {

        val serialized =
            event.serialize()

        ring.add(serialized)

        journal.append(
            "EVENT|$serialized"
        )
    }

    fun recordRaw(
        data: String
    ) {

        ring.add(data)

        journal.append(
            "RAW|$data"
        )
    }

    fun mark(
        type: String,
        detail: String,
        severity: Int = 0
    ) {

        record(
            GuardianEvent.create(
                type,
                detail,
                severity
            )
        )
    }

    fun sealIncident(
        reason: String
    ): File? {

        return try {

            val stamp =
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss_SSS",
                    Locale.US
                ).format(
                    Date()
                )

            val file =
                File(
                    directory,
                    "incident_$stamp.txt"
                )

            file.bufferedWriter().use { writer ->

                writer.appendLine(
                    "CURRENT APPLICATION GUARDIAN"
                )

                writer.appendLine(
                    "INCIDENT REPORT"
                )

                writer.appendLine(
                    "TIME=${System.currentTimeMillis()}"
                )

                writer.appendLine(
                    "REASON=$reason"
                )

                writer.appendLine(
                    "----- RING BUFFER -----"
                )

                ring.snapshot()
                    .forEach {
                        writer.appendLine(it)
                    }

                writer.appendLine(
                    "----- END -----"
                )
            }

            journal.rotate(
                "incident sealed: $reason"
            )

            file

        } catch (_: Exception) {
            null
        }
    }

    fun recentEvents(): List<String> {
        return ring.snapshot()
    }
}
