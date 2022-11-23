package com.example.dino.presentation

import android.util.Log
import androidx.compose.runtime.withFrameMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dino.presentation.GameWorldState.DinoJumpState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn

class GameWorldViewModel(
    private val gameWorld: GameWorldState = GameWorldState(gameWorldTicks = 0)
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        flow<Unit> {
            while (true) {
                if (gameWorld.size.height != 0) {
                    onGameLoop()
                }
                delay(42)
            }
        }.launchIn(viewModelScope)
    }

    fun onCanvasResize(width: Int, height: Int) {
        gameWorld.size = UiState.CanvasSize(width, height)
    }

    private fun emitUpdatedDinoState() {
        _uiState.getAndUpdate { oldUiState ->
            UiState(
                gameWorldTicks = gameWorld.gameWorldTicks,
                dino = UiState.Dino(
                    avatarState = when (gameWorld.dinoJumpState) {
                        DinoJumpState.JUMPING -> AvatarState.JUMPING
                        DinoJumpState.FALLING -> AvatarState.JUMPING
                        DinoJumpState.RUNNING -> AvatarState.RUNNING
                    },
                    yPos = gameWorld.dinoY
                ),
                canvasSize = gameWorld.size
            )
        }
    }

    private fun onGameLoop() {
        gameWorld.gameWorldTicks = gameWorld.gameWorldTicks + 1
        Log.d("!!!", "loop: $this gwt ${gameWorld.gameWorldTicks}")

        when (gameWorld.dinoJumpState) {
            DinoJumpState.JUMPING -> {
                Log.d("!!!", "loop: jumping ${gameWorld.dinoY}")
                if (gameWorld.dinoY < gameWorld.size.height) {
                    gameWorld.dinoY += 20
                } else {
                    gameWorld.dinoJumpState = DinoJumpState.FALLING
                }
            }
            DinoJumpState.FALLING -> {
                Log.d("!!!", "loop: falling ${gameWorld.dinoY}")
                if (gameWorld.dinoY > 0f) {
                    gameWorld.dinoY -= 20
                } else {
                    gameWorld.dinoJumpState = DinoJumpState.RUNNING
                }
            }
            DinoJumpState.RUNNING -> {
                // TODO: no action i think
            }
        }

        emitUpdatedDinoState()
//        Log.d("!!!", "ticks: ${gameWorld.gameWorldTicks}")
    }

    private fun createInitialState(): UiState {
        return UiState(
            0,
            gameWorld.size,
            UiState.Dino(AvatarState.RUNNING, yPos = 0f)
        )
    }

    fun onReceiveJumpInput() {
        Log.d("!!!", "onReceiveJumpInput")
        if (gameWorld.dinoJumpState == DinoJumpState.RUNNING) {
            Log.d("!!!", "onReceiveJumpInput - JUMP")
            gameWorld.dinoJumpState = DinoJumpState.JUMPING
        }
    }
}

data class GameWorldState(
    var gameWorldTicks: Long = 0,
    var size: UiState.CanvasSize = UiState.CanvasSize(0, 0),
    var dinoY: Float = 0f,
    var dinoJumpState: DinoJumpState = DinoJumpState.RUNNING
) {
    enum class DinoJumpState {
        JUMPING,
        FALLING,
        RUNNING
    }
}
