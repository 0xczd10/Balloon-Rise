import android.content.Context
import android.view.SurfaceView
import android.view.SurfaceHolder

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val gameThread: GameThread

    init {
        holder.addCallback(this)
        gameThread = GameThread(holder, this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Поверхность создана, запускаем игровой поток
        gameThread.setRunning(true)
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Можно обработать изменение размера экрана, если нужно
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Поверхность уничтожена, останавливаем поток
        var retry = true
        while (retry) {
            try {
                gameThread.setRunning(false)
                gameThread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    // Этот метод будет обновлять состояние всех игровых объектов
    fun update() {
        // Логика движения шарика, препятствий и т.д.
    }

    // А этот метод будет все рисовать на холсте (Canvas)
    // Мы будем вызывать его из GameThread
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        // Рисуем фон, шарик, препятствия, счет
    }
}