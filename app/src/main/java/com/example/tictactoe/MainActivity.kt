package com.example.tictactoe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.content.res.Resources

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var replayButton: Button
    private lateinit var tttSurfaceView: TicTacToeSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // dp to px helper
        fun dp(dp: Int) = (dp * Resources.getSystem().displayMetrics.density).toInt()

        // Root vertical LinearLayout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        // Status TextView with top margin
        statusTextView = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(12)) // Top and bottom padding for gap above and below
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(32), 0, 0) // Top margin for gap above
            }
        }

        // SurfaceView for the game, square and centered
        tttSurfaceView = TicTacToeSurfaceView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dp(350), // width
                dp(350)  // height
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        // REPLAY button just below the board
        replayButton = Button(this).apply {
            text = "REPLAY"
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = dp(24) // margin between board and button
                bottomMargin = dp(32) // margin below button
            }
            setOnClickListener {
                tttSurfaceView.resetGame()
                visibility = View.GONE
            }
        }

        // Add views in order
        layout.addView(statusTextView)
        layout.addView(tttSurfaceView)
        layout.addView(replayButton)

        setContentView(layout)

        tttSurfaceView.setStatusCallback(
            object : TicTacToeSurfaceView.StatusCallback {
                override fun onStatusUpdate(status: String) {
                    statusTextView.text = status
                }

                override fun onGameEnd() {
                    replayButton.visibility = View.VISIBLE
                }
            }
        )
    }
}