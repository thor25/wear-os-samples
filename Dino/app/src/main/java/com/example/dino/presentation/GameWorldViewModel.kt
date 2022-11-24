package com.example.dino.presentation

import android.graphics.Rect
import android.graphics.Rect.intersects
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dino.presentation.GameWorldState.CanvasSize
import com.example.dino.presentation.GameWorldState.DinoState.CRASHED
import com.example.dino.presentation.GameWorldState.DinoState.FALLING
import com.example.dino.presentation.GameWorldState.DinoState.JUMPING
import com.example.dino.presentation.GameWorldState.DinoState.RUNNING
import com.example.dino.presentation.GameWorldState.Obstacle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlin.random.Random

private const val MILLIS_PER_FRAME_24FPS = (1000 / 24).toLong()
private const val JUMP_SPEED = 30
private const val FALL_SPEED = 20
private const val DINO_WIDTH = 48
private const val DINO_HEIGHT = 64
private const val OBSTACLE_SPEED = 15
private const val OBSTACLE_DIST_MIN = 600
private const val OBSTACLE_DIST_MAX = 1000
private const val OBSTACLE_WIDTH = 48
private const val OBSTACLE_HEIGHT = 48
private const val JUMP_HEIGHT = DINO_HEIGHT * 2.2f

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

    fun onStartPressed() {
        gameWorld.isPlaying = true
        reset()
    }

    fun onCanvasResize(width: Int, height: Int) {
        if (gameWorld.size.width != width.toFloat() || gameWorld.size.height != height.toFloat()) {
            gameWorld.size = CanvasSize(width.toFloat(), height.toFloat())
            reset()
        }
    }

    private fun reset() {
        gameWorld.gameWorldTicks = 0
        gameWorld.dinoState = RUNNING
        gameWorld.dinoLeft = DINO_WIDTH * 1.5f
        gameWorld.dinoTop = gameWorld.size.groundY - DINO_HEIGHT
        gameWorld.obstacleOne.left = gameWorld.size.width
        gameWorld.obstacleOne.top = gameWorld.size.groundY - OBSTACLE_HEIGHT
        gameWorld.obstacleTwo.left = gameWorld.size.width + generateObstacleDistance()
        gameWorld.obstacleTwo.top = gameWorld.size.groundY - OBSTACLE_HEIGHT
    }

    private fun emitUpdatedState() {
        _uiState.update {
            UiState(
                gameWorldTicks = gameWorld.gameWorldTicks,
                score = gameWorld.score.toInt(),
                dino = UiState.Dino(
                    avatarState = when (gameWorld.dinoState) {
                        RUNNING -> AvatarState.RUNNING
                        CRASHED -> AvatarState.CRASHED
                        else -> AvatarState.JUMPING
                    },
                    left = gameWorld.dinoLeft,
                    top = gameWorld.dinoTop
                ),
                obstacles = listOf(
                    gameWorld.obstacleOne.toUiState(),
                    gameWorld.obstacleTwo.toUiState()
                ),
                isPlaying = gameWorld.isPlaying
            )
        }
    }

    private val dinoRectangle = Rect()
    private val obstacleOneRectangle = Rect()
    private val obstacleTwoRectangle = Rect()

    private fun onGameLoop() {
        gameWorld.gameWorldTicks++

        // TODO move this to a nicer spot, make less redundant
        dinoRectangle.left = gameWorld.dinoLeft.toInt()
        dinoRectangle.top = gameWorld.dinoTop.toInt()
        dinoRectangle.right = (gameWorld.dinoLeft + DINO_WIDTH).toInt()
        dinoRectangle.bottom = (gameWorld.dinoTop + DINO_HEIGHT).toInt()

        obstacleOneRectangle.left = gameWorld.obstacleOne.left.toInt()
        obstacleOneRectangle.top = gameWorld.obstacleOne.top.toInt()
        obstacleOneRectangle.right = (gameWorld.obstacleOne.left + OBSTACLE_WIDTH).toInt()
        obstacleOneRectangle.bottom = (gameWorld.obstacleOne.top + OBSTACLE_HEIGHT).toInt()

        obstacleTwoRectangle.left = gameWorld.obstacleTwo.left.toInt()
        obstacleTwoRectangle.top = gameWorld.obstacleTwo.top.toInt()
        obstacleTwoRectangle.right = (gameWorld.obstacleTwo.left + OBSTACLE_WIDTH).toInt()
        obstacleTwoRectangle.bottom = (gameWorld.obstacleTwo.top + OBSTACLE_HEIGHT).toInt()

        // Check for collisions
        if (intersects(dinoRectangle, obstacleOneRectangle) || intersects(
                dinoRectangle,
                obstacleTwoRectangle
            )
        ) {
            if (gameWorld.isPlaying) {
                gameWorld.score = (gameWorld.gameWorldTicks / 12).toInt()
            }
            gameWorld.isPlaying = false
            gameWorld.dinoState = CRASHED
        }

        if (gameWorld.dinoState == JUMPING) {
            if (gameWorld.dinoTop >= gameWorld.size.groundY - DINO_HEIGHT - JUMP_HEIGHT) {
                gameWorld.dinoTop -= JUMP_SPEED
            } else {
                gameWorld.dinoState = FALLING
            }
        }
        if (gameWorld.dinoState == FALLING) {
            if (gameWorld.dinoTop <= gameWorld.size.groundY - DINO_HEIGHT) {
                gameWorld.dinoTop += FALL_SPEED
            } else {
                gameWorld.dinoTop = gameWorld.size.groundY - DINO_HEIGHT
                gameWorld.dinoState = RUNNING
            }
        }

        if (gameWorld.isPlaying) {
            gameWorld.obstacleOne.left -= OBSTACLE_SPEED
            if (gameWorld.obstacleOne.left < 0 - OBSTACLE_WIDTH) {
                gameWorld.obstacleOne.left =
                    (gameWorld.size.width + generateObstacleDistance())
                gameWorld.obstacleOne.type =
                    GameWorldState.DessertType.values()[Random.nextInt(DessertType.values().size)]
            }

            gameWorld.obstacleTwo.left -= OBSTACLE_SPEED
            if (gameWorld.obstacleTwo.left < 0 - OBSTACLE_WIDTH) {
                gameWorld.obstacleTwo.left =
                    (gameWorld.size.width + generateObstacleDistance())
                gameWorld.obstacleTwo.type =
                    GameWorldState.DessertType.values()[Random.nextInt(DessertType.values().size)]
            }
        }

        emitUpdatedState()
    }

    private fun Obstacle.toUiState(): UiState.Obstacle {
        return UiState.Obstacle(
            top = top,
            left = left,
            type = when (type) {
                GameWorldState.DessertType.CAKE -> DessertType.CAKE
                GameWorldState.DessertType.DONUT -> DessertType.DONUT
            }
        )
    }

    fun onReceiveJumpInput() {
        if (gameWorld.dinoState != JUMPING && gameWorld.dinoState != FALLING) {
            gameWorld.dinoState = JUMPING
        }
    }

    private fun generateObstacleDistance(): Float {
        val distance = (OBSTACLE_DIST_MIN..OBSTACLE_DIST_MAX).random().toFloat()
        Log.d("!!!", "distance $distance")
        return distance
    }

}

data class GameWorldState(
    var gameWorldTicks: Long = 0,
    var size: CanvasSize = CanvasSize(JUMP_HEIGHT, JUMP_HEIGHT),
    var dinoLeft: Float = 0f,
    var dinoTop: Float = 0f,
    var dinoState: DinoState = RUNNING,
    var obstacleOne: Obstacle = Obstacle(0f, 0f, DessertType.CAKE),
    var obstacleTwo: Obstacle = Obstacle(0f, 0f, DessertType.DONUT),
    var isPlaying: Boolean = false,
    var score: Int = 0
) {

    data class CanvasSize(val width: Float, val height: Float) {
        val groundY: Float
            get() = height * .75f
    }

    data class Obstacle(
        var left: Float,
        var top: Float,
        var type: DessertType
    )

    enum class DinoState {
        JUMPING,
        FALLING,
        RUNNING,
        CRASHED
    }

    enum class DessertType {
        CAKE,
        DONUT
    }
}
