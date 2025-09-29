package com.ggs.balloonrise
import android.graphics.Bitmap
import android.graphics.RectF

class Balloon(
    var image: Bitmap,
    private val screenWidth: Int,
    private val screenHeight: Int,
    initialX: Float,
    initialY: Float
) {
    var boundingBox: RectF
    var velocityX: Float = 0f
    var airLevel: Float = 100f
    private val deflationRate: Float = 0.05f

    // --- ДОБАВЬТЕ ЭТОТ БЛОК ---
    init {
        // Сначала создаем полный прямоугольник
        boundingBox = RectF(initialX, initialY, initialX + image.width, initialY + image.height)

        // Теперь "сжимаем" его. Например, на 25% с каждой стороны по горизонтали и 20% по вертикали
        val insetX = image.width * 0.25f
        val insetY = image.height * 0.20f
        boundingBox.inset(insetX, insetY)
    }
    // -------------------------

    fun update() {
        // Горизонтальное движение
        boundingBox.left += velocityX
        boundingBox.right += velocityX

        // Ограничения по экрану
        if (boundingBox.left < 0) {
            boundingBox.left = 0f
            boundingBox.right = image.width - (2 * (image.width*0.25f)) // Учитываем сжатие
        }
        if (boundingBox.right > screenWidth) {
            boundingBox.right = screenWidth.toFloat()
            boundingBox.left = screenWidth.toFloat() - (image.width - (2 * (image.width*0.25f))) // Учитываем сжатие
        }

        // Сдувание
        if (airLevel > 0) {
            airLevel -= deflationRate
        }
    }

    fun inflate() {
        airLevel = 100f
    }
}