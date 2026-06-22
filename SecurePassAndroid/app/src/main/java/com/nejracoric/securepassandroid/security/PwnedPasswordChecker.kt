package com.nejracoric.securepassandroid.security

import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.Proxy
import java.net.SocketTimeoutException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object PwnedPasswordChecker {

    private const val API_BASE_URL = "https://api.pwnedpasswords.com/range/"
    private const val USER_AGENT = "SecurePassAndroid/1.0"
    private const val MAX_ATTEMPTS = 3

    private val ipv4PreferredDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            val addresses = InetAddress.getAllByName(hostname).toList()
            val ipv4Addresses = addresses.filterIsInstance<Inet4Address>()
            return if (ipv4Addresses.isNotEmpty()) ipv4Addresses else addresses
        }
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .proxy(Proxy.NO_PROXY)
            .dns(ipv4PreferredDns)
            .retryOnConnectionFailure(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val diagnosticClient: OkHttpClient by lazy {
        httpClient.newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Throws(IOException::class)
    fun checkPassword(password: String): Int {
        if (password.isEmpty()) return 0

        val hash = sha1Hex(password)
        val prefix = hash.take(5)
        val suffix = hash.drop(5)

        var lastError: IOException? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                return fetchBreachCount(prefix, suffix)
            } catch (error: IOException) {
                lastError = error
                if (attempt < MAX_ATTEMPTS - 1) {
                    Thread.sleep(1_000L shl attempt)
                }
            }
        }

        throw lastError ?: IOException("HIBP check failed")
    }

    fun init() {
        System.setProperty("java.net.preferIPv4Stack", "true")
        System.setProperty("java.net.preferIPv6Addresses", "false")
    }

    fun canReach(url: String): Boolean {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        return try {
            diagnosticClient.newCall(request).execute().use { response ->
                response.isSuccessful || response.code in 300..499
            }
        } catch (_: IOException) {
            false
        }
    }

    @Throws(IOException::class)
    private fun fetchBreachCount(prefix: String, suffix: String): Int {
        val request = Request.Builder()
            .url("$API_BASE_URL$prefix")
            .header("User-Agent", USER_AGENT)
            .header("Add-Padding", "true")
            .get()
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string().orEmpty()
                    throw IOException("HIBP API HTTP ${response.code}: $errorBody")
                }

                response.body?.byteStream()?.bufferedReader()?.use { reader ->
                    reader.lineSequence().forEach { line ->
                        val parts = line.split(':')
                        if (parts.size == 2 && parts[0].equals(suffix, ignoreCase = true)) {
                            return parts[1].trim().toIntOrNull() ?: 0
                        }
                    }
                }
            }
        } catch (error: SocketTimeoutException) {
            throw IOException("Isteklo je vrijeme čekanja na HIBP API.", error)
        }

        return 0
    }

    private fun sha1Hex(password: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        return digest.digest(password.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte ->
                "%02X".format(byte.toInt() and 0xFF)
            }
    }
}
