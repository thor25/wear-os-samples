package com.example.dino.presentation

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.onEach

@ExperimentalLifecycleComposeApi
@Composable
fun GameWorld(
    viewModel: GameWorldViewModel = GameWorldViewModel()
) {
    // used for "animations" in the [drawDino] drawscope
    val frameTimeMillis by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis { ftm ->
                value = ftm.toFloat()
            }
        }
    }

    // TODO: detect RSB / or tap and send this info to the viewmodel

    val uiState = viewModel.uiState
        .collectAsStateWithLifecycle()
    val (dino) = uiState.value
    foo(viewModel, dino, frameTimeMillis)
}

@Composable
private fun foo(
    viewModel: GameWorldViewModel,
    dino: DinoState,
    frameTimeMillis: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .pointerInput("screen_taps") {
                detectTapGestures {
                    viewModel.onReceiveJumpInput()
                }
            },
        onDraw = {
            // TODO: draw obstacles

            // TODO: the yPos is probably backwards
            //  Should the canvas size be communicated to the VM on initialization?
            translate(100F, dino.yPos.toFloat()) {
                drawDino(dino.avatarState, frameTimeMillis)
            }
        }
    )
}


