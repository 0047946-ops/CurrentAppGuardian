package com.currentguardian.network

class NetworkTransitionDetector {

    private var previousType:
        String? = null

    fun update(
        currentType: String
    ): Transition {

        val previous =
            previousType

        previousType =
            currentType

        if (previous == null) {

            return Transition.NONE
        }

        if (
            previous != currentType
        ) {

            return Transition.CHANGED(
                previous,
                currentType
            )
        }

        return Transition.NONE
    }

    sealed class Transition {

        data object NONE :
            Transition()

        data class CHANGED(
            val from: String,
            val to: String
        ) : Transition()
    }

    fun reset() {

        previousType =
            null
    }
}
