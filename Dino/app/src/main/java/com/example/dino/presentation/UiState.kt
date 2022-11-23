package com.example.dino.presentation

data class UiState(
    val gameWorldTicks: Long,
    val canvasSize: CanvasSize,
    val dino: Dino
) {
    data class CanvasSize(val width: Int, val height: Int) {
        val groundY: Float
            get() = height * .75f
    }

    data class Dino(
        val avatarState: AvatarState,
        val yPosFeet: Float
    )
}
