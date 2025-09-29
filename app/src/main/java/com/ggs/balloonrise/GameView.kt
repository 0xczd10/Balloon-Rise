package com.ggs.balloonrise

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*
import kotlin.collections.ArrayList

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private enum class GameState { PLAYING, GAME_OVER }
    private var gameState = GameState.PLAYING

    private val gameThread: GameThread

    // Игровые объекты
    private lateinit var balloon: Balloon
    private val obstacles = ArrayList<Obstacle>()
    private val pumps = ArrayList<PumpBonus>()

    // Графика
    private var originalBackgroundBitmap: Bitmap
    private lateinit var backgroundTileBitmap: Bitmap
    private var originalBalloonBitmap: Bitmap
    private lateinit var balloonBitmap: Bitmap
    private var originalObstacleBitmap: Bitmap
    private lateinit var obstacleBitmap: Bitmap
    private var originalPumpBitmap: Bitmap
    private lateinit var pumpBitmap: Bitmap

    private var bgY1 = 0f
    private var bgY2 = 0f
    private val scrollSpeed = 12

    // Размеры экрана
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    // Игровая логика
    private var score = 0
    private var spawnTimer = 0
    private val random = Random()

    // UI и текст
    private val scorePaint = Paint()
    private val gameOverPaint = Paint()
    private val airBarPaint = Paint()
    private val airBarBackgroundPaint = Paint()

    // --- НОВЫЙ КОД: Элементы для кнопки "Меню" ---
    private val menuButtonPaint = Paint()
    private val menuButtonTextPaint = Paint()
    private lateinit var menuButtonRect: RectF
    // ------------------------------------------

    init {
        holder.addCallback(this)
        gameThread = GameThread(holder, this)
        isFocusable = true

        originalBackgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.background_tile)
        val selectedSkinResId = GamePrefs.getSelectedSkin(context)
        originalBalloonBitmap = BitmapFactory.decodeResource(resources, selectedSkinResId)
        originalObstacleBitmap = BitmapFactory.decodeResource(resources, R.drawable.obstacle)
        originalPumpBitmap = BitmapFactory.decodeResource(resources, R.drawable.pump)

        scorePaint.apply { color = Color.BLACK; textSize = 80f; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
        gameOverPaint.apply { color = Color.RED; textSize = 150f; textAlign = Paint.Align.CENTER; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
        airBarPaint.color = Color.GREEN
        airBarBackgroundPaint.color = Color.GRAY

        // --- НОВЫЙ КОД: Настройка стилей для кнопки "Меню" ---
        menuButtonPaint.color = Color.DKGRAY
        menuButtonTextPaint.apply {
            color = Color.WHITE
            textSize = 60f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        // ----------------------------------------------------
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Пусто
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height

        if (!::balloon.isInitialized) {
            var scaledBG = scaleBitmapToWidth(originalBackgroundBitmap, screenWidth)
            if (scaledBG.height < screenHeight) {
                scaledBG = scaleBitmapToHeight(originalBackgroundBitmap, screenHeight)
            }
            backgroundTileBitmap = scaledBG

            val balloonWidth = screenWidth / 5
            balloonBitmap = scaleBitmapToWidth(originalBalloonBitmap, balloonWidth)
            val obstacleWidth = screenWidth / 4
            obstacleBitmap = scaleBitmapToWidth(originalObstacleBitmap, obstacleWidth)
            val pumpWidth = screenWidth / 7
            pumpBitmap = scaleBitmapToWidth(originalPumpBitmap, pumpWidth)

            bgY1 = 0f
            bgY2 = -backgroundTileBitmap.height.toFloat()
            restartGame()
        }

        if (gameThread.state == Thread.State.NEW) {
            gameThread.setRunning(true)
            gameThread.start()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread.setRunning(false)
        while (retry) {
            try { gameThread.join(); retry = false } catch (e: InterruptedException) { e.printStackTrace() }
        }
    }

    fun update() {
        if (gameState != GameState.PLAYING) return
        bgY1 += scrollSpeed; bgY2 += scrollSpeed
        if (bgY1 > screenHeight) bgY1 = bgY2 - backgroundTileBitmap.height
        if (bgY2 > screenHeight) bgY2 = bgY1 - backgroundTileBitmap.height
        balloon.update()
        score++
        spawnTimer++
        if (spawnTimer % 50 == 0) obstacles.add(Obstacle(obstacleBitmap, random.nextInt(screenWidth - obstacleBitmap.width).toFloat()))
        if (spawnTimer % 250 == 0) pumps.add(PumpBonus(pumpBitmap, random.nextInt(screenWidth - pumpBitmap.width).toFloat()))

        val obstaclesIterator = obstacles.iterator()
        while (obstaclesIterator.hasNext()) {
            val obstacle = obstaclesIterator.next()
            obstacle.boundingBox.offset(0f, scrollSpeed.toFloat())
            if (RectF.intersects(balloon.boundingBox, obstacle.boundingBox)) {
                endGame()
                return
            }
            if (obstacle.boundingBox.top > screenHeight) obstaclesIterator.remove()
        }

        val pumpsIterator = pumps.iterator()
        while (pumpsIterator.hasNext()) {
            val pump = pumpsIterator.next()
            pump.boundingBox.offset(0f, scrollSpeed.toFloat())
            if (RectF.intersects(balloon.boundingBox, pump.boundingBox)) { balloon.inflate(); pumpsIterator.remove() }
            else if (pump.boundingBox.top > screenHeight) pumpsIterator.remove()
        }

        if (balloon.airLevel <= 0) {
            endGame()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawBitmap(backgroundTileBitmap, 0f, bgY1, null)
        canvas.drawBitmap(backgroundTileBitmap, 0f, bgY2, null)
        if (!::balloon.isInitialized) return
        for (obstacle in obstacles) canvas.drawBitmap(obstacle.image, obstacle.boundingBox.left, obstacle.boundingBox.top, null)
        for (pump in pumps) canvas.drawBitmap(pump.image, pump.boundingBox.left, pump.boundingBox.top, null)
        canvas.drawBitmap(balloon.image, balloon.boundingBox.left, balloon.boundingBox.top, null)
        canvas.drawText("Score: $score", 50f, 100f, scorePaint)
        val airBarMaxWidth = 300f; val airBarHeight = 50f; val airBarX = screenWidth - airBarMaxWidth - 50f; val airBarY = 50f
        canvas.drawRect(airBarX, airBarY, airBarX + airBarMaxWidth, airBarY + airBarHeight, airBarBackgroundPaint)
        airBarPaint.color = if (balloon.airLevel > 25) Color.GREEN else Color.RED
        canvas.drawRect(airBarX, airBarY, airBarX + (airBarMaxWidth * (balloon.airLevel / 100f)), airBarY + airBarHeight, airBarPaint)

        if (gameState == GameState.GAME_OVER) {
            canvas.drawARGB(150, 0, 0, 0)
            canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 150, gameOverPaint)

            scorePaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 50, scorePaint)
            scorePaint.textAlign = Paint.Align.LEFT

            // --- НОВЫЙ КОД: Рисуем кнопку "Главное меню" ---
            val buttonWidth = 500f
            val buttonHeight = 150f
            val buttonTop = screenHeight / 2f + 150f
            val buttonLeft = screenWidth / 2f - buttonWidth / 2f
            menuButtonRect = RectF(buttonLeft, buttonTop, buttonLeft + buttonWidth, buttonTop + buttonHeight)
            canvas.drawRoundRect(menuButtonRect, 25f, 25f, menuButtonPaint)
            canvas.drawText("Main Menu", menuButtonRect.centerX(), menuButtonRect.centerY() + 20f, menuButtonTextPaint)
            // ------------------------------------------------
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_DOWN && gameState == GameState.GAME_OVER) {
            // --- НОВЫЙ КОД: Проверяем, нажата ли кнопка "Главное меню" ---
            if (::menuButtonRect.isInitialized && menuButtonRect.contains(event.x, event.y)) {
                // Завершаем текущую активность (GameActivity) и возвращаемся в меню
                (context as Activity).finish()
                return true
            }
            // -----------------------------------------------------------

            // Если не нажали на кнопку "меню", то перезапускаем игру
            restartGame()
            return true
        }

        if (gameState == GameState.PLAYING) {
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> { balloon.velocityX = if (event.x < screenWidth / 2) -15f else 15f; return true }
                MotionEvent.ACTION_UP -> { balloon.velocityX = 0f; return true }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun restartGame() {
        val selectedSkinResId = GamePrefs.getSelectedSkin(context)
        originalBalloonBitmap = BitmapFactory.decodeResource(resources, selectedSkinResId)
        val balloonWidth = screenWidth / 5
        balloonBitmap = scaleBitmapToWidth(originalBalloonBitmap, balloonWidth)

        if (!::balloon.isInitialized) {
            balloon = Balloon(balloonBitmap, screenWidth, screenHeight,
                screenWidth / 2f - balloonBitmap.width / 2f, screenHeight / 2f - balloonBitmap.height / 2f)
        }
        balloon.image = balloonBitmap
        balloon.boundingBox.offsetTo(screenWidth / 2f - balloonBitmap.width / 2f, screenHeight / 2f - balloonBitmap.height / 2f)
        balloon.inflate()
        balloon.velocityX = 0f
        obstacles.clear(); pumps.clear(); score = 0; spawnTimer = 0
        gameState = GameState.PLAYING
    }

    private fun endGame() {
        gameState = GameState.GAME_OVER
        GamePrefs.saveHighScore(context, score)
        val highScore = GamePrefs.getHighScore(context)
        GamePrefs.checkAndUnlockSkins(context, highScore)
    }

    private fun scaleBitmapToWidth(bitmap: Bitmap, newWidth: Int): Bitmap {
        val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
        val newHeight = (newWidth * aspectRatio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun scaleBitmapToHeight(bitmap: Bitmap, newHeight: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val newWidth = (newHeight * aspectRatio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}