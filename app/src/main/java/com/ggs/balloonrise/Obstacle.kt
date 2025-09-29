package com.ggs.balloonrise
import android.graphics.Bitmap
import android.graphics.RectF

class Obstacle(var image: Bitmap, initialX: Float) {
    var boundingBox: RectF

    init {
        // Создаем полный прямоугольник
        boundingBox = RectF(
            initialX, -image.height.toFloat(),
            initialX + image.width, 0f
        )
        // Сжимаем его (для препятствия можно чуть меньше, т.к. оно более "квадратное")
        val insetX = image.width * 0.15f
        val insetY = image.height * 0.15f
        boundingBox.inset(insetX, insetY)
    }
}