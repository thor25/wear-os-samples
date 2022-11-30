package com.example.genericspaceship

data class UiState(
    val spaceship: Spaceship = Spaceship()
) {
    data class Spaceship(
        val width: Float = 0f,
        val length: Float = 0f,
        val positionX: Float = 0f,
        val positionY: Float = 0f,
        val rotationDegrees: Float = 0f,
    )
}

