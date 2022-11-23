package com.example.dino.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@ExperimentalLifecycleComposeApi
@Composable
fun GameWorld(
    viewModel: GameWorldViewModel = remember { GameWorldViewModel() }
) {


    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val (gameWorldTicks, canvasSize, dino) = uiState.value
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                viewModel.onCanvasResize(it.width, it.height)
            }
            .padding(32.dp)
            .pointerInput("screen_taps") {
                // TODO: detect RSB too
                detectTapGestures {
                    viewModel.onReceiveJumpInput()
                }
            },
        onDraw = {
            // TODO: draw obstacles

            // TODO: the yPos is probably backwards
            //  Should the canvas size be communicated to the VM on initialization?
            translate(50F, canvasSize.height - dino.yPos) {
                drawDino(dino.avatarState, gameWorldTicks)
            }
        }
    )
}
