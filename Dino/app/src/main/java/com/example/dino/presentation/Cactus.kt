package com.example.dino.presentation

import android.content.res.Resources
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import com.example.dino.R

class Cactus {
    sealed interface Image {
        class SingleFrameImage(val frame: ImageBitmap) : Image
    }

    object CactusImages {
        lateinit var cactus: ImageBitmap

        fun initialize(resources: Resources) {
            cactus =
                ImageBitmap.imageResource(
                    resources,
                    R.drawable.chrome_dino_cactus
            )
        }
    }
}

fun DrawScope.drawCactus() {
    drawImage(Cactus.CactusImages.cactus)
}

