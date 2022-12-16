package com.example.genericspaceship

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpaceCanvas()
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun SpaceCanvas(
    modifier: Modifier = Modifier,
    viewModel: GameEngineViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isRound = LocalConfiguration.current.isScreenRound
    val gameController = remember {
        object : GameController {
            override fun onPressDownEvent() {
                viewModel.onPressEventDown()
            }

            override fun onPressUpEvent() {
                viewModel.onPressEventUp()
            }

            override fun onDoubleTapEvent() {
                viewModel.onDoubleTap()
            }

            override fun onRotateEvent(rotationPixels: Float) {
                viewModel.onRotate(rotationPixels)
            }
        }
    }
    SpaceCanvas(
        shouldDrawJet = uiState.value.spaceship.thrustersEngaged && uiState.value.ticks % 2 == 0L,
        spaceship = uiState.value.spaceship,
        shotsFired = uiState.value.shotsFired,
        gameController = gameController,
        modifier = modifier.onSizeChanged {
            viewModel.onCanvasSizeChange(it, isRound)
        }
    )
}

@ExperimentalComposeUiApi
@Composable
fun SpaceCanvas(
    shouldDrawJet: Boolean,
    spaceship: UiState.Spaceship,
    shotsFired: List<UiState.Shot>,
    gameController: GameController,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    // these paths only change if the spaceship.width/height changes,
    // so let's remember them instead of recreating the paths each time
    val (spaceshipPath, jetPath) = remember(spaceship.width, spaceship.length) {
        spaceship.createPath() to spaceship.createJetPath()
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            // pass `gameController` as key so that if it changes, the
            // PointerInputScope is remade with ref to the new one
            .pointerInput(key1 = gameController) {
                detectTapGestures(
                    onPress = {
                        gameController.onPressDownEvent()
                        tryAwaitRelease()
                        gameController.onPressUpEvent()
                    },
                    onDoubleTap = {
                        gameController.onDoubleTapEvent()
                    }
                )
            }
            .focusRequester(focusRequester)
            .onRotaryScrollEvent {
                gameController.onRotateEvent(it.verticalScrollPixels)
                true
            }
            .focusable()
    ) {
        rotate(
            degrees = spaceship.rotationDegrees,
            pivot = Offset(spaceship.positionX, spaceship.positionY)
        ) {
            shotsFired.forEach { draw(it) }
            translate(spaceship.positionX, spaceship.positionY) {
                if (shouldDrawJet) {
                    drawPath(jetPath, color = Color.Yellow, style = Stroke(width = 2f))
                }
                drawPath(spaceshipPath, color = Color.Black)
                drawPath(spaceshipPath, color = Color.White, style = Stroke(width = 2f))
            }
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private fun UiState.Spaceship.createPath() = Path().apply {
    moveTo(0.7f * length, 0f)
    lineTo(-0.3f * length, 0.5f * width)
    lineTo(-0.1f * length, 0f)
    lineTo(-0.3f * length, -0.5f * width)
    close()
}

private fun UiState.Spaceship.createJetPath() = Path().apply {
    moveTo(-0.4f * length, 0f)
    lineTo(0f, 0.3f * width)
    lineTo(0f, -0.3f * width)
    close()
}

/**
 * It's drawn facing right, which is 0 degrees.
 */
private fun DrawScope.draw(shot: UiState.Shot) {
    drawCircle(
        color = Color.White,
        center = Offset(shot.positionX, shot.positionY),
        radius = 2f
    )
}

interface GameController {
    fun onPressDownEvent()
    fun onPressUpEvent()
    fun onDoubleTapEvent()
    fun onRotateEvent(rotationPixels: Float)
}

/* ----------------------------------------------------------------- */

data class UiState(
    val ticks: Long = 0L,
    val spaceship: Spaceship = Spaceship(),
    val shotsFired: List<Shot> = emptyList()
) {
    data class Spaceship(
        val width: Float = 0f,
        val length: Float = 0f,
        val positionX: Float = 0f,
        val positionY: Float = 0f,
        val rotationDegrees: Float = 0f,
        val thrustersEngaged: Boolean = false
    )

    data class Shot(
        val positionX: Float,
        val positionY: Float
    )
}

/* ----------------------------------------------------------------- */

class GameEngineViewModel(
    private val state: GameState = GameState(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                if (state.size != IntSize.Zero) {
                    update()
                }
                delay(UPDATE_INTERVAL)
            }
        }
    }

    fun onCanvasSizeChange(newSize: IntSize, isRound: Boolean) {
        state.isRound = isRound
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
        val spaceship = state.spaceship
        spaceship.thrustersEngaged = false
        state.shotsFired.add(
            GameState.Shot(
                positionX = spaceship.positionX,
                positionY = spaceship.positionY,
                bearingRads = spaceship.rotationRads,
                endPositionX = spaceship.positionX +
                    SpaceshipConstants.SHOT_RANGE_MULTIPLIER * SpaceshipConstants.SHOT_SPEED * cos(
                    spaceship.rotationRads
                ).toFloat(),
                endPositionY = spaceship.positionY +
                    SpaceshipConstants.SHOT_RANGE_MULTIPLIER * SpaceshipConstants.SHOT_SPEED * sin(
                    spaceship.rotationRads
                ).toFloat(),
            )
        )
    }

    fun onRotate(rotationPixels: Float) {
        state.spaceship.rotationDegrees += rotationPixels
    }

    private fun update() {
        state.ticks++

        val spaceship = state.spaceship
        if (spaceship.thrustersEngaged) {
            spaceship.applyThrust()
        } else {
            spaceship.applyFriction()
        }
        spaceship.positionX += spaceship.thrustX
        spaceship.positionY += spaceship.thrustY
        spaceship.stayOnScreen()

        state.shotsFired.forEach { shot ->
            shot.positionX += SpaceshipConstants.SHOT_SPEED * cos(shot.bearingRads).toFloat()
            shot.positionY += SpaceshipConstants.SHOT_SPEED * sin(shot.bearingRads).toFloat()
        }
        state.shotsFired.removeAll { shot -> shot.hasExhaustedRange() }

        emitLatestState()
    }

    private fun GameState.Shot.hasExhaustedRange(): Boolean {
        return positionX >= endPositionX && positionY >= endPositionY
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
        if (state.isRound) {
            stayOnScreenRound()
        } else {
            stayOnScreenRectangle()
        }
    }

    private fun GameState.Spaceship.stayOnScreenRectangle() {
        val halfLength = length * 0.5f
        if (positionX + halfLength < 0) {
            positionX = state.size.width.toFloat()
        } else if (positionX - halfLength > state.size.width) {
            positionX = 0f
        }
        if (positionY + halfLength < 0) {
            positionY = state.size.height.toFloat()
        } else if (positionY - halfLength > state.size.height) {
            positionY = 0f
        }
    }

    /**
     * For round devices, it looks really weird if we assume a square canvas because it takes longer
     * for the ship to reappear if it flies through the corners of the square which are further away
     * from the center of the screen than the sides.
     */
    private fun GameState.Spaceship.stayOnScreenRound() {
        val radius = state.size.width * 0.5
        val centerX = radius
        val centerY = radius

        // pythagoras' theorem to see if ship is outside of the circle
        val a = abs(positionX - centerX)
        val b = abs(positionY - centerY)
        val distFromCenter = sqrt(a.pow(2) + b.pow(2))

        val halfLength = length * 0.5f
        if (distFromCenter > radius + halfLength + 1) {
            positionX = when {
                positionX < centerX -> centerX + a
                positionX > centerX -> centerX - a
                else -> positionX
            }.toFloat()

            positionY = when {
                positionY < centerY -> centerY + b
                positionY > centerY -> centerY - b
                else -> positionY
            }.toFloat()
        }
    }

    private fun emitLatestState() {
        _uiState.update {
            UiState(
                ticks = state.ticks,
                spaceship = UiState.Spaceship(
                    width = state.spaceship.width,
                    length = state.spaceship.length,
                    positionX = state.spaceship.positionX,
                    positionY = state.spaceship.positionY,
                    rotationDegrees = state.spaceship.rotationDegrees,
                    thrustersEngaged = state.spaceship.thrustersEngaged
                ),
                shotsFired = state.shotsFired.map {
                    UiState.Shot(
                        positionX = it.positionX,
                        positionY = it.positionY
                    )
                }
            )
        }
    }

    companion object {
        private const val FPS = 30
        private const val UPDATE_INTERVAL = 1000L / FPS
    }
}

object SpaceshipConstants {

    const val MAX_THRUST = 12f
    const val THRUST_RATE = 0.25f
    const val FRICTION = 0.05f
    const val SHOT_SPEED = MAX_THRUST + 3f
    const val SHOT_RANGE_MULTIPLIER = 30f
}

data class GameState(
    var ticks: Long = 0L,
    var size: IntSize = IntSize.Zero,
    var isRound: Boolean = true,
    val shotsFired: MutableList<Shot> = mutableListOf(),
    val spaceship: Spaceship = Spaceship()
) {
    data class Shot(
        var positionX: Float,
        var positionY: Float,
        val bearingRads: Double,
        val endPositionX: Float,
        val endPositionY: Float
    )

    data class Spaceship(
        /**
         * Bounding box length.
         * TODO: should this be based on [GameState.size]?
         */
        var length: Float = 50f,
        /**
         * Rotation around the pivot point.
         */
        var rotationDegrees: Float = 0f,
        /**
         * Pivot point's x-offset from origin.
         */
        var positionX: Float = 0f,
        /**
         * Pivot point's y-offset from origin.
         */
        var positionY: Float = 0f,
        var thrustX: Float = 0f,
        var thrustY: Float = 0f,
        var thrustersEngaged: Boolean = false,
    ) {
        /**
         * Bounding box width.
         */
        val width
            get() = length * 0.7f

        val rotationRads
            get() = rotationDegrees / 180 * Math.PI
    }
}

