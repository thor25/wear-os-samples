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
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.cos
import kotlin.math.sin

class GameEngineViewModel(
    private val state: GameState = GameState(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        flow<Unit> {
            while (true) {
                if (state.size != IntSize.Zero) {
                    tickLoop()
                }
                delay(42L)
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

    fun onTap() {
        // TODO: fire
    }

    fun onRotate(rotationPixels: Float) {
        state.spaceship.rotationDegrees += rotationPixels
    }

    private fun tickLoop() {
        state.ticks++
        val spaceship = state.spaceship

        if (spaceship.thrustersEngaged) {
            spaceship.calculateSpeedIncrease()
        } else {
            spaceship.calculateSpeedReduction()
        }

        spaceship.positionX += spaceship.translateX
        spaceship.positionY += spaceship.translateY

        // teleport the ship if it goes offscreen
        val halfLength = spaceship.length * 0.5f
        if (spaceship.positionX + halfLength < 0) {
            spaceship.positionX = state.size.width + halfLength
        } else if  (spaceship.positionX - halfLength > state.size.width){
            spaceship.positionX = 0 - halfLength
        }
        if (spaceship.positionY + halfLength < 0) {
            spaceship.positionY = state.size.height + halfLength
        } else if  (spaceship.positionY - halfLength > state.size.height){
            spaceship.positionY = 0 - halfLength
        }

        emitLatestState()
    }

    /**
     * Update [GameState.Spaceship.translateX] and [GameState.Spaceship.translateY] accounting for
     * for the rotation so that the ship moves towards the direction it's pointing.
     */
    private fun GameState.Spaceship.calculateSpeedIncrease() {
        val angle = rotationDegrees.toDouble()
        translateX -= (SpaceshipConstants.ACCELERATION_RATE * sin(angle)).toFloat()
        translateY -= (SpaceshipConstants.ACCELERATION_RATE * cos(angle)).toFloat()
        clampTranslation()
    }

    private fun GameState.Spaceship.clampTranslation() {
        translateX = min(translateX, SpaceshipConstants.MAX_TRANSLATE)
        translateX = max(translateX, SpaceshipConstants.MIN_TRANSLATE)

        translateY = min(translateY, SpaceshipConstants.MAX_TRANSLATE)
        translateY = max(translateY, SpaceshipConstants.MIN_TRANSLATE)
    }

    /**
     * Update [GameState.Spaceship.translateX] and [GameState.Spaceship.translateY] so that it
     * trends towards 0 (no movement).
     */
    private fun GameState.Spaceship.calculateSpeedReduction() {
        // TODO: decay the translateXY so that it'll get to 0
        clampTranslation()
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
