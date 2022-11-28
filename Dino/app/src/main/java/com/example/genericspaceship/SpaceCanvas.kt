package com.example.genericspaceship

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged

@ExperimentalComposeUiApi
@Composable
fun SpaceCanvas(
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var rotation by remember { mutableStateOf(0f) }
    var center by remember { mutableStateOf(Offset(0f, 0f)) }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                center = Offset(it.width * 0.5f, it.height * 0.5f)
            }
            .pointerInput("screen_taps") {
                detectTapGestures {
                    Log.d("!!!", "tap")
                }
            }
            .onRotaryScrollEvent {
                rotation += it.verticalScrollPixels
                true
            }
            .focusRequester(focusRequester)
            .focusable(true)
    ) {
        draw(Spaceship(center.x, center.y, rotation))
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
