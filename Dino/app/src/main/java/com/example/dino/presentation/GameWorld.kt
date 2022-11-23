package com.example.dino.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private const val DINO_WIDTH = 86
private const val DINO_HEIGHT = 97
private const val CACTUS_HEIGHT = 75

@ExperimentalLifecycleComposeApi
@Composable
fun GameWorld(
    viewModel: GameWorldViewModel = viewModel()
) {

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val (gameWorldTicks, canvasSize, dino, obstacles) = uiState.value
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                viewModel.onCanvasResize(it.width, it.height)
            }
            .pointerInput("screen_taps") {
                // TODO: detect RSB too
                detectTapGestures {
                    viewModel.onReceiveJumpInput()
                }
            },
        onDraw = {
            for (obstacle in obstacles) {
                // TODO: draw obstacles
                translate(
                    left = obstacle.xPosLeft,
                    top = obstacle.yPosBottom - CACTUS_HEIGHT
                ) {
                    drawCactus()
                }
            }

            translate(
                left = DINO_WIDTH * 0.5f,
                top = dino.yPosFeet - DINO_HEIGHT
            ) {
                drawDino(dino.avatarState, gameWorldTicks)
            }
        }
    )
}
