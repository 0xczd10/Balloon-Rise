package com.ggs.balloonrise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var highScoreText: TextView
    private lateinit var skinRed: ImageView
    private lateinit var skinBlue: ImageView
    private lateinit var skinGreen: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим все элементы интерфейса
        highScoreText = findViewById(R.id.highScoreText)
        skinRed = findViewById(R.id.skinRed)
        skinBlue = findViewById(R.id.skinBlue)
        skinGreen = findViewById(R.id.skinGreen)

        // Настраиваем кнопку "PLAY"
        findViewById<Button>(R.id.playButton).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        // Настраиваем клики по иконкам скинов
        setupSkinSelector()
    }

    override fun onResume() {
        super.onResume()
        // Этот метод вызывается каждый раз, когда мы возвращаемся на этот экран
        // (например, после проигрыша в игре).
        // Поэтому здесь мы обновляем всю информацию.
        updateHighScoreDisplay()
        updateSkinAvailability()
        updateSelectionBorder()
    }

    /**
     * Обновляет текстовое поле с рекордом, загружая его из GamePrefs.
     */
    private fun updateHighScoreDisplay() {
        val highScore = GamePrefs.getHighScore(this)
        highScoreText.text = "High Score: $highScore"
    }

    /**
     * Настраивает логику нажатий на иконки скинов.
     */
    private fun setupSkinSelector() {
        // Красный скин всегда доступен
        skinRed.setOnClickListener {
            GamePrefs.setSelectedSkin(this, R.drawable.balloon)
            updateSelectionBorder()
        }

        // Синий скин доступен только если разблокирован
        skinBlue.setOnClickListener {
            if (GamePrefs.isSkinUnlocked(this, "skinBlueUnlocked")) {
                GamePrefs.setSelectedSkin(this, R.drawable.balloon_blue)
                updateSelectionBorder()
            }
        }

        // Зеленый скин доступен только если разблокирован
        skinGreen.setOnClickListener {
            if (GamePrefs.isSkinUnlocked(this, "skinGreenUnlocked")) {
                GamePrefs.setSelectedSkin(this, R.drawable.balloon_green)
                updateSelectionBorder()
            }
        }
    }

    /**
     * Проверяет, какие скины разблокированы, и меняет их прозрачность.
     * Заблокированные скины будут полупрозрачными.
     */
    private fun updateSkinAvailability() {
        skinBlue.alpha = if (GamePrefs.isSkinUnlocked(this, "skinBlueUnlocked")) 1.0f else 0.3f
        skinGreen.alpha = if (GamePrefs.isSkinUnlocked(this, "skinGreenUnlocked")) 1.0f else 0.3f
    }

    /**
     * Рисует желтую рамку вокруг выбранного в данный момент скина.
     */
    private fun updateSelectionBorder() {
        val selectedSkin = GamePrefs.getSelectedSkin(this)

        // Сначала убираем рамки со всех
        skinRed.background = null
        skinBlue.background = null
        skinGreen.background = null

        // Затем добавляем рамку только выбранному скину
        val selectionDrawable = ContextCompat.getDrawable(this, R.drawable.selection_border) // Предполагаем, что вы создадите этот файл
        when (selectedSkin) {
            R.drawable.balloon -> skinRed.background = selectionDrawable
            R.drawable.balloon_blue -> skinBlue.background = selectionDrawable
            R.drawable.balloon_green -> skinGreen.background = selectionDrawable
        }
    }
}

