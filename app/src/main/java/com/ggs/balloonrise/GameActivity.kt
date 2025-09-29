package com.ggs.balloonrise

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- ИСПРАВЛЕННЫЙ КОД ДЛЯ ПОЛНОЭКРАННОГО РЕЖИМА ---
        // Сначала устанавливаем флаги, чтобы приложение знало, что нужно занять весь экран
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        // Теперь устанавливаем наш игровой холст
        setContentView(GameView(this))

        // И уже после этого скрываем системные панели для новых версий Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
        // -----------------------------------------------------------
    }
}