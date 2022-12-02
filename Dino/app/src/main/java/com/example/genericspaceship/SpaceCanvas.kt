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
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
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
        draw(spaceship)
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

/**
 * It's drawn facing right, which is 0 degrees.
 */
private fun DrawScope.draw(spaceship: UiState.Spaceship) {
    val tip = Offset(spaceship.positionX + 0.7f * spaceship.length, spaceship.positionY)
    val backLeft = Offset(
        spaceship.positionX - 0.3f * spaceship.length,
        spaceship.positionY - 0.5f * spaceship.width
    )
    val backRight = Offset(
        spaceship.positionX - 0.3f * spaceship.length,
        spaceship.positionY + 0.5f * spaceship.width
    )
    drawCircle(
        color = Color.Red,
        center = Offset(spaceship.positionX, spaceship.positionY),
        radius = 4f
    )
    rotate(
        degrees = spaceship.rotationDegrees,
        pivot = Offset(spaceship.positionX, spaceship.positionY)
    ) {
        drawPoints(
            points = listOf(tip, backLeft, backRight, tip),
            pointMode = PointMode.Polygon,
            color = Color.White
        )
    }
}
