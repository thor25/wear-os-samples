package com.example.genericspaceship

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate

private const val WIDTH = 40f
private const val LENGTH = 50f

data class Spaceship(
    var centerX: Float,
    var centerY: Float,
    var rotationDegrees: Float
) {
    val tip: Offset
        get() = Offset(centerX, centerY - 0.7f * LENGTH)

    val backLeft: Offset
        get() = Offset(centerX - 0.5f * WIDTH, centerY + 0.3f * LENGTH)

    val backRight: Offset
        get() = Offset(centerX + 0.5f * WIDTH, centerY + 0.3f * LENGTH)
}

fun DrawScope.draw(spaceship: Spaceship) {
    drawCircle(
        Color.Green,
        radius = 5f,
        center = center
    )
    rotate(
        degrees = spaceship.rotationDegrees,
        pivot = center
    ) {
        drawPoints(
            points = listOf(
                spaceship.tip,
                spaceship.backLeft,
                spaceship.backRight,
                spaceship.tip
            ),
            pointMode = PointMode.Polygon,
            color = Color.White
        )
    }
}


