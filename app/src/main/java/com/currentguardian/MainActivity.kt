package com.currentguardian

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    32,
                    32,
                    32,
                    32
                )
            }

        val title =
            TextView(this).apply {

                text =
                    "Current App Guardian"

                textSize =
                    28f

                gravity =
                    Gravity.CENTER
            }

        val status =
            TextView(this).apply {

                text =
                    "P1 基線正常啟動"

                textSize =
                    18f

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    24,
                    0,
                    0
                )
            }

        root.addView(
            title
        )

        root.addView(
            status
        )

        setContentView(
            root
        )
    }
}
