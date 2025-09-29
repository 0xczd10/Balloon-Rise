package com.ggs.balloonrise

import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {
    private var running: Boolean = false
    private val targetFPS = 60

    fun setRunning(isRunning: Boolean) {
        this.running = isRunning
    }

    override fun run() {
        while (running) {
            var canvas: Canvas? = null
            try {
                canvas = this.surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    this.gameView.update()
                    this.gameView.draw(canvas!!)
                }
            } catch (e: Exception) {
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            sleep((1000 / targetFPS).toLong())
        }
    }
}