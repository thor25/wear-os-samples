package com.example.dino.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

class GameWorldViewModel(
    private val clock: Clock = object : Clock {
        override val currentTimeMillis: Long = System.currentTimeMillis()
    }
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        flow {
            while (true) {
                emit(Unit)
                delay(1)
            }
        }
    }

    private fun createInitialState(): UiState {
        return UiState(
            DinoState(AvatarState.WAITING, yPos = 0)
        )
    }

    fun onReceiveJumpInput() {
        _uiState.update {
            it.copy(dinoState = DinoState(AvatarState.JUMPING, 0))
        }
    }
}

interface Clock {
    val currentTimeMillis: Long
}

data class UiState(
    val dinoState: DinoState
)

data class DinoState(
    val avatarState: AvatarState,
    val yPos: Int
)
