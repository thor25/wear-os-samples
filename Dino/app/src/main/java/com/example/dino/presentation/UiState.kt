package com.example.dino.presentation

data class UiState(
    val gameWorldTicks: Long,
    val dino: Dino,
    val obstacles: List<Obstacle>
) {
    data class Dino(
        val avatarState: AvatarState,
        val left: Float,
        val top: Float
    )

    data class Obstacle(
        val top: Float,
        val left: Float
    )
}
