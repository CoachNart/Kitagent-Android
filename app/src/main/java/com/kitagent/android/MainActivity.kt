package com.kitagent.android

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
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
            setPadding(18, 10, 18, 0)
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 22)
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
        if (index == 0) home() else {
            addHeader(sections[index])
            when (index) {
                1 -> market()
                2 -> chart()
                3 -> cex()
                4 -> history()
                5 -> profile()
            }
        }
        updateNav(index)
        content.alpha = 0f
        content.translationY = 18f
        content.animate().alpha(1f).translationY(0f).setDuration(300).setInterpolator(DecelerateInterpolator()).start()
    }

    private fun addHeader(title: String) {
        val header = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(4, 12, 4, 20) }
        val eyebrow = TextView(this).apply { text = "KITSETUPS"; textSize = 10f; letterSpacing = 0.22f; setTextColor(cyan) }
        header.addView(eyebrow)
        val heading = TextView(this).apply { text = title; textSize = 29f; setTypeface(typeface, Typeface.BOLD); setTextColor(text); setPadding(0, 7, 0, 0) }
        header.addView(heading)
        content.addView(header)
    }

    private fun updateNav(active: Int) {
        for (i in 0 until nav.childCount) (nav.getChildAt(i) as TextView).setTextColor(if (i == active) cyan else muted)
    }

    private fun home() {
        val top = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(4, 18, 4, 12) }
        val brand = TextView(this).apply {
            text = "KITSETUPS"
            textSize = 11f
            letterSpacing = 0.25f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(cyan)
        }
        top.addView(brand)
        val greeting = TextView(this).apply {
            text = "Your market.\nYour edge."
            textSize = 34f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(text)
            setPadding(0, 10, 0, 0)
        }
        top.addView(greeting)
        val sub = TextView(this).apply {
            text = "A live trading cockpit built for decisions, not decoration."
            textSize = 13f
            setTextColor(muted)
            setPadding(0, 8, 0, 0)
        }
        top.addView(sub)
        content.addView(top)

        val pulse = addHeroPanel()
        pulse.postDelayed({ animatePulse(pulse) }, 350)

        addSectionLabel("MARKETS")
        addMarketStrip()
        addSectionLabel("COMMAND CENTER")
        addActionGrid()
        addSectionLabel("YOUR FLOW")
        addPanel("Portfolio", "Connect a wallet or CEX to bring balances, positions and P&L into one view.", "READY")
    }

    private fun addHeroPanel(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundResource(com.kitagent.android.R.drawable.bg_panel)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 18 }
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val label = TextView(this).apply { text = "MARKET PULSE"; textSize = 10f; letterSpacing = 0.16f; setTypeface(typeface, Typeface.BOLD); setTextColor(muted) }
        row.addView(label, LinearLayout.LayoutParams(0, -2, 1f))
        val live = TextView(this).apply { text = "● LIVE"; textSize = 10f; setTypeface(typeface, Typeface.BOLD); setTextColor(cyan) }
        row.addView(live)
        card.addView(row)
        val value = TextView(this).apply { text = "BTC  $104,820"; textSize = 28f; setTypeface(typeface, Typeface.BOLD); setTextColor(text); setPadding(0, 14, 0, 0) }
        card.addView(value)
        val change = TextView(this).apply { text = "+2.84%     BTC / USDT"; textSize = 12f; setTextColor(cyan); setPadding(0, 5, 0, 0) }
        card.addView(change)
        return card
    }

    private fun animatePulse(view: View) {
        val pulse = ObjectAnimator.ofFloat(view, View.ALPHA, 0.72f, 1f)
        pulse.duration = 1100
        pulse.repeatMode = ObjectAnimator.REVERSE
        pulse.repeatCount = ObjectAnimator.INFINITE
        pulse.start()
    }

    private fun addSectionLabel(label: String) {
        val v = TextView(this).apply { text = label; textSize = 9f; letterSpacing = 0.18f; setTypeface(typeface, Typeface.BOLD); setTextColor(muted); setPadding(4, 2, 4, 8) }
        content.addView(v)
    }

    private fun addMarketStrip() {
        val strip = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 0, 0, 16) }
        listOf("BTC\n104.8K", "ETH\n3.8K", "SOL\n241").forEachIndexed { i, item ->
            val v = TextView(this).apply { text = item; textSize = 12f; setTextColor(text); setTypeface(typeface, Typeface.BOLD); setPadding(14, 14, 14, 14); setBackgroundResource(com.kitagent.android.R.drawable.bg_panel) }
            strip.addView(v, LinearLayout.LayoutParams(0, -2, 1f).apply { if (i < 2) rightMargin = 7 })
        }
        content.addView(strip)
    }

    private fun addActionGrid() {
        val actions = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 10 } }
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        addAction(row1, "MARKET", "Scan setups")
        addAction(row1, "CHART", "Open terminal")
        actions.addView(row1)
        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 8, 0, 0) }
        addAction(row2, "CEX", "Trade & connect")
        addAction(row2, "HISTORY", "Review flow")
        actions.addView(row2)
        content.addView(actions)
    }

    private fun addAction(row: LinearLayout, title: String, body: String) {
        val v = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(15, 15, 15, 15)
            setBackgroundResource(com.kitagent.android.R.drawable.bg_panel)
            setOnClickListener { performClickMotion(this) }
        }
        val t = TextView(this).apply { text = title; textSize = 9f; letterSpacing = 0.14f; setTypeface(typeface, Typeface.BOLD); setTextColor(cyan) }
        val b = TextView(this).apply { text = body; textSize = 13f; setTypeface(typeface, Typeface.BOLD); setTextColor(text); setPadding(0, 7, 0, 0) }
        v.addView(t); v.addView(b)
        row.addView(v, LinearLayout.LayoutParams(0, -2, 1f).apply { if (row.childCount == 1) rightMargin = 8 })
    }

    private fun performClickMotion(view: View) {
        view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
        }.start()
    }

    private fun market() { addPanel("Market overview", "BTC / ETH / SOL\nLive market data will populate this terminal.", "MARKET"); addPanel("Analysis", "Market structure\nKey levels\nTrend context\nSetup analysis", "ANALYSIS") }
    private fun chart() { addPanel("Chart Terminal", "Native Android terminal surface reserved for the chart engine.\n\nTradingView integration will be wired here without wrapping the web app.", "CHART"); addPanel("Terminal controls", "Symbol    •    Timeframe    •    Indicators    •    Drawing tools", "TOOLS") }
    private fun cex() { addPanel("CEX connections", "Connect supported exchanges and manage trading connections securely.", "CONNECTIONS"); addPanel("Orders", "Open orders, positions and account activity will appear here.", "ORDERS") }
    private fun history() { addPanel("Transaction history", "Transfers, swaps and trading activity will be listed here.", "HISTORY"); addPanel("Trade activity", "Execution records and completed orders will appear here.", "TRADES") }
    private fun profile() { addPanel("Account", "Authentication and account settings will live here.", "ACCOUNT"); addPanel("Security", "Secure session, wallet and device settings.", "SECURITY") }

    private fun addPanel(title: String, body: String, tag: String) {
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 16, 18, 18); setBackgroundResource(com.kitagent.android.R.drawable.bg_panel); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 } }
        val tagView = TextView(this).apply { text = tag; textSize = 9f; letterSpacing = 0.16f; setTypeface(typeface, Typeface.BOLD); setTextColor(cyan) }
        card.addView(tagView)
        val titleView = TextView(this).apply { text = title; textSize = 16f; setTypeface(typeface, Typeface.BOLD); setTextColor(text); setPadding(0, 8, 0, 0) }
        card.addView(titleView)
        val bodyView = TextView(this).apply { text = body; textSize = 13f; setTextColor(muted); setLineSpacing(3f, 1f); setPadding(0, 7, 0, 0) }
        card.addView(bodyView)
        content.addView(card)
    }
}
