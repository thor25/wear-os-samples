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
private const val OBSTACLE_SPEED = 15
private const val DINO_WIDTH = 86
private const val DINO_HEIGHT = 97
private const val CACTUS_WIDTH = 75
private const val CACTUS_HEIGHT = 75
private const val JUMP_HEIGHT = DINO_HEIGHT * 1.5f

class GameWorldViewModel(
    private val gameWorld: GameWorldState = GameWorldState()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState: StateFlow<UiState?> = _uiState.asStateFlow()

    init {
        flow<Unit> {
            while (true) {
                if (gameWorld.size.height != JUMP_HEIGHT) {
                    onGameLoop()
                }
                delay(MILLIS_PER_FRAME_24FPS)
            }
        }.launchIn(viewModelScope)
    }

    fun onCanvasResize(width: Int, height: Int) {
        if (gameWorld.size.width != width.toFloat() || gameWorld.size.height != height.toFloat()) {
            gameWorld.size = GameWorldState.CanvasSize(width.toFloat(), height.toFloat())
            gameWorld.dinoLeft = DINO_WIDTH * 0.5f
            gameWorld.dinoTop = gameWorld.size.groundY - DINO_HEIGHT
            gameWorld.obstacleOne.left = gameWorld.size.width.toFloat()
            gameWorld.obstacleOne.top = gameWorld.size.groundY - CACTUS_HEIGHT
            gameWorld.obstacleTwo.left = gameWorld.size.width.toFloat() + 100F
            gameWorld.obstacleTwo.top = gameWorld.size.groundY - CACTUS_HEIGHT
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
                    left = gameWorld.dinoLeft,
                    top = gameWorld.dinoTop
                ),
                obstacles = listOf(
                    gameWorld.obstacleOne.toUiState(),
                    gameWorld.obstacleTwo.toUiState()
                )
            )
        }
    }

    private fun onGameLoop() {
        gameWorld.gameWorldTicks++

        if (gameWorld.dinoJumpState == JUMPING) {
            if (gameWorld.dinoTop >= gameWorld.size.groundY - DINO_HEIGHT - JUMP_HEIGHT) {
                gameWorld.dinoTop -= JUMP_SPEED
            } else {
                gameWorld.dinoJumpState = FALLING
            }
        }
        if (gameWorld.dinoJumpState == FALLING) {
            if (gameWorld.dinoTop <= gameWorld.size.groundY - DINO_HEIGHT) {
                gameWorld.dinoTop += JUMP_SPEED
            } else {
                gameWorld.dinoTop = gameWorld.size.groundY - DINO_HEIGHT
                gameWorld.dinoJumpState = DinoJumpState.RUNNING
            }
        }

        gameWorld.obstacleOne.left -= OBSTACLE_SPEED
        if (gameWorld.obstacleOne.left < 0 - CACTUS_WIDTH) {
            gameWorld.obstacleOne.left = gameWorld.size.width.toFloat()
        }

        gameWorld.obstacleTwo.left -= OBSTACLE_SPEED
        if (gameWorld.obstacleTwo.left < 0 - CACTUS_WIDTH) {
            gameWorld.obstacleTwo.left = gameWorld.size.width.toFloat()
        }

        emitUpdatedState()
    }

    private fun GameWorldState.Obstacle.toUiState(): UiState.Obstacle {
        return UiState.Obstacle(
            top = top,
            left = left
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
    var size: CanvasSize = CanvasSize(JUMP_HEIGHT, JUMP_HEIGHT),
    var dinoLeft: Float = 0f,
    var dinoTop: Float = 0f,
    var dinoJumpState: DinoJumpState = DinoJumpState.RUNNING,
    var obstacleOne: Obstacle = Obstacle(0f, 0f),
    var obstacleTwo: Obstacle = Obstacle(0f, 0f)
) {

    data class CanvasSize(val width: Float, val height: Float) {
        val groundY: Float
            get() = height * .75f
    }

    data class Obstacle(
        var left: Float,
        var top: Float
    )

    enum class DinoJumpState {
        JUMPING,
        FALLING,
        RUNNING
    }
}
