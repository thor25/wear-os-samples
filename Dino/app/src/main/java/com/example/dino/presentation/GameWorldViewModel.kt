package com.example.dino.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dino.presentation.GameWorldState.DinoJumpState
import com.example.dino.presentation.GameWorldState.DinoJumpState.FALLING
import com.example.dino.presentation.GameWorldState.DinoJumpState.JUMPING
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update

private const val MILLIS_PER_FRAME_24FPS = (1000 / 24).toLong()

private const val JUMP_SPEED = 20

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
                delay(MILLIS_PER_FRAME_24FPS)
            }
        }.launchIn(viewModelScope)
    }

    fun onCanvasResize(width: Int, height: Int) {
        if (gameWorld.size.width != width || gameWorld.size.height != height) {
            gameWorld.size = UiState.CanvasSize(width, height)
            gameWorld.dinoYPosFeet = gameWorld.size.groundY
        }
    }

    private fun emitUpdatedDinoState() {
        _uiState.update {
            UiState(
                gameWorldTicks = gameWorld.gameWorldTicks,
                dino = UiState.Dino(
                    avatarState = when (gameWorld.dinoJumpState) {
                        DinoJumpState.RUNNING -> AvatarState.RUNNING
                        else -> AvatarState.JUMPING
                    },
                    yPosFeet = gameWorld.dinoYPosFeet
                ),
                canvasSize = gameWorld.size
            )
        }
    }

    private fun onGameLoop() {
        gameWorld.gameWorldTicks++

        if (gameWorld.dinoJumpState == JUMPING) {
            // we don't want dino's feet to go within 100px of the top edge
            if (gameWorld.dinoYPosFeet >= 100) {
                gameWorld.dinoYPosFeet -= JUMP_SPEED
            } else {
                gameWorld.dinoJumpState = FALLING
            }
        }
        if (gameWorld.dinoJumpState == FALLING) {
            if (gameWorld.dinoYPosFeet < gameWorld.size.groundY) {
                gameWorld.dinoYPosFeet += JUMP_SPEED
            } else {
                gameWorld.dinoYPosFeet = gameWorld.size.groundY
                gameWorld.dinoJumpState = DinoJumpState.RUNNING
            }
        }

        emitUpdatedDinoState()
    }

    private fun createInitialState(): UiState {
        return UiState(
            0,
            gameWorld.size,
            UiState.Dino(AvatarState.RUNNING, yPosFeet = gameWorld.size.groundY)
        )
    }

    fun onReceiveJumpInput() {
        if (gameWorld.dinoJumpState != JUMPING && gameWorld.dinoJumpState != FALLING) {
            gameWorld.dinoJumpState = JUMPING
        }
    }
}

data class GameWorldState(
    var gameWorldTicks: Long = 0,
    var size: UiState.CanvasSize = UiState.CanvasSize(0, 0),
    var dinoYPosFeet: Float = 0f,
    var dinoJumpState: DinoJumpState = DinoJumpState.RUNNING
) {
    enum class DinoJumpState {
        JUMPING,
        FALLING,
        RUNNING
    }
}
