package com.example.genericspaceship

import androidx.compose.ui.unit.IntSize

object SpaceshipConstants {

    const val MIN_SPEED = 0f
    const val MAX_SPEED = 4f

    /**
     * Amount to increase acceleration values per tick, while thruster is engaged.
     */
    const val ACCELERATION_RATE = 0.1f

    /**
     * Amount to decrease acceleration values per tick, when thruster is disengaged.
     */
    const val ACCELERATION_DAMPING = 0.5f

    /**
     * Floor for acceleration values (so that damping is limited).
     */
    const val MIN_ACCELERATION = 0f

    /**
     * Ceiling for acceleration values.
     */
    const val MAX_ACCELERATION = MAX_SPEED * 0.1f
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
        var speedX: Float = SpaceshipConstants.MIN_SPEED,
        var speedY: Float = SpaceshipConstants.MIN_SPEED,
        /**
         * Change in [speedX] per tick
         */
        var accelerationX: Float = SpaceshipConstants.MIN_ACCELERATION,
        /**
         * Change in [speedY] per tick
         */
        var accelerationY: Float = SpaceshipConstants.MIN_ACCELERATION,
    ) {
        /**
         * Bounding box width.
         */
        val width
            get() = length * 0.8f
    }
}
