package com.ggs.balloonrise
import android.graphics.Bitmap
import android.graphics.RectF

class PumpBonus(var image: Bitmap, initialX: Float) {
    var boundingBox: RectF

    init {
        // Создаем полный прямоугольник
        boundingBox = RectF(
            initialX, -image.height.toFloat(),
            initialX + image.width, 0f
        )
        // Сжимаем
        val insetX = image.width * 0.20f
        val insetY = image.height * 0.20f
        boundingBox.inset(insetX, insetY)
    }
}