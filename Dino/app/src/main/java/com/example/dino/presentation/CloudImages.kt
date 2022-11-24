package com.example.dino.presentation

import android.content.res.Resources
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import com.example.dino.R

object CloudImages {
    lateinit var one: ImageBitmap
    lateinit var two: ImageBitmap
    lateinit var three: ImageBitmap

    fun initialize(resources: Resources) {
        one = ImageBitmap.imageResource(
            resources,
            R.drawable.cloud_one
        )
        two = ImageBitmap.imageResource(
            resources,
            R.drawable.cloud_two
        )
        three = ImageBitmap.imageResource(
            resources,
            R.drawable.cloud_three
        )
    }
}

fun DrawScope.drawCloud(type: CloudType) {
    when (type) {
        CloudType.CLOUD_ONE -> drawImage(CloudImages.one)
        CloudType.CLOUD_TWO -> drawImage(CloudImages.two)
        CloudType.CLOUD_THREE -> drawImage(CloudImages.three)
    }
}

enum class CloudType {
    CLOUD_ONE,
    CLOUD_TWO,
    CLOUD_THREE
}
