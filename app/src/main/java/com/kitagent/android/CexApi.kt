package com.kitagent.android

import android.util.Base64
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

/** Exchange API primitives. Private credentials are supplied at runtime only. */
object CexApi {
    data class AccountSnapshot(val balances: Map<String, Double>)

    fun binanceAccount(apiKey: String, secret: String): AccountSnapshot {
        require(apiKey.isNotBlank() && secret.isNotBlank())
        val timestamp = System.currentTimeMillis()
        val query = "timestamp=$timestamp&recvWindow=5000"
        val signature = hmacSha256(secret, query)
        val url = URL("https://api.binance.com/api/v3/account?$query&signature=$signature")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 7000
            readTimeout = 7000
            setRequestProperty("X-MBX-APIKEY", apiKey)
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("Exchange API error ($code)")
            val json = JSONObject(body)
            val balances = mutableMapOf<String, Double>()
            val items = json.getJSONArray("balances")
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val free = item.optString("free").toDoubleOrNull() ?: 0.0
                val locked = item.optString("locked").toDoubleOrNull() ?: 0.0
                if (free + locked > 0.0) balances[item.getString("asset")] = free + locked
            }
            return AccountSnapshot(balances)
        } finally {
            connection.disconnect()
        }
    }

    private fun hmacSha256(secret: String, value: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
