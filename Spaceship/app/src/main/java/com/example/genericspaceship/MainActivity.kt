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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

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
    val gameController = object : GameController {
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
    Canvas(
        modifier = modifier
            .fillMaxSize()
            // pass `gameController` as key so that if it changes, the
            // PointerInputScope is remade with ref to the new one
            .pointerInput(key1 = gameController) {
                detectTapGestures(
                    onPress = {
                        gameController.onPressUpEvent()
                        tryAwaitRelease()
                        gameController.onPressDownEvent()
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
        shotsFired.forEach { draw(it) }
        draw(shouldDrawJet, spaceship)
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
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

/**
 * It's drawn facing right, which is 0 degrees.
 */
private fun DrawScope.draw(
    shouldDrawJet: Boolean,
    spaceship: UiState.Spaceship
) {
    rotate(
        degrees = spaceship.rotationDegrees,
        pivot = Offset(spaceship.positionX, spaceship.positionY)
    ) {
        if (shouldDrawJet) {
            val jetPath = Path().apply {
                moveTo(
                    spaceship.positionX - 0.4f * spaceship.length,
                    spaceship.positionY
                )
                lineTo(
                    spaceship.positionX,
                    spaceship.positionY + 0.3f * spaceship.width
                )
                lineTo(
                    spaceship.positionX,
                    spaceship.positionY - 0.3f * spaceship.width
                )
                close()
            }
            drawPath(jetPath, color = Color.Yellow, style = Stroke())
        }

        val spaceshipPath = Path().apply {
            moveTo(spaceship.positionX + 0.7f * spaceship.length, spaceship.positionY)
            lineTo(
                spaceship.positionX - 0.3f * spaceship.length,
                spaceship.positionY + 0.5f * spaceship.width
            )
            lineTo(
                spaceship.positionX - 0.1f * spaceship.length,
                spaceship.positionY
            )
            lineTo(
                spaceship.positionX - 0.3f * spaceship.length,
                spaceship.positionY - 0.5f * spaceship.width
            )
            close()
        }
        drawPath(spaceshipPath, color = Color.Black)
        drawPath(spaceshipPath, color = Color.White, style = Stroke())
    }
}

interface GameController {
    fun onPressDownEvent()
    fun onPressUpEvent()
    fun onDoubleTapEvent()
    fun onRotateEvent(rotationPixels: Float)
}
