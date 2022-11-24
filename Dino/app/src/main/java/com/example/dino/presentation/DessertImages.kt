package com.example.dino.presentation

import android.content.res.Resources
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import com.example.dino.R

object DessertImages {
    lateinit var cake: ImageBitmap
    lateinit var donut: ImageBitmap

    fun initialize(resources: Resources) {
        cake = ImageBitmap.imageResource(
            resources,
            R.drawable.cake
        )
        donut = ImageBitmap.imageResource(
            resources,
            R.drawable.donut
        )
    }
}

fun DrawScope.drawDessert(type: DessertType) {
    when (type) {
        DessertType.CAKE -> drawImage(DessertImages.cake)
        DessertType.DONUT -> drawImage(DessertImages.donut)
    }
}

enum class DessertType {
    CAKE,
    DONUT
}
