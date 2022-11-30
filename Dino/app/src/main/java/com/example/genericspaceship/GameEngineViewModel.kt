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

    fun onTap() {

    }

    fun onRotate(rotationPixels: Float) {
        state.spaceship.rotationDegrees += rotationPixels
    }

    private fun tickLoop() {
        state.ticks++
        emitLatestState()
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
