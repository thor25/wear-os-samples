package com.example.dino.presentation

import android.content.res.Resources
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

fun DrawScope.drawDino(avatarState: AvatarState, frameTimeMillis: Float) {
    when (val images = DinoImages.imagesFor(avatarState)) {
        is Image.DualFrameImage -> {
            if (frameTimeMillis.toInt() % 2 == 0) {
                drawImage(images.frameOne)
            } else {
                drawImage(images.frameTwo)
            }
        }
        is Image.SingleFrameImage -> drawImage(images.frame)
    }

//    val images = mapDino[dinoState.value]
//    if (images!!.size > 1) {
//        if (flippedBit) {
//    drawImage(ImageBitmap.imageResource(resources, id = R.drawable.chrome_dino_waiting))
//        } else {
//            drawImage(images[1])
//        }
//    } else {
//        drawImage(images[0])
//    }
//    val dinoState = remember {
//        mutableStateOf(TRexAnim.WAITING)
//    }
//
//    val time by produceState(0f) {
//        while (true) {
//            withInfiniteAnimationFrameMillis {
//                value = it.toFloat() * 0.5f
//            }
//        }
//    }
//    val flippedBit by remember {
//        derivedStateOf {
//            time % 2 == 0f
//        }
//    }
//    val gameStarted = remember {
//        mutableStateOf(false)
//    }
//    val mapDino: MutableMap<TRexAnim, List<ImageBitmap>> = mutableMapOf()
//    TRexAnim.values().forEach {
//        val listImages = it.listImages.map { imageRes ->
//            ImageBitmap.imageResource(id = imageRes)
//        }
//        mapDino[it] = listImages
//    }
//
//    Canvas(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(32.dp)
//            .pointerInput("dino") {
//                detectTapGestures {
//                    if (!gameStarted.value) {
//                        gameStarted.value = true
//                        dinoState.value = TRexAnim.RUNNING
//                    } else {
//                        dinoState.value = TRexAnim.JUMPING
//                    }
//                }
//            },
//        onDraw = {
//            val images = mapDino[dinoState.value]
//            if (images!!.size > 1) {
//                if (flippedBit) {
//                    drawImage(images[0])
//                } else {
//                    drawImage(images[1])
//                }
//            } else {
//                drawImage(images[0])
//            }
//
//        }
//    )
}

enum class AvatarState {
    WAITING,
    RUNNING,
    CRASHED,
    JUMPING
}
