package com.kitagent.android

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CexApi {
    data class Market(val last: Double, val change: Double, val fair: Double, val index: Double, val funding: Double, val volume: Double, val asks: List<List<Double>>, val bids: List<List<Double>>, val candles: List<Candle>)
    data class Candle(val time: Long, val open: Double, val high: Double, val low: Double, val close: Double)
    data class AccountSnapshot(val balances: Map<String, Double>)

    fun market(exchange: String, symbol: String, interval: String): Market {
        return if (exchange == "bitget") bitgetMarket(symbol, interval) else mexcMarket(symbol, interval)
    }

    fun account(exchange: String, key: String, secret: String, passphrase: String = ""): AccountSnapshot {
        require(key.isNotBlank() && secret.isNotBlank())
        return if (exchange == "bitget") bitgetAccount(key, secret, passphrase) else mexcAccount(key, secret)
    }

    private fun mexcMarket(symbol: String, interval: String): Market {
        val s = symbol.replace("/", "").uppercase().replace("USDT", "_USDT")
        val ticker = getJson("https://api.mexc.com/api/v1/contract/ticker?symbol=$s")
        val book = getJson("https://api.mexc.com/api/v1/contract/depth/$s?limit=30")
        val end = System.currentTimeMillis() / 1000
        val start = end - 7 * 86400
        val iv = mapOf("1m" to "Min1", "5m" to "Min5", "15m" to "Min15", "30m" to "Min30", "1h" to "Min60", "4h" to "Hour4", "1d" to "Day1")[interval] ?: "Min5"
        val candles = getJson("https://api.mexc.com/api/v1/contract/kline/$s?interval=$iv&start=$start&end=$end")
        val asks = levels(book.optJSONArray("asks")); val bids = levels(book.optJSONArray("bids"))
        return Market(ticker.optDouble("lastPrice"), ticker.optDouble("riseFallRate") * 100, ticker.optDouble("fairPrice"), ticker.optDouble("indexPrice"), ticker.optDouble("fundingRate") * 100, ticker.optDouble("volume24"), asks, bids, mexcCandles(candles))
    }

    private fun bitgetMarket(symbol: String, interval: String): Market {
        val s = symbol.replace("/", "").uppercase()
        val ticker = getJson("https://api.bitget.com/api/v3/market/tickers?category=USDT-FUTURES&symbol=$s").optJSONArray("data")?.optJSONObject(0) ?: JSONObject()
        val book = getJson("https://api.bitget.com/api/v3/market/orderbook?category=USDT-FUTURES&symbol=$s&limit=30").optJSONObject("data") ?: JSONObject()
        val iv = mapOf("1m" to "1m", "5m" to "5m", "15m" to "15m", "30m" to "30m", "1h" to "1H", "4h" to "4H", "1d" to "1D")[interval] ?: "5m"
        val candles = getJson("https://api.bitget.com/api/v3/market/candles?category=USDT-FUTURES&symbol=$s&interval=$iv&limit=200")
        val asks = levels(book.optJSONArray("asks")); val bids = levels(book.optJSONArray("bids"))
        val last = ticker.optString("lastPr").toDoubleOrNull() ?: 0.0
        val change = ticker.optString("change24h").toDoubleOrNull()?.times(100) ?: 0.0
        return Market(last, change, ticker.optString("markPrice").toDoubleOrNull() ?: 0.0, ticker.optString("indexPrice").toDoubleOrNull() ?: 0.0, ticker.optString("fundingRate").toDoubleOrNull()?.times(100) ?: 0.0, ticker.optString("baseVolume").toDoubleOrNull() ?: 0.0, asks, bids, bitgetCandles(candles))
    }

    private fun mexcAccount(key: String, secret: String): AccountSnapshot {
        val signed = mexcSigned("/api/v1/private/account/assets", emptyMap(), key, secret)
        val json = getJson(signed.first, signed.second)
        val data = json.optJSONArray("data") ?: JSONArray()
        val balances = mutableMapOf<String, Double>()
        for (i in 0 until data.length()) {
            val x = data.optJSONObject(i) ?: continue
            val coin = x.optString("currency")
            val balance = x.optString("availableBalance").toDoubleOrNull() ?: x.optString("equity").toDoubleOrNull() ?: 0.0
            if (coin.isNotBlank() && balance != 0.0) balances[coin] = balance
        }
        return AccountSnapshot(balances)
    }

    private fun bitgetAccount(key: String, secret: String, passphrase: String): AccountSnapshot {
        require(passphrase.isNotBlank())
        val path = "/api/v3/account/assets"; val ts = System.currentTimeMillis().toString(); val sign = b64Hmac(secret, ts + "GET" + path)
        val json = getJson("https://api.bitget.com$path", mapOf("ACCESS-KEY" to key, "ACCESS-SIGN" to sign, "ACCESS-TIMESTAMP" to ts, "ACCESS-PASSPHRASE" to passphrase, "locale" to "en-US"))
        val data = json.optJSONArray("data") ?: JSONArray(); val balances = mutableMapOf<String, Double>()
        for (i in 0 until data.length()) { val x = data.optJSONObject(i) ?: continue; val coin = x.optString("coin"); val v = x.optString("available").toDoubleOrNull() ?: x.optString("availableBalance").toDoubleOrNull() ?: 0.0; if (coin.isNotBlank() && v != 0.0) balances[coin] = v }
        return AccountSnapshot(balances)
    }

    private fun mexcSigned(path: String, params: Map<String, String>, key: String, secret: String): Pair<String, Map<String, String>> {
        val q = buildString { params.forEach { if (isNotEmpty()) append('&'); append(it.key).append('=').append(it.value) }; if (isNotEmpty()) append('&'); append("timestamp=").append(System.currentTimeMillis()) }
        val sig = hmacHex(secret, q); return "https://api.mexc.com$path?$q&signature=$sig" to mapOf("X-MEXC-APIKEY" to key)
    }

    private fun getJson(url: String, headers: Map<String, String> = emptyMap()): JSONObject {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = "GET"; c.connectTimeout = 7000; c.readTimeout = 7000; headers.forEach { c.setRequestProperty(it.key, it.value) }
        try { val code = c.responseCode; val stream = if (code in 200..299) c.inputStream else c.errorStream; val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty(); if (code !in 200..299) error("Exchange API error ($code)"); return JSONObject(body) } finally { c.disconnect() }
    }

    private fun levels(a: JSONArray?): List<List<Double>> = buildList { if (a != null) for (i in 0 until a.length()) { val x = a.opt(i); if (x is JSONArray && x.length() >= 2) add(listOf(x.optDouble(0), x.optDouble(1))) } }
    private fun mexcCandles(j: JSONObject): List<Candle> { val t=j.optJSONArray("time") ?: return emptyList(); val o=j.optJSONArray("open"); val h=j.optJSONArray("high"); val l=j.optJSONArray("low"); val c=j.optJSONArray("close"); return buildList { for(i in 0 until t.length()) add(Candle(t.optLong(i)*1000,o?.optDouble(i)?:0.0,h?.optDouble(i)?:0.0,l?.optDouble(i)?:0.0,c?.optDouble(i)?:0.0)) } }
    private fun bitgetCandles(j: JSONObject): List<Candle> { val d=j.optJSONArray("data") ?: return emptyList(); return buildList { for(i in 0 until d.length()) { val x=d.optJSONArray(i) ?: continue; add(Candle(x.optLong(0),x.optDouble(1),x.optDouble(2),x.optDouble(3),x.optDouble(4))) } } }
    private fun hmacHex(secret: String, value: String) = hmac(secret, value).joinToString("") { "%02x".format(it) }
    private fun b64Hmac(secret: String, value: String) = Base64.encodeToString(hmac(secret, value), Base64.NO_WRAP)
    private fun hmac(secret: String, value: String): ByteArray { val mac=Mac.getInstance("HmacSHA256"); mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8),"HmacSHA256")); return mac.doFinal(value.toByteArray(Charsets.UTF_8)) }
}
