package com.musheer360.swiftslate.api

import android.content.SharedPreferences
import com.musheer360.swiftslate.model.PrefKeys
import java.net.Authenticator
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.URL

/**
 * Global HTTP/SOCKS proxy for all API traffic.
 *
 * Initialized once from the Application class with the "settings"
 * SharedPreferences. Every API client routes its HttpURLConnection through
 * [openConnection], so enabling the proxy in Settings immediately affects key
 * validation, model listing, and generation — for every provider. No per-client
 * or per-provider plumbing needed.
 *
 * Proxy auth is served by a single process-wide Authenticator installed at init.
 * It answers ONLY proxy challenges (never origin-server challenges) and only
 * while the proxy is enabled with a username set, so credentials cannot leak to
 * API endpoints.
 */
object ProxyManager {

    @Volatile
    private var prefs: SharedPreferences? = null

    fun init(prefs: SharedPreferences) {
        this.prefs = prefs
        Authenticator.setDefault(object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication? {
                if (requestorType != RequestorType.PROXY) return null
                val p = this@ProxyManager.prefs ?: return null
                if (!p.getBoolean(PrefKeys.PROXY_ENABLED, false)) return null
                val user = p.getString(PrefKeys.PROXY_USERNAME, "").orEmpty()
                if (user.isBlank()) return null
                val pass = p.getString(PrefKeys.PROXY_PASSWORD, "").orEmpty()
                return PasswordAuthentication(user, pass.toCharArray())
            }
        })
    }

    /** Build the Proxy from current prefs, or null when disabled/misconfigured. */
    fun currentProxy(): Proxy? {
        val p = prefs ?: return null
        if (!p.getBoolean(PrefKeys.PROXY_ENABLED, false)) return null
        val host = p.getString(PrefKeys.PROXY_HOST, "").orEmpty().trim()
        val port = p.getInt(PrefKeys.PROXY_PORT, 0)
        if (host.isEmpty() || port !in 1..65535) return null
        val type = if (p.getString(PrefKeys.PROXY_TYPE, PrefKeys.PROXY_TYPE_HTTP) == PrefKeys.PROXY_TYPE_SOCKS)
            Proxy.Type.SOCKS else Proxy.Type.HTTP
        return Proxy(type, InetSocketAddress(host, port))
    }

    /** True when a usable proxy is configured (for UI hints). */
    fun isActive(): Boolean = currentProxy() != null

    /**
     * Open an HttpURLConnection to [url], via the proxy when one is configured.
     * Drop-in replacement for `URL(url).openConnection()`.
     */
    fun openConnection(url: String): HttpURLConnection {
        val proxy = currentProxy()
        val conn = if (proxy != null) URL(url).openConnection(proxy) else URL(url).openConnection()
        return conn as HttpURLConnection
    }
}
