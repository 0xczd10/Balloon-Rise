package com.ggs.balloonrise

import android.content.Context
import android.content.SharedPreferences

object GamePrefs {

    private const val PREFS_NAME = "BalloonRisePrefs"
    private const val KEY_HIGH_SCORE = "highScore"
    private const val KEY_SELECTED_SKIN = "selectedSkin"
    // Ключи для разблокированных скинов
    private const val KEY_SKIN_BLUE_UNLOCKED = "skinBlueUnlocked"
    private const val KEY_SKIN_GREEN_UNLOCKED = "skinGreenUnlocked"


    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Рекорд ---
    fun getHighScore(context: Context): Int {
        return getPrefs(context).getInt(KEY_HIGH_SCORE, 0)
    }

    fun saveHighScore(context: Context, score: Int) {
        if (score > getHighScore(context)) {
            getPrefs(context).edit().putInt(KEY_HIGH_SCORE, score).apply()
        }
    }

    // --- Выбранный скин ---
    fun getSelectedSkin(context: Context): Int {
        // По умолчанию возвращаем красный шарик
        return getPrefs(context).getInt(KEY_SELECTED_SKIN, R.drawable.balloon)
    }

    fun setSelectedSkin(context: Context, skinResId: Int) {
        getPrefs(context).edit().putInt(KEY_SELECTED_SKIN, skinResId).apply()
    }

    // --- Разблокировка скинов ---
    fun isSkinUnlocked(context: Context, skinKey: String): Boolean {
        return getPrefs(context).getBoolean(skinKey, false)
    }

    fun unlockSkin(context: Context, skinKey: String) {
        getPrefs(context).edit().putBoolean(skinKey, true).apply()
    }

    /**
     * Проверяет и разблокирует новые скины на основе рекорда
     */
    fun checkAndUnlockSkins(context: Context, score: Int) {
        // Синий шарик за 500 очков
        if (score >= 500 && !isSkinUnlocked(context, KEY_SKIN_BLUE_UNLOCKED)) {
            unlockSkin(context, KEY_SKIN_BLUE_UNLOCKED)
        }
        // Зеленый шарик за 1000 очков
        if (score >= 1000 && !isSkinUnlocked(context, KEY_SKIN_GREEN_UNLOCKED)) {
            unlockSkin(context, KEY_SKIN_GREEN_UNLOCKED)
        }
        // Сюда можно добавлять новые скины
    }
}
