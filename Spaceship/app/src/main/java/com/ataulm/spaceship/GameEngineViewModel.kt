package com.ataulm.spaceship

import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.math.*

private const val FPS = 30
private const val UPDATE_INTERVAL = 1000L / FPS

class GameEngineViewModel(
    private val state: GameState = GameState(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        flow<Unit> {
            while (true) {
                if (state.size != IntSize.Zero) {
                    update()
                }
                delay(UPDATE_INTERVAL)
            }
        }.launchIn(viewModelScope)
    }

    fun onCanvasSizeChange(newSize: IntSize, isRound: Boolean) {
        state.isRound = isRound
        if (state.size != newSize) {
            state.size = newSize
            state.spaceship.positionX = newSize.width * 0.5f
            state.spaceship.positionY = newSize.height * 0.5f
        }
    }

    fun onPressEventDown() {
        state.spaceship.thrustersEngaged = true
    }

    fun onPressEventUp() {
        state.spaceship.thrustersEngaged = false
    }

    fun onDoubleTap() {
        val spaceship = state.spaceship
        spaceship.thrustersEngaged = false
        state.shotsFired.add(
            GameState.Shot(
                positionX = spaceship.positionX,
                positionY = spaceship.positionY,
                bearingRads = spaceship.rotationRads,
                endPositionX = spaceship.positionX +
                        SpaceshipConstants.SHOT_RANGE_MULTIPLIER * SpaceshipConstants.SHOT_SPEED * cos(
                    spaceship.rotationRads
                ).toFloat(),
                endPositionY = spaceship.positionY +
                        SpaceshipConstants.SHOT_RANGE_MULTIPLIER * SpaceshipConstants.SHOT_SPEED * sin(
                    spaceship.rotationRads
                ).toFloat(),
            )
        )
    }

    fun onRotate(rotationPixels: Float) {
        state.spaceship.rotationDegrees += rotationPixels
    }

    private fun update() {
        state.ticks++

        val spaceship = state.spaceship
        if (spaceship.thrustersEngaged) {
            spaceship.applyThrust()
        } else {
            spaceship.applyFriction()
        }
        spaceship.positionX += spaceship.thrustX
        spaceship.positionY += spaceship.thrustY
        spaceship.stayOnScreen()

        state.shotsFired.forEach { shot ->
            shot.positionX += SpaceshipConstants.SHOT_SPEED * cos(shot.bearingRads).toFloat()
            shot.positionY += SpaceshipConstants.SHOT_SPEED * sin(shot.bearingRads).toFloat()
        }
        state.shotsFired.removeAll { shot -> shot.hasExhaustedRange() }

        emitLatestState()
    }

    private fun GameState.Shot.hasExhaustedRange(): Boolean {
        return positionX >= endPositionX && positionY >= endPositionY
    }

    /**
     * Update [GameState.Spaceship.thrustX] and [GameState.Spaceship.thrustY] accounting for
     * for the rotation so that the ship moves towards the direction it's pointing.
     */
    private fun GameState.Spaceship.applyThrust() {
        thrustX += (SpaceshipConstants.THRUST_RATE * cos(rotationRads)).toFloat()
        thrustY += (SpaceshipConstants.THRUST_RATE * sin(rotationRads)).toFloat()

        thrustX = thrustX.coerceIn(
            minimumValue = SpaceshipConstants.MAX_THRUST * -1,
            maximumValue = SpaceshipConstants.MAX_THRUST
        )
        thrustY = thrustY.coerceIn(
            minimumValue = SpaceshipConstants.MAX_THRUST * -1,
            maximumValue = SpaceshipConstants.MAX_THRUST
        )
    }

    /**
     * Update [GameState.Spaceship.thrustX] and [GameState.Spaceship.thrustY] so that it
     * trends towards 0 (no movement).
     */
    private fun GameState.Spaceship.applyFriction() {
        thrustX = when {
            abs(thrustX - 0) < SpaceshipConstants.FRICTION -> 0F
            thrustX > 0 -> thrustX - SpaceshipConstants.FRICTION
            else -> thrustX + SpaceshipConstants.FRICTION
        }
        thrustY = when {
            abs(thrustY - 0) < SpaceshipConstants.FRICTION -> 0F
            thrustY > 0 -> thrustY - SpaceshipConstants.FRICTION
            else -> thrustY + SpaceshipConstants.FRICTION
        }
    }

    /**
     * Teleport the ship if it goes offscreen
     */
    private fun GameState.Spaceship.stayOnScreen() {
        if (state.isRound) {
            stayOnScreenRound()
        } else {
            stayOnScreenRectangle()
        }
    }

    private fun GameState.Spaceship.stayOnScreenRectangle() {
        val halfLength = length * 0.5f
        if (positionX + halfLength < 0) {
            positionX = state.size.width.toFloat()
        } else if (positionX - halfLength > state.size.width) {
            positionX = 0f
        }
        if (positionY + halfLength < 0) {
            positionY = state.size.height.toFloat()
        } else if (positionY - halfLength > state.size.height) {
            positionY = 0f
        }
    }

    /**
     * For round devices, it looks really weird if we assume a square canvas because it takes longer
     * for the ship to reappear if it flies through the corners of the square which are further away
     * from the center of the screen than the sides.
     */
    private fun GameState.Spaceship.stayOnScreenRound() {
        val radius = state.size.width * 0.5
        val centerX = radius
        val centerY = radius

        // pythagoras' theorem to see if ship is outside of the circle
        val a = abs(positionX - centerX)
        val b = abs(positionY - centerY)
        val distFromCenter = sqrt(a.pow(2) + b.pow(2))

        val halfLength = length * 0.5f
        if (distFromCenter > radius + halfLength + 1) {
            positionX = when {
                positionX < centerX -> centerX + a
                positionX > centerX -> centerX - a
                else -> positionX
            }.toFloat()

            positionY = when {
                positionY < centerY -> centerY + b
                positionY > centerY -> centerY - b
                else -> positionY
            }.toFloat()
        }
    }

    private fun emitLatestState() {
        _uiState.update {
            UiState(
                ticks = state.ticks,
                spaceship = UiState.Spaceship(
                    width = state.spaceship.width,
                    length = state.spaceship.length,
                    positionX = state.spaceship.positionX,
                    positionY = state.spaceship.positionY,
                    rotationDegrees = state.spaceship.rotationDegrees,
                    thrustersEngaged = state.spaceship.thrustersEngaged
                ),
                shotsFired = state.shotsFired.map {
                    UiState.Shot(
                        positionX = it.positionX,
                        positionY = it.positionY
                    )
                }
            )
        }
    }
}

object SpaceshipConstants {

    const val MAX_THRUST = 12f
    const val THRUST_RATE = 0.25f
    const val FRICTION = 0.05f
    const val SHOT_SPEED = MAX_THRUST + 3f
    const val SHOT_RANGE_MULTIPLIER = 30f
}

data class GameState(
    var ticks: Long = 0L,
    var size: IntSize = IntSize.Zero,
    var isRound: Boolean = true,
    val shotsFired: MutableList<Shot> = mutableListOf(),
    val spaceship: Spaceship = Spaceship()
) {
    data class Shot(
        var positionX: Float,
        var positionY: Float,
        val bearingRads: Double,
        val endPositionX: Float,
        val endPositionY: Float
    )

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
            get() = length * 0.7f

        val rotationRads
            get() = rotationDegrees / 180 * Math.PI
    }
}
