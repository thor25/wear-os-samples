package com.example.dino.presentation

import android.content.res.Resources
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import com.example.dino.R

sealed interface Image {
    class SingleFrameImage(val frame: ImageBitmap) : Image
    class DualFrameImage(val frameOne: ImageBitmap, val frameTwo: ImageBitmap) : Image
}

object DinoImages {
    private lateinit var waiting: Image
    private lateinit var running: Image
    private lateinit var crashed: Image

    fun initialize(resources: Resources) {
        waiting = Image.SingleFrameImage(
            ImageBitmap.imageResource(
                resources,
                R.drawable.chrome_dino_waiting
            )
        )
        running = Image.DualFrameImage(
            ImageBitmap.imageResource(resources, R.drawable.chrome_dino_walk_1),
            ImageBitmap.imageResource(resources, R.drawable.chrome_dino_walk_2)
        )
        crashed = Image.SingleFrameImage(
            ImageBitmap.imageResource(
                resources,
                R.drawable.chrome_dino_closed_eyes
            )
        )
    }

    fun imagesFor(avatarState: AvatarState): Image {
        return when (avatarState) {
            AvatarState.RUNNING -> running
            AvatarState.CRASHED -> crashed
            AvatarState.WAITING,
            AvatarState.JUMPING -> waiting
        }
    }
}

fun DrawScope.drawDino(avatarState: AvatarState, gameTimeTicks: Long) {
    Log.d("!!!", "drawDino: $avatarState, gwt $gameTimeTicks")
    val flippedBit = gameTimeTicks.toInt() % 2 == 0
    when (val images = DinoImages.imagesFor(avatarState)) {
        is Image.DualFrameImage -> {
            if (flippedBit) {
                drawImage(images.frameOne)
            } else {
                drawImage(images.frameTwo)
            }
        }
        is Image.SingleFrameImage -> drawImage(images.frame)
    }
}

enum class AvatarState {
    WAITING,
    RUNNING,
    CRASHED,
    JUMPING
}
