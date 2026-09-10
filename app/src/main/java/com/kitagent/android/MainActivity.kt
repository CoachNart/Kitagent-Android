package com.kitagent.android

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val cyan = Color.rgb(37, 214, 208)
    private val bg = Color.rgb(7, 9, 12)
    private val panelColor = Color.rgb(13, 20, 25)
    private val textColor = Color.rgb(238, 243, 249)
    private val muted = Color.rgb(113, 128, 151)
    private val green = Color.rgb(37, 214, 208)
    private val red = Color.rgb(255, 82, 102)
    private val sections = listOf("Home", "Market Analysis", "Chart Terminal", "CEX", "History", "Profile")
    private var active = 0
    private var btc = 0.0
    private var eth = 0.0
    private var sol = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg; window.navigationBarColor = bg
        buildShell(); showSection(0); refreshHomeMarket()
    }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }

    private fun buildShell() {
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(12,6,12,0) }
        val scroll = ScrollView(this).apply { isFillViewport=true; layoutParams=LinearLayout.LayoutParams(-1,0,1f) }
        content = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(0,0,0,18) }
        scroll.addView(content); root.addView(scroll)
        nav = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER; setBackgroundColor(bg); setPadding(0,3,0,4) }
        root.addView(nav, LinearLayout.LayoutParams(-1,66)); setContentView(root); rebuildNav()
    }

    private fun rebuildNav() {
        nav.removeAllViews()
        sections.forEachIndexed { i, label ->
            val v=TextView(this).apply { text=when(label){"Market Analysis"->"MARKET";"Chart Terminal"->"CHART";else->label.uppercase()}; textSize=8.5f; gravity=Gravity.CENTER; setTextColor(if(i==active)cyan else muted); setPadding(1,8,1,8); setOnClickListener{clickMotion(this);showSection(i)} }
            nav.addView(v,LinearLayout.LayoutParams(0,-1,1f))
        }
    }
    private fun showSection(i:Int){ active=i; content.removeAllViews(); when(i){0->home();1->{header("Market Analysis");market()};2->{header("Chart Terminal");chart()};3->{header("CEX");cex()};4->{header("History");history()};5->{header("Profile");profile()} }; rebuildNav(); content.alpha=0f; content.translationY=14f; content.animate().alpha(1f).translationY(0f).setDuration(240).start() }
    private fun clickMotion(v:View){v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);v.animate().scaleX(.94f).scaleY(.94f).setDuration(55).withEndAction{v.animate().scaleX(1f).scaleY(1f).setDuration(110).start()}.start()}
    private fun header(title:String){val h=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(4,12,4,14)};h.addView(label("KITSETUPS",cyan,10,true));h.addView(label(title,textColor,27,true).apply{setPadding(0,6,0,0)});content.addView(h)}
    private fun label(s:String,c:Int,size:Float,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size;setTextColor(c);if(bold)setTypeface(typeface,1);letterSpacing=if(size<=10).16f else 0f}
    private fun panel()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(panelColor);background=android.graphics.drawable.GradientDrawable().apply{setColor(panelColor);cornerRadius=16f;setStroke(1,Color.rgb(23,36,43))};setPadding(14,14,14,14)}
    private fun addPanel(title:String,body:String,tag:String){val p=panel();p.addView(label(tag,cyan,9,true));p.addView(label(title,textColor,17,true).apply{setPadding(0,6,0,0)});p.addView(label(body,muted,12.5f).apply{setPadding(0,6,0,0);setLineSpacing(3f,1f)});content.addView(p,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=10})}

    private fun home(){
        val top=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(4,16,4,10)};top.addView(label("KITSETUPS",cyan,11,true));top.addView(label("Trade the move.\nSee the market.",textColor,33,true).apply{setPadding(0,8,0,0)});top.addView(label("Native trading cockpit for analysis, execution and review.",muted,13f).apply{setPadding(0,7,0,0)});content.addView(top)
        val hero=panel();val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};r.addView(label("MARKET PULSE",muted,9,true),LinearLayout.LayoutParams(0,-2,1f));r.addView(label("● LIVE",cyan,9,true));hero.addView(r);hero.addView(label("BTC  ${money(btc)}",textColor,28,true).apply{setPadding(0,12,0,0)});hero.addView(label("Live Binance market snapshot",muted,11));hero.addView(SparklineView(this),LinearLayout.LayoutParams(-1,66).apply{topMargin=8});content.addView(hero,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=14})
        section("MARKETS");val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};listOf("BTC" to btc,"ETH" to eth,"SOL" to sol).forEachIndexed{i,(s,v)->val p=panel();p.setPadding(11,11,11,11);p.addView(label(s,muted,9,true));p.addView(label(money(v),textColor,14,true).apply{setPadding(0,4,0,0)});row.addView(p,LinearLayout.LayoutParams(0,-2,1f).apply{if(i<2)rightMargin=6})};content.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=14})
        section("COMMAND CENTER");val actions=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};actionRow(actions,"MARKET","Scan setups"){showSection(1)};actionRow(actions,"CHART","Open terminal"){showSection(2)};actionRow(actions,"CEX","Trade & connect"){showSection(3)};actionRow(actions,"HISTORY","Review flow"){showSection(4)};content.addView(actions);section("PORTFOLIO");addPanel("Portfolio cockpit","Balances, positions and P&L live in the connected CEX workspace.","READY")
    }
    private fun actionRow(box:LinearLayout,title:String,body:String,click:()->Unit){val v=panel();v.setPadding(14,13,14,13);v.setOnClickListener{clickMotion(v);click()};v.addView(label(title,cyan,9,true));v.addView(label(body,textColor,13,true).apply{setPadding(0,5,0,0)});box.addView(v,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=7})}
    private fun section(s:String){content.addView(label(s,muted,9,true).apply{setPadding(4,2,4,7)})}

    private fun market(){addPanel("Market overview","BTC ${money(btc)}\nETH ${money(eth)}\nSOL ${money(sol)}","LIVE");addPanel("Momentum radar","Trend structure • Volatility • Liquidity\n\nLive feeds can be expanded without changing the native shell.","ANALYSIS");addPanel("Setup scanner","Breakout • Reclaim • Sweep • Continuation","SETUPS")}
    private fun chart(){val w=WebView(this);w.settings.javaScriptEnabled=true;w.settings.domStorageEnabled=true;w.setBackgroundColor(bg);w.webChromeClient=android.webkit.WebChromeClient();w.loadUrl("https://www.tradingview.com/widgetembed/?symbol=BINANCE%3ABTCUSDT&interval=60&theme=dark&style=1&locale=en&hide_top_toolbar=0&hide_legend=0&save_image=0&withdateranges=1&hide_side_toolbar=0");content.addView(w,LinearLayout.LayoutParams(-1,500).apply{bottomMargin=10});addPanel("Terminal","BTC/USDT • 1H\nTradingView chart surface inside the native Chart Terminal.","CHART")}

    private fun cex(){
        val terminal=CexTerminalView();content.addView(terminal,LinearLayout.LayoutParams(-1,-2));
    }

    private fun history(){addPanel("Activity","Completed orders, positions and execution events will appear here from the connected exchange.","HISTORY");addPanel("Performance","P&L • Fees • Volume • Win rate\n\nPerformance stays isolated to the native app.","STATS")}
    private fun profile(){addPanel("Account","Native Android session and account state.","ACCOUNT");addPanel("Security","API credentials are runtime inputs only. Use trading/read permissions and never withdrawal-enabled keys.","SECURITY");addPanel("About KitSetups","Independent native Android trading terminal\nVersion 1.0.0","APP")}

    private fun refreshHomeMarket(){scope.launch{val x=withContext(Dispatchers.IO){runCatching{PriceService.snapshot()}.getOrNull()};if(x!=null){btc=x["BTC"]?:0.0;eth=x["ETH"]?:0.0;sol=x["SOL"]?:0.0;if(active==0)showSection(0)}}}
    private fun money(v:Double)=if(v>0)String.format(Locale.US,"$%,.2f",v) else "—"

    private inner class CexTerminalView:LinearLayout(this@MainActivity){
        private var exchange="mexc";private var symbol="BTCUSDT";private var interval="5m";private var connected=false;private var market:CexApi.Market?=null;private var selectedTab="positions";private var status:TextView?=null;private var body:LinearLayout?=null;private var chartView:CexChartView?=null
        init{orientation=VERTICAL;setBackgroundColor(Color.rgb(7,10,15));background=android.graphics.drawable.GradientDrawable().apply{setColor(Color.rgb(7,10,15));cornerRadius=12f;setStroke(1,Color.rgb(23,29,39))};render()}
        private fun small(s:String)=label(s,muted,9,true)
        private fun field(hint:String,secret:Boolean=false):EditText=EditText(this@MainActivity).apply{this.hint=hint;setHintTextColor(muted);setTextColor(textColor);textSize=11f;singleLine=true;if(secret)inputType=0x81;setPadding(9,0,9,0);background=android.graphics.drawable.GradientDrawable().apply{setColor(Color.rgb(12,17,24));cornerRadius=7f;setStroke(1,Color.rgb(32,42,56))}}
        private fun btn(t:String,active:Boolean=false)=Button(this@MainActivity).apply{text=t;textSize=10f;setTextColor(if(active)Color.rgb(6,16,18) else Color.rgb(170,182,199));isAllCaps=false;setPadding(8,0,8,0);background=android.graphics.drawable.GradientDrawable().apply{setColor(if(active)cyan else Color.rgb(17,23,33));cornerRadius=7f;setStroke(1,if(active)cyan else Color.rgb(38,49,65))}}
        private fun render(){removeAllViews();
            val head=LinearLayout(context).apply{orientation=HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(9,8,9,8)};head.addView(label("Futures",textColor,12,true));val sp=Spinner(context);sp.adapter=ArrayAdapter(context,android.R.layout.simple_spinner_dropdown_item,arrayOf("MEXC","Bitget"));sp.setSelection(if(exchange=="mexc")0 else 1);sp.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){};override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){exchange=if(pos==0)"mexc" else "bitget";loadMarket()}};head.addView(sp,LinearLayout.LayoutParams(92,36).apply{leftMargin=7});val pair=field(symbol);pair.setOnEditorActionListener{_,_,_->symbol=pair.text.toString().uppercase().replace("/","");loadMarket();true};head.addView(pair,LinearLayout.LayoutParams(145,36).apply{leftMargin=7});status=label(if(connected)"● Live account" else "○ Market data live",if(connected)cyan else muted,9,true);head.addView(status,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=8});val connect=btn(if(connected)"Account connected" else "Connect exchange",true);connect.setOnClickListener{clickMotion(connect);connectDialog()};head.addView(connect,LinearLayout.LayoutParams(125,36));addView(head)
            val bar=LinearLayout(context).apply{orientation=HORIZONTAL;setPadding(9,7,9,7);setBackgroundColor(Color.rgb(10,14,20))};listOf("LAST","24H","FAIR","INDEX","FUNDING","VOLUME").forEach{bar.addView(metric(it,"—"),LinearLayout.LayoutParams(0,-2,1f))};addView(bar)
            val grid=LinearLayout(context).apply{orientation=HORIZONTAL;minimumHeight=500};chartView=CexChartView(context);val chartBox=LinearLayout(context).apply{orientation=VERTICAL;setPadding(9,9,9,9);addView(label("$symbol Perpetual",textColor,11,true));val tfs=LinearLayout(context).apply{orientation=HORIZONTAL;setPadding(0,6,0,6)};arrayOf("1m","5m","15m","30m","1h","4h","1d").forEach{t->val b=btn(t,t==interval);b.setOnClickListener{interval=t;loadMarket()};tfs.addView(b,LinearLayout.LayoutParams(0,34,1f).apply{rightMargin=3})};addView(tfs);addView(chartView,LinearLayout.LayoutParams(-1,390))};grid.addView(chartBox,LinearLayout.LayoutParams(0,-1,1.25f));grid.addView(orderBook(),LinearLayout.LayoutParams(0,-1,.9f));grid.addView(orderPanel(),LinearLayout.LayoutParams(0,-1,1f));addView(grid)
            val tabs=LinearLayout(context).apply{orientation=HORIZONTAL;setPadding(9,8,9,0)};arrayOf("Positions","Open Orders","Order History").forEachIndexed{i,t->val b=btn(t,selectedTab==arrayOf("positions","orders","history")[i]);b.setOnClickListener{selectedTab=arrayOf("positions","orders","history")[i];render()};addView(b,LinearLayout.LayoutParams(0,36,1f).apply{rightMargin=4})};addView(tabs);body=LinearLayout(context).apply{orientation=VERTICAL;setPadding(9,7,9,12)};body!!.addView(label(if(connected)"Connected account data will populate here." else "Connect an exchange to load account positions, open orders and history.",muted,10));addView(body)
            loadMarket()
        }
        private fun metric(a:String,b:String):LinearLayout{val x=LinearLayout(context).apply{orientation=VERTICAL};x.addView(label(a,muted,8));x.addView(label(b,textColor,10,true).apply{setPadding(0,3,0,0)});return x}
        private fun orderBook():LinearLayout{val p=panel();p.setPadding(9,9,9,9);p.addView(label("Order Book",textColor,11,true));val m=market;val asks=m?.asks?.take(12)?.reversed()?:emptyList();val bids=m?.bids?.take(12)?:emptyList();asks.forEach{x->p.addView(label(String.format(Locale.US,"%.2f    %.4f",x[0],x[1]),red,9f))};p.addView(label(m?.last?.let{String.format(Locale.US,"%.2f",it)}?:"—",textColor,11,true).apply{setPadding(0,7,0,7)});bids.forEach{x->p.addView(label(String.format(Locale.US,"%.2f    %.4f",x[0],x[1]),green,9f))};return p}
        private fun orderPanel():LinearLayout{val p=panel();p.setPadding(9,9,9,9);p.addView(label("Order",textColor,11,true));val sides=LinearLayout(context).apply{orientation=HORIZONTAL};val l=btn("Open Long",true);val s=btn("Open Short");sides.addView(l,LinearLayout.LayoutParams(0,38,1f));sides.addView(s,LinearLayout.LayoutParams(0,38,1f).apply{leftMargin=4});p.addView(sides);p.addView(field("Margin: Cross / Isolated").apply{setPadding(9,0,9,0)},LinearLayout.LayoutParams(-1,40).apply{topMargin=8});p.addView(field("Order size"),LinearLayout.LayoutParams(-1,40).apply{topMargin=7});p.addView(field("Limit price (optional)"),LinearLayout.LayoutParams(-1,40).apply{topMargin=7});p.addView(label("Leverage    5x",muted,9).apply{setPadding(0,8,0,4)});p.addView(SeekBar(context).apply{max=99;progress=4});p.addView(field("Take Profit (optional)"),LinearLayout.LayoutParams(-1,40).apply{topMargin=6});p.addView(field("Stop Loss (optional)"),LinearLayout.LayoutParams(-1,40).apply{topMargin=6});val submit=btn(if(connected)"Open Long" else "Connect exchange first",true);submit.setOnClickListener{if(!connected)connectDialog() else Toast.makeText(context,"Order review opened — confirm symbol, size, leverage and risk before execution.",Toast.LENGTH_LONG).show()};p.addView(submit,LinearLayout.LayoutParams(-1,42).apply{topMargin=8});p.addView(label(if(connected)"Balance: connected" else "Balance: —",muted,9).apply{setPadding(0,8,0,0)});return p}
        private fun connectDialog(){val box=LinearLayout(context).apply{orientation=VERTICAL;setPadding(18,4,18,4)};val key=field("API key");val secret=field("API secret",true);val pass=field("API passphrase (Bitget only)");box.addView(key,LinearLayout.LayoutParams(-1,50));box.addView(secret,LinearLayout.LayoutParams(-1,50).apply{topMargin=8});box.addView(pass,LinearLayout.LayoutParams(-1,50).apply{topMargin=8});AlertDialog.Builder(context).setTitle("Connect ${exchange.uppercase()}").setView(box).setPositiveButton("TEST CONNECTION"){_,_->testConnection(key.text.toString().trim(),secret.text.toString(),pass.text.toString())}.setNegativeButton("Cancel",null).show()}
        private fun testConnection(key:String,secret:String,pass:String){status?.text="● Checking account…";status?.setTextColor(cyan);scope.launch{val r=withContext(Dispatchers.IO){runCatching{CexApi.account(exchange,key,secret,pass)}};r.onSuccess{a->connected=true;status?.text="● Live account";status?.setTextColor(cyan);Toast.makeText(context,"Connected • ${a.balances.size} non-zero assets",Toast.LENGTH_SHORT).show();render()}.onFailure{status?.text="○ Connection failed";status?.setTextColor(red);Toast.makeText(context,it.message?:"Exchange connection failed",Toast.LENGTH_LONG).show()}}}
        private fun loadMarket(){scope.launch{val r=withContext(Dispatchers.IO){runCatching{CexApi.market(exchange,symbol,interval)}};r.onSuccess{market=it;chartView?.candles=it.candles;renderMarketMetrics();chartView?.invalidate()}.onFailure{status?.text="○ Market data unavailable";status?.setTextColor(red)}}}
        private fun renderMarketMetrics(){val b=getChildAt(1) as? LinearLayout ?: return;val m=market?:return;val vals=listOf(m.last.toString(),String.format(Locale.US,"%.2f%%",m.change),m.fair.toString(),m.index.toString(),String.format(Locale.US,"%.4f%%",m.funding),m.volume.toString());for(i in 0 until minOf(6,b.childCount)){val box=b.getChildAt(i) as LinearLayout;(box.getChildAt(1) as TextView).text=vals[i]}}
    }

    private class CexChartView(context:android.content.Context):View(context){var candles:List<CexApi.Candle> = emptyList();private val p=Paint(1);override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(8,11,16));if(candles.isEmpty())return;val min=candles.minOf{it.low};val max=candles.maxOf{it.high};val range=(max-min).coerceAtLeast(.000001);val path=Path();candles.takeLast(90).forEachIndexed{i,x->val xx=i*width.toFloat()/89f;val yy=height-(x.close-min)/range*height;if(i==0)path.moveTo(xx,yy)else path.lineTo(xx,yy)};p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.rgb(37,214,208);c.drawPath(path,p)}}
}

class SparklineView(context:android.content.Context):View(context){private val p=Paint(1);override fun onDraw(c:Canvas){super.onDraw(c);p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.rgb(37,214,208);val path=Path();for(i in 0..40){val x=i*width/40f;val y=height*.55f+(kotlin.math.sin(i*.42)*height*.2f)+(i%7)*1.4f;if(i==0)path.moveTo(x,y)else path.lineTo(x,y)};c.drawPath(path,p)}}
