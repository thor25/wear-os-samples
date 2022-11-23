package com.example.dino.presentation

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
            gameWorld.obstacleOne.xPosLeft = gameWorld.size.width.toFloat()
            gameWorld.obstacleTwo.xPosLeft = gameWorld.size.width.toFloat() + 100F
        }
    }

    private fun emitUpdatedState() {
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
                canvasSize = gameWorld.size,
                obstacles = listOf(
                    gameWorld.obstacleOne.toUIState(),
                    gameWorld.obstacleTwo.toUIState()
                )
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

        gameWorld.obstacleOne.xPosLeft -= 10
        gameWorld.obstacleTwo.xPosLeft -= 10

        if (gameWorld.obstacleOne.xPosLeft < 0) {
            gameWorld.obstacleOne.xPosLeft = gameWorld.size.width.toFloat()
        }

        if (gameWorld.obstacleTwo.xPosLeft < 0) {
            gameWorld.obstacleTwo.xPosLeft = gameWorld.size.width.toFloat()
        }

        emitUpdatedState()
    }

    private fun createInitialState(): UiState {
        return UiState(
            0,
            gameWorld.size,
            UiState.Dino(AvatarState.RUNNING, yPosFeet = gameWorld.size.groundY),
            obstacles = listOf(
                gameWorld.obstacleOne.toUIState(),
                gameWorld.obstacleTwo.toUIState()
            )
        )
    }

    private fun GameWorldState.Obstacle.toUIState(): UiState.Obstacle {
        return UiState.Obstacle(
            yPosBottom = yPosBottom,
            xPosLeft = xPosLeft,
            width = width,
            height = height
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
    var dinoJumpState: DinoJumpState = DinoJumpState.RUNNING,
    var obstacleOne: Obstacle = Obstacle(
        xPosLeft = size.width.toFloat(),
        yPosBottom = size.groundY
    ),
    var obstacleTwo: Obstacle = Obstacle(
        xPosLeft = size.width.toFloat() + 100F,
        yPosBottom = size.groundY
    ),
) {

    data class Obstacle(
        var xPosLeft: Float,
        var yPosBottom: Float,
        val width: Int = 50,
        val height: Int = 50
    )

    enum class DinoJumpState {
        JUMPING,
        FALLING,
        RUNNING
    }
}
