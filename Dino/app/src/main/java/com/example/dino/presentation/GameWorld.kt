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

@ExperimentalLifecycleComposeApi
@Composable
fun GameWorld(
    viewModel: GameWorldViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
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
            val uiStateValue = uiState.value
            if (uiStateValue != null) {
                for (obstacle in uiStateValue.obstacles) {
                    translate(left = obstacle.left, top = obstacle.top) {
                        drawCactus()
                    }
                }

                translate(left = uiStateValue.dino.left, top = uiStateValue.dino.top) {
                    drawDino(uiStateValue.dino.avatarState, uiStateValue.gameWorldTicks)
                }
            }
        }
    )
}
