package com.example.tictactoe

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.min

class TicTacToeSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    interface StatusCallback {
        fun onStatusUpdate(status: String)
        fun onGameEnd()
    }

    private val gameLogic = GameLogic()
    private val paint = Paint()
    private var cellSize: Float = 0f
    private var animProgress = Array(3) { FloatArray(3) { 0f } }
    private var animWinLineProgress = 0f
    private var winLine: GameLogic.WinLine? = null
    private var handler = Handler(Looper.getMainLooper())
    private var statusCallback: StatusCallback? = null
    private var isAnimating = false
    private var isPlayerTurn = true // Player is always X
    private var surfaceReady = false

    init {
        holder.addCallback(this)
        isFocusable = true
        paint.isAntiAlias = true
        paint.strokeWidth = 12f
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
    }

    fun setStatusCallback(cb: StatusCallback) {
        statusCallback = cb
        updateStatus()
    }

    fun resetGame() {
        gameLogic.reset()
        for (i in 0..2) for (j in 0..2) animProgress[i][j] = 0f
        animWinLineProgress = 0f
        winLine = null
        isAnimating = false
        isPlayerTurn = true
        surfaceReady = true
        updateStatus()
        drawBoard()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceReady = true
        drawBoard()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        drawBoard()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isPlayerTurn && !gameLogic.isGameOver() && !isAnimating) {
            val x = (event.x / cellSize).toInt()
            val y = (event.y / cellSize).toInt()
            if (gameLogic.board[x][y] == GameLogic.EMPTY) {
                animateMove(x, y, GameLogic.PLAYER_X)
            }
        }
        return true
    }

    private fun animateMove(x: Int, y: Int, player: Int) {
        isAnimating = true
        animProgress[x][y] = 0f
        val animSteps = 10
        fun step(frame: Int = 0) {
            animProgress[x][y] = frame / animSteps.toFloat()
            drawBoard()
            if (frame < animSteps) {
                handler.postDelayed({ step(frame + 1) }, 15)
            } else {
                makeMove(x, y, player)
                animProgress[x][y] = 1f
                isAnimating = false
                drawBoard()
                if (player == GameLogic.PLAYER_X && !gameLogic.isGameOver()) {
                    handler.postDelayed({ aiMove() }, 500)
                }
            }
        }
        step()
    }

    private fun makeMove(x: Int, y: Int, player: Int) {
        gameLogic.makeMove(x, y)
        checkWinOrDraw()
        isPlayerTurn = player == GameLogic.PLAYER_O
        updateStatus()
    }

    private fun aiMove() {
        val move = gameLogic.getComputerMove()
        if (move != null) {
            animateMove(move.first, move.second, GameLogic.PLAYER_O)
            isPlayerTurn = true
            updateStatus()
        }
    }

    private fun checkWinOrDraw() {
        winLine = gameLogic.getWinLine()
        if (gameLogic.isGameOver() && winLine != null) {
            animateWinLine()
        } else if (gameLogic.isGameOver()) {
            statusCallback?.onGameEnd()
        }
    }

    private fun animateWinLine() {
        animWinLineProgress = 0f
        isAnimating = true
        val animSteps = 20
        fun step(frame: Int = 0) {
            animWinLineProgress = frame / animSteps.toFloat()
            drawBoard()
            if (frame < animSteps) {
                handler.postDelayed({ step(frame + 1) }, 15)
            } else {
                animWinLineProgress = 1f
                isAnimating = false
                drawBoard()
                statusCallback?.onGameEnd()
            }
        }
        step()
    }

    private fun updateStatus() {
        if (gameLogic.isGameOver()) {
            val winner = gameLogic.getWinner()
            statusCallback?.onStatusUpdate(
                when (winner) {
                    GameLogic.PLAYER_X -> "You win! 🎉"
                    GameLogic.PLAYER_O -> "Computer wins!"
                    else -> "It's a draw!"
                }
            )
        } else {
            statusCallback?.onStatusUpdate(
                if (isPlayerTurn) "Your turn (X)" else "Computer's turn (O)"
            )
        }
    }

    private fun drawBoard() {
        if (!surfaceReady) return
        val canvas = holder.lockCanvas()
        if (canvas != null) {
            val w = width
            val h = height
            cellSize = min(w, h) / 3f

            // Background
            canvas.drawColor(Color.WHITE)

            // Draw grid
            paint.color = Color.DKGRAY
            for (i in 1..2) {
                canvas.drawLine(cellSize * i, 0f, cellSize * i, cellSize * 3, paint)
                canvas.drawLine(0f, cellSize * i, cellSize * 3, cellSize * i, paint)
            }

            // Draw pieces with animation
            for (x in 0..2) {
                for (y in 0..2) {
                    when (gameLogic.board[x][y]) {
                        GameLogic.PLAYER_X -> drawX(canvas, x, y, animProgress[x][y])
                        GameLogic.PLAYER_O -> drawO(canvas, x, y, animProgress[x][y])
                    }
                }
            }

            // Draw winning line if any
            winLine?.let { line ->
                drawWinLine(canvas, line, animWinLineProgress)
            }

            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawX(canvas: Canvas, x: Int, y: Int, progress: Float) {
        val pad = cellSize * 0.22f
        val sx = x * cellSize + pad
        val sy = y * cellSize + pad
        val ex = (x + 1) * cellSize - pad
        val ey = (y + 1) * cellSize - pad

        paint.color = Color.RED

        // Animate X: first line, then second
        val half = 0.5f
        if (progress < half) {
            val p = progress / half
            canvas.drawLine(sx, sy, sx + (ex - sx) * p, sy + (ey - sy) * p, paint)
        } else {
            canvas.drawLine(sx, sy, ex, ey, paint)
            val p = (progress - half) / half
            canvas.drawLine(sx, ey, sx + (ex - sx) * p, ey - (ey - sy) * p, paint)
        }

        paint.color = Color.DKGRAY
    }

    private fun drawO(canvas: Canvas, x: Int, y: Int, progress: Float) {
        val pad = cellSize * 0.22f
        val cx = x * cellSize + cellSize / 2
        val cy = y * cellSize + cellSize / 2
        val radius = cellSize / 2 - pad

        paint.color = Color.BLUE

        // Animate O: sweep angle 0..360*progress
        val rect = RectF(
            cx - radius, cy - radius,
            cx + radius, cy + radius
        )
        canvas.drawArc(rect, -90f, 360f * progress, false, paint)

        paint.color = Color.DKGRAY
    }

    private fun drawWinLine(canvas: Canvas, line: GameLogic.WinLine, progress: Float) {
        val (start, end) = line
        val pad = cellSize / 2
        val sx = start.first * cellSize + pad
        val sy = start.second * cellSize + pad
        val ex = end.first * cellSize + pad
        val ey = end.second * cellSize + pad

        paint.color = Color.GREEN
        paint.strokeWidth = 18f
        canvas.drawLine(
            sx, sy,
            sx + (ex - sx) * progress,
            sy + (ey - sy) * progress,
            paint
        )
        paint.strokeWidth = 12f
        paint.color = Color.DKGRAY
    }
}