package com.example.genericspaceship

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
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@ExperimentalComposeUiApi
@Composable
fun SpaceCanvas(
    modifier: Modifier = Modifier,
    viewModel: GameEngineViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    SpaceCanvas(
        spaceship = uiState.value.spaceship,
        shotsFired = uiState.value.shotsFired,
        onPressEventDown = { viewModel.onPressEventDown() },
        onPressEventUp = { viewModel.onPressEventUp() },
        onDoubleTap = { viewModel.onDoubleTap() },
        onRotate = { viewModel.onRotate(it) },
        modifier = modifier.onSizeChanged(viewModel::onCanvasSizeChange)
    )
}

@ExperimentalComposeUiApi
@Composable
fun SpaceCanvas(
    spaceship: UiState.Spaceship,
    shotsFired: List<UiState.Shot>,
    onPressEventDown: () -> Unit,
    onPressEventUp: () -> Unit,
    onDoubleTap: () -> Unit,
    onRotate: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput("screen_taps") {
                detectTapGestures(
                    onPress = {
                        onPressEventDown()
                        tryAwaitRelease()
                        onPressEventUp()
                    },
                    onDoubleTap = { onDoubleTap() }
                )
            }
            .onRotaryScrollEvent {
                onRotate(it.verticalScrollPixels)
                true
            }
            .focusRequester(focusRequester)
            .focusable(true)
    ) {
        shotsFired.forEach { draw(it) }
        draw(spaceship)
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
private fun DrawScope.draw(spaceship: UiState.Spaceship) {
    rotate(
        degrees = spaceship.rotationDegrees,
        pivot = Offset(spaceship.positionX, spaceship.positionY)
    ) {
        val spaceshipPath = Path().apply {
            moveTo(spaceship.positionX + 0.7f * spaceship.length, spaceship.positionY)
            lineTo(
                spaceship.positionX - 0.3f * spaceship.length,
                spaceship.positionY + 0.5f * spaceship.width
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
