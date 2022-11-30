package com.example.genericspaceship

import androidx.compose.ui.unit.IntSize

object SpaceshipConstants {

    const val MIN_TRANSLATE = -4f
    const val MAX_TRANSLATE = MIN_TRANSLATE * -1
    const val ACCELERATION_RATE = 0.1f
    const val DECELERATION_RATE = 0.5f
}

data class GameState(
    var ticks: Long = 0,
    var size: IntSize = IntSize.Zero,
    val spaceship: Spaceship = Spaceship()
) {
    data class Spaceship(
        /**
         * Bounding box length.
         * TODO: should this be based on [GameState.size]?
         */
        var length: Float = 50f,
        /**
         * Rotation around the pivot point.
         */
        var rotationDegrees: Float = 0f,
        /**
         * Pivot point's x-offset from origin.
         */
        var positionX: Float = 0f,
        /**
         * Pivot point's y-offset from origin.
         */
        var positionY: Float = 0f,
        var translateX: Float = 0f,
        var translateY: Float = 0f,
        var thrustersEngaged: Boolean = false,
    ) {
        /**
         * Bounding box width.
         */
        val width
            get() = length * 0.8f
    }
}
