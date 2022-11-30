package com.example.genericspaceship

import android.util.Log
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
    private val state: GameState = GameState()
) : ViewModel() {

    private val _uiState = MutableStateFlow(state)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

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
        Log.d("!!!", "uiState == state? ${_uiState.value == state}")
        Log.d("!!!", "uiState ${_uiState.value}")
        Log.d("!!!", "  state $state")
//        _uiState.update { state.copy() }
    }
}
