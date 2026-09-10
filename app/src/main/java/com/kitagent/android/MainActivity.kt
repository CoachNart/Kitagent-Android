package com.kitagent.android

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val cyan = Color.rgb(37, 214, 208)
    private val bg = Color.rgb(7, 9, 12)
    private val muted = Color.rgb(113, 128, 151)
    private lateinit var web: WebView
    private lateinit var nav: LinearLayout
    private var active = 0
    private val sections = listOf("Home", "Market Analysis", "Chart Terminal", "CEX", "History", "Profile")

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildShell()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        web = WebView(this).apply {
            setBackgroundColor(bg)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.setSupportZoom(false)
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))

        nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(9, 12, 16))
            setPadding(6, 5, 6, 7)
            elevation = 16f
        }
        root.addView(nav, LinearLayout.LayoutParams(-1, 78))
        setContentView(root)
        drawNav()
        openSection(0)
    }

    private fun drawNav() {
        nav.removeAllViews()
        sections.forEachIndexed { index, name ->
            val label = when (index) {
                1 -> "Market"
                2 -> "Chart"
                else -> name
            }
            val item = TextView(this).apply {
                text = label
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(2, 4, 2, 4)
                minHeight = 60
                setTextColor(if (index == active) cyan else muted)
                setTypeface(typeface, if (index == active) 1 else 0)
                setOnClickListener { openSection(index) }
            }
            nav.addView(item, LinearLayout.LayoutParams(0, -1, 1f))
        }
    }

    private fun openSection(index: Int) {
        active = index
        drawNav()
        when (index) {
            0 -> web.loadUrl("https://kitsetups.xyz/")
            1 -> openWebPage("Market analysis")
            2 -> web.loadUrl("https://www.tradingview.com/widgetembed/?symbol=BINANCE%3ABTCUSDT&interval=60&theme=dark&style=1&locale=en&hide_top_toolbar=0&hide_legend=0&save_image=0&withdateranges=1&hide_side_toolbar=0")
            3 -> openWebPage("Perpetuals")
            4 -> openWebPage("History")
            5 -> openWebPage("Profile")
        }
    }

    private fun openWebPage(target: String) {
        web.loadUrl("https://kitsetups.xyz/")
        web.postDelayed({
            val escaped = target.replace("'", "\\'")
            val script = """
                (function() {
                    var wanted = '$escaped'.toLowerCase();
                    var nodes = Array.from(document.querySelectorAll('button,a,[role="button"]'));
                    var el = nodes.find(function(x) {
                        return (x.innerText || '').trim().toLowerCase() === wanted;
                    });
                    if (el) { el.click(); }
                })();
            """.trimIndent()
            web.evaluateJavascript(script, null)
        }, 1200)
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
