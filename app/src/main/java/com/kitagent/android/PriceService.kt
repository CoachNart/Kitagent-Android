package com.kitagent.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object PriceService {
    suspend fun snapshot(): Map<String, Double> = withContext(Dispatchers.IO) {
        val symbols = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT")
        buildMap {
            symbols.forEach { symbol ->
                runCatching {
                    val connection = URL("https://api.binance.com/api/v3/ticker/price?symbol=$symbol").openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.requestMethod = "GET"
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    put(symbol.removeSuffix("USDT"), JSONObject(body).getDouble("price"))
                    connection.disconnect()
                }
            }
        }
    }
}
