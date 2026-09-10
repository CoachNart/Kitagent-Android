package com.kitagent.android

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout

    private val cyan = Color.rgb(78, 226, 255)
    private val bg = Color.rgb(7, 9, 12)
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
            setPadding(18, 14, 18, 0)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 18)
        }
        scroll.addView(content)
        root.addView(scroll)

        nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 4)
            setBackgroundColor(bg)
        }
        root.addView(nav, LinearLayout.LayoutParams(-1, 66))
        setContentView(root)
        rebuildNav()
    }

    private fun rebuildNav() {
        nav.removeAllViews()
        sections.forEachIndexed { index, label ->
            val item = TextView(this).apply {
                text = navLabel(label)
                textSize = 9f
                gravity = Gravity.CENTER
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(if (index == 0) cyan else muted)
                setPadding(2, 8, 2, 8)
                setOnClickListener { showSection(index) }
            }
            nav.addView(item, LinearLayout.LayoutParams(0, -1, 1f))
        }
    }

    private fun navLabel(label: String): String = when (label) {
        "Market Analysis" -> "MARKET"
        "Chart Terminal" -> "CHART"
        else -> label.uppercase()
    }

    private fun showSection(index: Int) {
        content.removeAllViews()
        addHeader(sections[index])
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

    private fun addHeader(title: String) {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 12, 4, 20)
        }
        val eyebrow = TextView(this).apply {
            text = "KITSETUPS"
            textSize = 10f
            letterSpacing = 0.22f
            setTextColor(cyan)
        }
        header.addView(eyebrow)

        val heading = TextView(this).apply {
            text = title
            textSize = 29f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(text)
            setPadding(0, 7, 0, 0)
        }
        header.addView(heading)
        content.addView(header)
    }

    private fun updateNav(active: Int) {
        for (i in 0 until nav.childCount) {
            (nav.getChildAt(i) as TextView).setTextColor(if (i == active) cyan else muted)
        }
    }

    private fun home() {
        addPanel("Portfolio", "Connect a wallet or CEX to see balances and positions.", "READY")
        addPanel("Quick access", "Market Analysis    •    Chart Terminal    •    CEX", "TERMINAL")
        addPanel("Activity", "Your latest transactions and trading activity will appear here.", "HISTORY")
    }

    private fun market() {
        addPanel("Market overview", "BTC / ETH / SOL\nLive market data will populate this terminal.", "MARKET")
        addPanel("Analysis", "Market structure\nKey levels\nTrend context\nSetup analysis", "ANALYSIS")
    }

    private fun chart() {
        addPanel("Chart Terminal", "Native Android terminal surface reserved for the chart engine.\n\nTradingView integration will be wired here without wrapping the web app.", "CHART")
        addPanel("Terminal controls", "Symbol    •    Timeframe    •    Indicators    •    Drawing tools", "TOOLS")
    }

    private fun cex() {
        addPanel("CEX connections", "Connect supported exchanges and manage trading connections securely.", "CONNECTIONS")
        addPanel("Orders", "Open orders, positions and account activity will appear here.", "ORDERS")
    }

    private fun history() {
        addPanel("Transaction history", "Transfers, swaps and trading activity will be listed here.", "HISTORY")
        addPanel("Trade activity", "Execution records and completed orders will appear here.", "TRADES")
    }

    private fun profile() {
        addPanel("Account", "Authentication and account settings will live here.", "ACCOUNT")
        addPanel("Security", "Secure session, wallet and device settings.", "SECURITY")
    }

    private fun addPanel(title: String, body: String, tag: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 18)
            setBackgroundResource(com.kitagent.android.R.drawable.bg_panel)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 }
        }

        val tagView = TextView(this).apply {
            text = tag
            textSize = 9f
            letterSpacing = 0.16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(cyan)
        }
        card.addView(tagView)

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(text)
            setPadding(0, 8, 0, 0)
        }
        card.addView(titleView)

        val bodyView = TextView(this).apply {
            text = body
            textSize = 13f
            setTextColor(muted)
            setLineSpacing(3f, 1f)
            setPadding(0, 7, 0, 0)
        }
        card.addView(bodyView)
        content.addView(card)
    }
}
