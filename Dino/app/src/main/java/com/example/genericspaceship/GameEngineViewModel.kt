package com.example.genericspaceship

import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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

    fun onCanvasSizeChange(newSize: IntSize) {
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
        // TODO: fire
    }

    fun onRotate(rotationPixels: Float) {
        state.spaceship.rotationDegrees += rotationPixels
    }

    private fun update() {
        val spaceship = state.spaceship

        if (spaceship.thrustersEngaged) {
            spaceship.applyThrust()
        } else {
            spaceship.applyFriction()
        }

        spaceship.positionX += spaceship.thrustX
        spaceship.positionY += spaceship.thrustY
        spaceship.stayOnScreen()

        emitLatestState()
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
        val halfLength = length * 0.5f
        if (positionX + halfLength < 0) {
            positionX = state.size.width + halfLength
        } else if (positionX - halfLength > state.size.width) {
            positionX = 0 - halfLength
        }
        if (positionY + halfLength < 0) {
            positionY = state.size.height + halfLength
        } else if (positionY - halfLength > state.size.height) {
            positionY = 0 - halfLength
        }
    }

    private fun emitLatestState() {
        _uiState.update {
            UiState(
                spaceship = UiState.Spaceship(
                    width = state.spaceship.width,
                    length = state.spaceship.length,
                    positionX = state.spaceship.positionX,
                    positionY = state.spaceship.positionY,
                    rotationDegrees = state.spaceship.rotationDegrees
                )
            )
        }
    }
}
