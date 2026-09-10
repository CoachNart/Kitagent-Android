package com.kitagent.android

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(7, 9, 12)
        window.navigationBarColor = Color.rgb(7, 9, 12)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.rgb(7, 9, 12))
        }

        val mark = TextView(this).apply {
            text = "KITAGENT"
            textSize = 13f
            setTextColor(Color.rgb(78, 226, 255))
            letterSpacing = 0.18f
        }
        root.addView(mark, LinearLayout.LayoutParams(-1, -2))

        val title = TextView(this).apply {
            text = "Your crypto\ntransaction agent."
            textSize = 38f
            setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 18, 0, 12)
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        val body = TextView(this).apply {
            text = "A native Android terminal for sending, swapping and managing onchain transactions."
            textSize = 16f
            setTextColor(Color.rgb(157, 166, 178))
            setLineSpacing(4f, 1f)
        }
        root.addView(body, LinearLayout.LayoutParams(-1, -2))

        val spacer = TextView(this)
        root.addView(spacer, LinearLayout.LayoutParams(1, 34))

        val status = TextView(this).apply {
            text = "●  ANDROID CORE ONLINE"
            textSize = 12f
            setTextColor(Color.rgb(78, 226, 255))
            setPadding(18, 16, 18, 16)
            setBackgroundColor(Color.rgb(13, 20, 25))
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))

        setContentView(root)
    }
}
