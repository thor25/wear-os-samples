package com.example.genericspaceship

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.RotaryScrollEvent
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import kotlinx.coroutines.launch

@ExperimentalComposeUiApi
@Composable
fun SpaceCanvas(
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput("screen_taps") {
                detectTapGestures {
                    focusRequester.requestFocus()
                    focusRequester.captureFocus()
                    Log.d("!!!", "tap")
                }
            }
            .onFocusChanged { focusState: FocusState ->
                Log.d("!!!", focusState.toString())
            }
            .onRotaryScrollEvent { event: RotaryScrollEvent ->
                Log.d("!!!", "horizontal: ${event.horizontalScrollPixels}")
                Log.d("!!!", "vertical: ${event.verticalScrollPixels}")
                Log.d("!!!", "uptime: ${event.uptimeMillis}")
                // TODO: handle event
                coroutineScope.launch {
                    Log.d("!!!", "launch")
                }
                true
            }
            .focusable(true)// TODO: needed?
            .focusRequester(focusRequester)
    ) {
        drawRoundRect(
            color = Color.Red,
            topLeft = Offset(150f, 150f)
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

