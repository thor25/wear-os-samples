package com.example.genericspaceship

import androidx.compose.ui.unit.IntSize

object SpaceshipConstants {

    const val MAX_THRUST = 12f
    const val THRUST_RATE = 0.25f
    const val FRICTION = 0.05f
}

data class GameState(
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
        var thrustX: Float = 0f,
        var thrustY: Float = 0f,
        var thrustersEngaged: Boolean = false,
    ) {
        /**
         * Bounding box width.
         */
        val width
            get() = length * 0.8f

        val rotationRads
            get() = rotationDegrees / 180 * Math.PI
    }
}
