package com.kitagent.android

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout
    private val cyan = Color.rgb(78, 226, 255)
    private val bg = Color.rgb(7, 9, 12)
    private val panel = Color.rgb(13, 20, 25)
    private val text = Color.rgb(245, 247, 250)
    private val muted = Color.rgb(143, 155, 168)

    private val sections = listOf("Home", "Market Analysis", "Chart Terminal", "CEX", "History", "Profile")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildShell()
        showSection(0)
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(18, 18, 18, 8)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        root.addView(content)

        nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 4)
        }
        root.addView(nav, LinearLayout.LayoutParams(-1, 66))
        setContentView(root)
        rebuildNav()
    }

    private fun rebuildNav() {
        nav.removeAllViews()
        sections.forEachIndexed { index, label ->
            val item = TextView(this).apply {
                text = label
                textSize = if (label == "Chart Terminal") 10f else 9f
                gravity = Gravity.CENTER
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(if (index == 0) cyan else muted)
                setPadding(4, 8, 4, 8)
                isAllCaps = false
                setOnClickListener { showSection(index) }
            }
            nav.addView(item, LinearLayout.LayoutParams(0, -1, 1f))
        }
    }

    private fun showSection(index: Int) {
        content.removeAllViews()
        val title = sections[index]
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 10, 4, 18)
        }
        val eyebrow = TextView(this).apply {
            text = "KITAGENT"
            textSize = 11f
            letterSpacing = 0.16f
            setTextColor(cyan)
        }
        header.addView(eyebrow)
        val heading = TextView(this).apply {
            text = title
            textSize = 30f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(text)
            setPadding(0, 8, 0, 0)
        }
        header.addView(heading)
        content.addView(header)

        when (index) {
            0 -> home()
            1 -> market()
            2 -> chart()
            3 -> cex()
            4 -> history()
            5 -> profile()
        }
        updateNav(index)
    }

    private fun updateNav(active: Int) {
        for (i in 0 until nav.childCount) {
            (nav.getChildAt(i) as TextView).setTextColor(if (i == active) cyan else muted)
        }
    }

    private fun home() {
        addPanel("Portfolio", "Connect a wallet or CEX to see balances and positions.")
        addPanel("Quick access", "Market Analysis   •   Chart Terminal   •   CEX")
        addPanel("Activity", "Your latest transactions and trading activity will appear here.")
    }

    private fun market() {
        addPanel("Market overview", "BTC / ETH / SOL\nLive market data will populate this native terminal.")
        addPanel("Analysis", "Market structure, levels, trend context and setup analysis.")
    }

    private fun chart() {
        addPanel("Chart Terminal", "TradingView chart integration will live here.\n\nThis screen is reserved for the full chart terminal.")
    }

    private fun cex() {
        addPanel("CEX connections", "Connect supported exchanges and manage trading connections securely.")
        addPanel("Orders", "Open orders, positions and account activity will appear here.")
    }

    private fun history() {
        addPanel("Transaction history", "Transfers, swaps and trading activity will be listed here.")
    }

    private fun profile() {
        addPanel("Account", "Authentication and account settings will live here.")
        addPanel("Security", "Secure session, wallet and device settings.")
    }

    private fun addPanel(title: String, body: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundResource(com.kitagent.android.R.drawable.bg_panel)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 }
        }
        val t = TextView(this).apply {
            text = title
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(text)
        }
        card.addView(t)
        val b = TextView(this).apply {
            text = body
            textSize = 13f
            setTextColor(muted)
            setLineSpacing(4f, 1f)
            setPadding(0, 8, 0, 0)
        }
        card.addView(b)
        content.addView(card)
    }
}
