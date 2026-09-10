package com.kitagent.android

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val cyan = Color.rgb(78, 226, 255)
    private val bg = Color.rgb(7, 9, 12)
    private val panelColor = Color.rgb(13, 20, 25)
    private val textColor = Color.rgb(245, 247, 250)
    private val muted = Color.rgb(143, 155, 168)
    private val green = Color.rgb(65, 224, 157)
    private val sections = listOf("Home", "Market Analysis", "Chart Terminal", "CEX", "History", "Profile")
    private var active = 0
    private var btc = 104820.0
    private var eth = 3800.0
    private var sol = 241.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildShell()
        showSection(0)
        refreshMarket()
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }

    private fun buildShell() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(16, 6, 16, 0) }
        val scroll = ScrollView(this).apply { isFillViewport = true; layoutParams = LinearLayout.LayoutParams(-1, 0, 1f) }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, 22) }
        scroll.addView(content); root.addView(scroll)
        nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, 3, 0, 4); setBackgroundColor(bg) }
        root.addView(nav, LinearLayout.LayoutParams(-1, 68)); setContentView(root); rebuildNav()
    }

    private fun rebuildNav() {
        nav.removeAllViews()
        sections.forEachIndexed { index, label ->
            val item = TextView(this).apply {
                text = navLabel(label); textSize = 8.5f; gravity = Gravity.CENTER; setTextColor(if (index == active) cyan else muted); setPadding(1, 8, 1, 8)
                setOnClickListener { performClickMotion(this); showSection(index) }
            }
            nav.addView(item, LinearLayout.LayoutParams(0, -1, 1f))
        }
    }

    private fun navLabel(label: String) = when (label) { "Market Analysis" -> "MARKET"; "Chart Terminal" -> "CHART"; else -> label.uppercase() }

    private fun showSection(index: Int) {
        active = index; content.removeAllViews()
        if (index == 0) home() else { addHeader(sections[index]); when (index) { 1 -> market(); 2 -> chart(); 3 -> cex(); 4 -> history(); 5 -> profile() } }
        rebuildNav(); content.alpha = 0f; content.translationY = 16f
        content.animate().alpha(1f).translationY(0f).setDuration(260).start()
    }

    private fun addHeader(title: String) {
        val h = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(4, 14, 4, 16) }
        h.addView(label("KITSETUPS", cyan, 10f, true)); h.addView(label(title, textColor, 28f, true).apply { setPadding(0, 7, 0, 0) }); content.addView(h)
    }

    private fun label(s: String, c: Int, size: Float, bold: Boolean = false) = TextView(this).apply { text = s; textSize = size; setTextColor(c); if (bold) setTypeface(typeface, 1); letterSpacing = if (size <= 10f) .16f else 0f }

    private fun home() {
        content.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(4, 18, 4, 10)
            addView(label("KITSETUPS", cyan, 11f, true))
            addView(label("Trade the move.\nSee the market.", textColor, 34f, true).apply { setPadding(0, 9, 0, 0) })
            addView(label("A native trading cockpit for analysis, execution and review.", muted, 13f).apply { setPadding(0, 8, 0, 0) })
        })
        val hero = panel(); hero.setPadding(20, 18, 20, 18)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(label("MARKET PULSE", muted, 9f, true), LinearLayout.LayoutParams(0, -2, 1f)); row.addView(label("● LIVE", cyan, 9f, true)); hero.addView(row)
        hero.addView(label("BTC  ${money(btc)}", textColor, 29f, true).apply { setPadding(0, 13, 0, 0) })
        hero.addView(label("+2.84%     BTC / USDT", green, 12f).apply { setPadding(0, 4, 0, 0) })
        hero.addView(SparklineView(this), LinearLayout.LayoutParams(-1, 70).apply { topMargin = 10 })
        content.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 16 })
        sectionLabel("MARKETS"); marketStrip(); sectionLabel("COMMAND CENTER"); actionGrid(); sectionLabel("PORTFOLIO")
        addPanel("Portfolio cockpit", "Balances, positions and P&L in one view. Connect your account when you're ready.", "READY")
    }

    private fun marketStrip() {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("BTC" to money(btc), "ETH" to money(eth), "SOL" to money(sol)).forEachIndexed { i, p ->
            val card = panel(); card.setPadding(14, 13, 14, 13); card.addView(label(p.first, muted, 9f, true)); card.addView(label(p.second, textColor, 15f, true).apply { setPadding(0, 5, 0, 0) })
            row.addView(card, LinearLayout.LayoutParams(0, -2, 1f).apply { if (i < 2) rightMargin = 7 })
        }
        content.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 15 })
    }

    private fun actionGrid() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val a = LinearLayout(this); addAction(a, "MARKET", "Scan setups") { showSection(1) }; addAction(a, "CHART", "Open terminal") { showSection(2) }; box.addView(a)
        val b = LinearLayout(this).apply { setPadding(0, 8, 0, 0) }; addAction(b, "CEX", "Trade & connect") { showSection(3) }; addAction(b, "HISTORY", "Review flow") { showSection(4) }; box.addView(b); content.addView(box)
    }

    private fun addAction(row: LinearLayout, title: String, body: String, click: () -> Unit) {
        val v = panel(); v.setPadding(15, 15, 15, 15)
        v.setOnClickListener { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); v.animate().scaleX(.97f).scaleY(.97f).setDuration(70).withEndAction { v.animate().scaleX(1f).scaleY(1f).setDuration(130).start(); click() }.start() }
        v.addView(label(title, cyan, 9f, true)); v.addView(label(body, textColor, 13f, true).apply { setPadding(0, 7, 0, 0) })
        row.addView(v, LinearLayout.LayoutParams(0, -2, 1f).apply { if (row.childCount > 0) rightMargin = 8 })
    }

    private fun market() {
        addPanel("Market overview", "BTC  ${money(btc)}\nETH  ${money(eth)}\nSOL  ${money(sol)}", "LIVE")
        addPanel("Momentum radar", "Trend: bullish\nVolatility: elevated\nLiquidity: healthy\nWatch BTC structure around current range.", "ANALYSIS")
        addPanel("Setup scanner", "Breakout • Reclaim • Sweep • Continuation\n\nSignal engine is ready for live market feeds.", "SETUPS")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun chart() {
        val w = WebView(this); w.setBackgroundColor(bg); w.settings.javaScriptEnabled = true; w.settings.domStorageEnabled = true; w.settings.cacheMode = WebSettings.LOAD_DEFAULT; w.webChromeClient = WebChromeClient()
        val url = "https://www.tradingview.com/widgetembed/?symbol=BINANCE%3ABTCUSDT&interval=60&theme=dark&style=1&locale=en&hide_top_toolbar=0&hide_legend=0&save_image=0&withdateranges=1&hide_side_toolbar=0"
        w.loadUrl(url); content.addView(w, LinearLayout.LayoutParams(-1, 520).apply { bottomMargin = 12 }); addPanel("Terminal", "BTC/USDT • 1H\nTradingView chart surface with native Android controls around it.", "CHART")
    }

    private fun cex() {
        addPanel("Exchange connections", "Connect supported CEX accounts through secure API credentials. Secrets are never displayed or logged by this app.", "CEX")
        addPanel("Execution desk", "Spot • Perpetuals • Orders • Positions\n\nOrder placement will require explicit user confirmation before execution.", "TRADE")
        addPanel("Risk guard", "Review symbol, side, size, leverage and estimated fees before submitting an order.", "SAFETY")
    }

    private fun history() {
        addPanel("Activity", "Your completed trades, transfers and swaps will appear here.", "HISTORY")
        addPanel("Performance", "Win rate • P&L • Fees • Volume\n\nPerformance analytics will be calculated from locally cached execution records.", "STATS")
    }

    private fun profile() {
        addPanel("Account", "Sign-in/session integration is isolated to this native Android repo. No credentials are shared with the web shell.", "ACCOUNT")
        addPanel("Security", "Biometric lock • Session controls • Secure storage\n\nWallet keys and exchange secrets must remain in Android secure storage and are never hardcoded.", "SECURITY")
        addPanel("About KitSetups", "Native Android trading terminal\nVersion 1.0.0", "APP")
    }

    private fun sectionLabel(s: String) { content.addView(label(s, muted, 9f, true).apply { setPadding(4, 2, 4, 8) }) }
    private fun addPanel(title: String, body: String, tag: String) { val c = panel(); c.setPadding(18, 16, 18, 18); c.addView(label(tag, cyan, 9f, true)); c.addView(label(title, textColor, 17f, true).apply { setPadding(0, 7, 0, 0) }); c.addView(label(body, muted, 13f).apply { setPadding(0, 7, 0, 0); setLineSpacing(3f, 1f) }); content.addView(c, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 }) }
    private fun panel() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(panelColor); setPadding(18, 16, 18, 16); background = android.graphics.drawable.GradientDrawable().apply { setColor(panelColor); cornerRadius = 18f; setStroke(1, Color.rgb(23, 36, 43)) } }

    private fun refreshMarket() { scope.launch { val data = withContext(Dispatchers.IO) { fetchPrices() }; if (data != null) { btc = data[0]; eth = data[1]; sol = data[2]; if (active == 0) showSection(0) } } }
    private fun fetchPrices(): DoubleArray? = try { val symbols = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT"); DoubleArray(3) { i -> val c = URL("https://api.binance.com/api/v3/ticker/price?symbol=${symbols[i]}").openConnection() as HttpURLConnection; c.connectTimeout = 5000; c.readTimeout = 5000; val s = c.inputStream.bufferedReader().use { it.readText() }; Regex("\\\"price\\\":\\\"([0-9.]+)\\\"").find(s)?.groupValues?.get(1)?.toDouble() ?: 0.0 } } catch (_: Exception) { null }
    private fun money(v: Double) = if (v >= 1000) String.format(Locale.US, "$%,.0f", v) else String.format(Locale.US, "$%.2f", v)

    class SparklineView(c: android.content.Context) : View(c) {
        private val p = Paint(1).apply { color = Color.rgb(78, 226, 255); style = Paint.Style.STROKE; strokeWidth = 4f; strokeCap = Paint.Cap.ROUND }
        private val values = floatArrayOf(.52f, .48f, .56f, .51f, .62f, .58f, .66f, .61f, .72f, .68f, .81f, .76f, .90f, .84f, .96f)
        override fun onDraw(canvas: Canvas) { super.onDraw(canvas); val path = Path(); for (i in values.indices) { val x = i * (width.toFloat() / (values.size - 1)); val y = height * .9f - values[i] * height * .72f; if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }; canvas.drawPath(path, p) }
    }
}
