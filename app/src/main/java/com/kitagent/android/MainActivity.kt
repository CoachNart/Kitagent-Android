package com.kitagent.android

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
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
    private val cyan = Color.rgb(37,214,208)
    private val bg = Color.rgb(7,9,12)
    private val panel = Color.rgb(13,20,25)
    private val text = Color.rgb(238,243,249)
    private val muted = Color.rgb(113,128,151)
    private val red = Color.rgb(255,82,102)
    private val sections = listOf("Home","Market Analysis","Chart Terminal","CEX","History","Profile")
    private var active = 0
    private var btc=0.0; private var eth=0.0; private var sol=0.0

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.statusBarColor=bg; window.navigationBarColor=bg; shell(); show(0); prices() }
    override fun onDestroy(){scope.cancel();super.onDestroy()}

    private fun shell(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(bg);setPadding(12,6,12,0)}
        val scroll=ScrollView(this).apply{isFillViewport=true;layoutParams=LinearLayout.LayoutParams(-1,0,1f)}
        content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(0,0,0,18)};scroll.addView(content);root.addView(scroll)
        nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setBackgroundColor(bg);setPadding(0,3,0,4)}
        root.addView(nav,LinearLayout.LayoutParams(-1,66));setContentView(root);drawNav()
    }
    private fun drawNav(){nav.removeAllViews();sections.forEachIndexed{i,s->val v=TextView(this).apply{text=when(s){"Market Analysis"->"MARKET";"Chart Terminal"->"CHART";else->s.uppercase()};textSize=8.5f;gravity=Gravity.CENTER;setTextColor(if(i==active)cyan else muted);setOnClickListener{tap(this);show(i)}};nav.addView(v,LinearLayout.LayoutParams(0,-1,1f))}}
    private fun show(i:Int){active=i;content.removeAllViews();when(i){0->home();1->{title("Market Analysis");market()};2->{title("Chart Terminal");chart()};3->{title("CEX");cex()};4->{title("History");history()};5->{title("Profile");profile()}};drawNav();content.alpha=0f;content.translationY=12f;content.animate().alpha(1f).translationY(0f).setDuration(220).start()}
    private fun tap(v:View){v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);v.animate().scaleX(.94f).scaleY(.94f).setDuration(50).withEndAction{v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()}.start()}
    private fun label(s:String,c:Int,size:Float,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size;setTextColor(c);if(bold)setTypeface(typeface,1);letterSpacing=if(size<=10f).16f else 0f}
    private fun label(s:String,c:Int,size:Int,bold:Boolean=false)=label(s,c,size.toFloat(),bold)
    private fun title(s:String){val x=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(4,12,4,14)};x.addView(label("KITSETUPS",cyan,10,true));x.addView(label(s,text,27,true).apply{setPadding(0,6,0,0)});content.addView(x)}
    private fun card():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(panel);background=android.graphics.drawable.GradientDrawable().apply{setColor(panel);cornerRadius=16f;setStroke(1,Color.rgb(23,36,43))};setPadding(14,14,14,14)}
    private fun addCard(tag:String,head:String,body:String){val p=card();p.addView(label(tag,cyan,9,true));p.addView(label(head,text,17,true).apply{setPadding(0,6,0,0)});p.addView(label(body,muted,12.5f).apply{setPadding(0,6,0,0);setLineSpacing(3f,1f)});content.addView(p,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=10})}
    private fun section(s:String){content.addView(label(s,muted,9,true).apply{setPadding(4,3,4,7)})}

    private fun home(){
        val t=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(4,16,4,10)};t.addView(label("KITSETUPS",cyan,11,true));t.addView(label("Trade the move.\nSee the market.",text,33,true).apply{setPadding(0,8,0,0)});t.addView(label("Native trading cockpit for analysis, execution and review.",muted,13,true).apply{setPadding(0,7,0,0)});content.addView(t)
        val h=card();val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};r.addView(label("MARKET PULSE",muted,9,true),LinearLayout.LayoutParams(0,-2,1f));r.addView(label("● LIVE",cyan,9,true));h.addView(r);h.addView(label("BTC  ${money(btc)}",text,28,true).apply{setPadding(0,12,0,0)});h.addView(label("Live Binance market snapshot",muted,11));h.addView(SparklineView(this),LinearLayout.LayoutParams(-1,66).apply{topMargin=8});content.addView(h,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=14})
        section("MARKETS");val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};listOf("BTC" to btc,"ETH" to eth,"SOL" to sol).forEachIndexed{i,pair->{val c=card();c.setPadding(11,11,11,11);c.addView(label(pair.first,muted,9,true));c.addView(label(money(pair.second),text,14,true).apply{setPadding(0,4,0,0)});row.addView(c,LinearLayout.LayoutParams(0,-2,1f).apply{if(i<2)rightMargin=6})}};content.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=14})
        section("COMMAND CENTER");val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};action(box,"MARKET","Scan setups",1);action(box,"CHART","Open terminal",2);action(box,"CEX","Trade & connect",3);action(box,"HISTORY","Review flow",4);content.addView(box);section("PORTFOLIO");addCard("READY","Portfolio cockpit","Balances, positions and P&L live in the connected CEX workspace.")
    }
    private fun action(box:LinearLayout,a:String,b:String,target:Int){val v=card();v.setPadding(14,13,14,13);v.setOnClickListener{tap(v);show(target)};v.addView(label(a,cyan,9,true));v.addView(label(b,text,13,true).apply{setPadding(0,5,0,0)});box.addView(v,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=7})}
    private fun market(){addCard("LIVE","Market overview","BTC ${money(btc)}\nETH ${money(eth)}\nSOL ${money(sol)}");addCard("ANALYSIS","Momentum radar","Trend structure • Volatility • Liquidity");addCard("SETUPS","Setup scanner","Breakout • Reclaim • Sweep • Continuation")}
    private fun chart(){val w=WebView(this);w.settings.javaScriptEnabled=true;w.settings.domStorageEnabled=true;w.webChromeClient=WebChromeClient();w.setBackgroundColor(bg);w.loadUrl("https://www.tradingview.com/widgetembed/?symbol=BINANCE%3ABTCUSDT&interval=60&theme=dark&style=1&locale=en&hide_top_toolbar=0&hide_legend=0&save_image=0&withdateranges=1&hide_side_toolbar=0");content.addView(w,LinearLayout.LayoutParams(-1,500).apply{bottomMargin=10});addCard("CHART","Terminal","BTC/USDT • 1H\nTradingView chart surface inside the native Chart Terminal.")}
    private fun history(){addCard("HISTORY","Activity","Completed orders, positions and execution events from the connected exchange.");addCard("STATS","Performance","P&L • Fees • Volume • Win rate")}
    private fun profile(){addCard("ACCOUNT","Account","Native Android session and account state.");addCard("SECURITY","Security","API credentials are runtime inputs only. Use trading/read permissions and never withdrawal-enabled keys.");addCard("APP","About KitSetups","Independent native Android trading terminal\nVersion 1.0.0")}
    private fun prices(){scope.launch{val x=withContext(Dispatchers.IO){runCatching{PriceService.snapshot()}.getOrNull()};if(x!=null){btc=x["BTC"]?:0.0;eth=x["ETH"]?:0.0;sol=x["SOL"]?:0.0;if(active==0)show(0)}}}
    private fun money(v:Double)=if(v>0)String.format(Locale.US,"$%,.2f",v) else "—"

    private fun cex(){content.addView(CexTerminal(this),LinearLayout.LayoutParams(-1,-2))}
    private inner class CexTerminal(ctx:Activity):LinearLayout(ctx){
        private var exchange="mexc";private var symbol="BTCUSDT";private var interval="5m";private var connected=false;private var m:CexApi.Market?=null;private var chart:CexChartView?=null;private val tabs=listOf("Positions","Open Orders","Order History")
        init{orientation=VERTICAL;setBackgroundColor(Color.rgb(7,10,15));background=android.graphics.drawable.GradientDrawable().apply{setColor(Color.rgb(7,10,15));cornerRadius=12f;setStroke(1,Color.rgb(23,29,39))};render()}
        private fun field(v:String,secret:Boolean=false)=EditText(ctx).apply{setText(v);setTextColor(text);setHintTextColor(muted);textSize=11f;isSingleLine=true;if(secret)inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;setPadding(9,0,9,0);background=android.graphics.drawable.GradientDrawable().apply{setColor(Color.rgb(12,17,24));cornerRadius=7f;setStroke(1,Color.rgb(32,42,56))}}
        private fun button(s:String,on:Boolean=false)=Button(ctx).apply{text=s;textSize=10f;isAllCaps=false;setTextColor(if(on)Color.rgb(6,16,18) else Color.rgb(170,182,199));background=android.graphics.drawable.GradientDrawable().apply{setColor(if(on)cyan else Color.rgb(17,23,33));cornerRadius=7f;setStroke(1,if(on)cyan else Color.rgb(38,49,65))}}
        private fun render(){removeAllViews();
            val head=LinearLayout(ctx).apply{orientation=HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(9,8,9,8)};head.addView(label("Futures",text,12,true));val ex=Spinner(ctx);ex.adapter=ArrayAdapter(ctx,android.R.layout.simple_spinner_dropdown_item,arrayOf("MEXC","Bitget"));ex.setSelection(if(exchange=="mexc")0 else 1);ex.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){};override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){val n=if(pos==0)"mexc" else "bitget";if(n!=exchange){exchange=n;load()}}};head.addView(ex,LinearLayout.LayoutParams(92,36).apply{leftMargin=7});val pair=field(symbol);pair.setOnEditorActionListener{_,_,_->symbol=pair.text.toString().uppercase().replace("/","");load();true};head.addView(pair,LinearLayout.LayoutParams(145,36).apply{leftMargin=7});head.addView(label(if(connected)"● Live account" else "○ Market data live",if(connected)cyan else muted,9,true),LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=8});val c=button(if(connected)"Account connected" else "Connect exchange",true);c.setOnClickListener{tap(c);connectDialog()};head.addView(c,LinearLayout.LayoutParams(125,36));addView(head)
            val metrics=LinearLayout(ctx).apply{orientation=HORIZONTAL;setPadding(9,7,9,7);setBackgroundColor(Color.rgb(10,14,20))};listOf("LAST","24H","FAIR","INDEX","FUNDING","VOLUME").forEach{metrics.addView(metric(it,"—"),LinearLayout.LayoutParams(0,-2,1f))};addView(metrics)
            val chartBox=LinearLayout(ctx).apply{orientation=VERTICAL;setPadding(9,9,9,9)};chartBox.addView(label("$symbol Perpetual",text,11,true));val tf=LinearLayout(ctx).apply{orientation=HORIZONTAL;setPadding(0,6,0,6)};arrayOf("1m","5m","15m","30m","1h","4h","1d").forEach{v->val b=button(v,v==interval);b.setOnClickListener{interval=v;load()};tf.addView(b,LinearLayout.LayoutParams(0,34,1f).apply{rightMargin=3})};chartBox.addView(tf);chart=CexChartView(ctx);chartBox.addView(chart,LinearLayout.LayoutParams(-1,320));addView(chartBox)
            val book=card();book.setPadding(9,9,9,9);book.addView(label("Order Book",text,11,true));m?.asks?.take(10)?.reversed()?.forEach{book.addView(label(String.format(Locale.US,"%.2f    %.4f",it[0],it[1]),red,9f))};book.addView(label(m?.last?.let{String.format(Locale.US,"%.2f",it)}?:"—",text,11,true).apply{setPadding(0,7,0,7)});m?.bids?.take(10)?.forEach{book.addView(label(String.format(Locale.US,"%.2f    %.4f",it[0],it[1]),cyan,9f))};addView(book,LinearLayout.LayoutParams(-1,-2).apply{setMargins(9,0,9,8)})
            orderPanel();val bottom=LinearLayout(ctx).apply{orientation=HORIZONTAL;setPadding(9,8,9,8)};tabs.forEach{t->val b=button(t,t=="Positions");bottom.addView(b,LinearLayout.LayoutParams(0,36,1f).apply{rightMargin=4})};addView(bottom);val note=if(connected)"Connected account data will populate here." else "Connect an exchange to load account positions, open orders and history.";addView(label(note,muted,10).apply{setPadding(9,7,9,12)});load()
        }
        private fun metric(a:String,b:String)=LinearLayout(ctx).apply{orientation=VERTICAL;addView(label(a,muted,8));addView(label(b,text,10,true).apply{setPadding(0,3,0,0)})}
        private fun orderPanel(){val p=card();p.setPadding(9,9,9,9);p.addView(label("Order",text,11,true));val sides=LinearLayout(ctx).apply{orientation=HORIZONTAL};val l=button("Open Long",true);val s=button("Open Short");sides.addView(l,LinearLayout.LayoutParams(0,38,1f).apply{rightMargin=4});sides.addView(s,LinearLayout.LayoutParams(0,38,1f));p.addView(sides);val row=LinearLayout(ctx).apply{orientation=HORIZONTAL};listOf("Market","Limit").forEachIndexed{i,v->{val b=button(v,i==0);row.addView(b,LinearLayout.LayoutParams(0,36,1f).apply{rightMargin=4})}};p.addView(row);val price=field("Price");price.hint="Price";val size=field("Size");val lev=field("Leverage");val tp=field("TP");val sl=field("SL");listOf(price,size,lev,tp,sl).forEach{p.addView(it,LinearLayout.LayoutParams(-1,38).apply{topMargin=6})};val exec=button("Connect exchange to execute",true);exec.setOnClickListener{if(!connected)connectDialog()};p.addView(exec,LinearLayout.LayoutParams(-1,40).apply{topMargin=8});addView(p,LinearLayout.LayoutParams(-1,-2).apply{setMargins(9,0,9,8)})}
        private fun connectDialog(){val box=LinearLayout(ctx).apply{orientation=VERTICAL;setPadding(22,8,22,0)};val key=field("API key");val secret=field("API secret",true);val pass=field("Bitget passphrase",true);box.addView(key);box.addView(secret,LinearLayout.LayoutParams(-1,42).apply{topMargin=8});box.addView(pass,LinearLayout.LayoutParams(-1,42).apply{topMargin=8});AlertDialog.Builder(ctx).setTitle("Connect $exchange").setMessage("Use read/trade permissions only. Withdrawal permission is not needed.").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Connect"){_,_->test(key.text.toString(),secret.text.toString(),pass.text.toString())}.show()}
        private fun test(key:String,secret:String,pass:String){Toast.makeText(ctx,"Checking exchange…",Toast.LENGTH_SHORT).show();scope.launch{val r=withContext(Dispatchers.IO){runCatching{CexApi.account(exchange,key,secret,pass)}};r.onSuccess{connected=true;Toast.makeText(ctx,"Exchange connected",Toast.LENGTH_SHORT).show();render()}.onFailure{Toast.makeText(ctx,it.message?:"Connection failed",Toast.LENGTH_LONG).show()}}}
        private fun load(){scope.launch{val r=withContext(Dispatchers.IO){runCatching{CexApi.market(exchange,symbol,interval)}};r.onSuccess{m=it;chart?.candles=it.candles;chart?.invalidate();renderMetrics(it)}.onFailure{Toast.makeText(ctx,"Market data unavailable",Toast.LENGTH_SHORT).show()}}}
        private fun renderMetrics(x:CexApi.Market){if(childCount<2)return;val bar=getChildAt(1) as? LinearLayout?:return;val vals=listOf(String.format(Locale.US,"%.2f",x.last),String.format(Locale.US,"%.2f%%",x.change),String.format(Locale.US,"%.2f",x.fair),String.format(Locale.US,"%.2f",x.index),String.format(Locale.US,"%.4f%%",x.funding),String.format(Locale.US,"%.2f",x.volume));for(i in 0 until minOf(6,bar.childCount))(bar.getChildAt(i).getChildAt(1) as TextView).text=vals[i]}
    }
}

private class CexChartView(context:android.content.Context):View(context){var candles:List<CexApi.Candle> = emptyList();private val p=Paint(1);override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(8,11,16));if(candles.isEmpty())return;val xs=candles.takeLast(90);val min=xs.minOf{it.low};val max=xs.maxOf{it.high};val range=(max-min).coerceAtLeast(.000001);val path=Path();xs.forEachIndexed{i,x->val xx=i*width.toFloat()/maxOf(1,xs.size-1);val yy=(height-(x.close-min)/range*height).toFloat();if(i==0)path.moveTo(xx,yy)else path.lineTo(xx,yy)};p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.rgb(37,214,208);c.drawPath(path,p)}}

private class SparklineView(context:android.content.Context):View(context){private val p=Paint(1);override fun onDraw(c:Canvas){super.onDraw(c);p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.rgb(37,214,208);val path=Path();for(i in 0..40){val x=i*width/40f;val y=(height*.55f+(kotlin.math.sin(i*.42)*height*.2f)+(i%7)*1.4f).toFloat();if(i==0)path.moveTo(x,y)else path.lineTo(x,y)};c.drawPath(path,p)}}