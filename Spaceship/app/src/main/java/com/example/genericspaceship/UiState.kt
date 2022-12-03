package com.example.genericspaceship

data class UiState(
    val ticks: Long = 0L,
    val spaceship: Spaceship = Spaceship(),
    val shotsFired: List<Shot> = emptyList()
) {
    data class Spaceship(
        val width: Float = 0f,
        val length: Float = 0f,
        val positionX: Float = 0f,
        val positionY: Float = 0f,
        val rotationDegrees: Float = 0f,
        val thrustersEngaged: Boolean = false
    )

    data class Shot(
        val positionX: Float,
        val positionY: Float
    )
}

