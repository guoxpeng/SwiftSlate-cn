package com.musheer360.swiftslate.model

/**
 * Single source of truth for the SharedPreferences keys used by the
 * provider/model configuration flow. Centralizing these prevents silent
 * breakage from mistyped string literals scattered across UI, service, and
 * client code.
 *
 * Values are unchanged from the literals previously used inline, so existing
 * stored preferences continue to resolve identically.
 */
object PrefKeys {
    /** Active provider ("gemini" | "groq" | "custom") — see [ProviderType]. */
    const val PROVIDER_TYPE = "provider_type"

    /** Selected Gemini model id. */
    const val GEMINI_MODEL = "model"

    /** Selected Groq model id. */
    const val GROQ_MODEL = "groq_model"

    /** Custom (OpenAI-compatible) model id. */
    const val CUSTOM_MODEL = "custom_model"

    /** Selected DeepSeek model id. */
    const val DEEPSEEK_MODEL = "deepseek_model"

    /** Selected Qwen (通义千问) model id. */
    const val QWEN_MODEL = "qwen_model"

    /** Selected Zhipu (智谱 GLM) model id. */
    const val ZHIPU_MODEL = "zhipu_model"

    /** Selected Kimi (Moonshot) model id. */
    const val KIMI_MODEL = "kimi_model"

    /** Custom (OpenAI-compatible) endpoint base URL. */
    const val CUSTOM_ENDPOINT = "custom_endpoint"

    /** Sampling temperature (Float). */
    const val TEMPERATURE = "temperature"

    /** Epoch millis when structured output was last disabled (0 = never). */
    const val STRUCTURED_OUTPUT_DISABLED_AT = "structured_output_disabled_at"

    /** Proxy enabled (Boolean). When on, ALL API traffic goes through the proxy. */
    const val PROXY_ENABLED = "proxy_enabled"

    /** Proxy type: "http" or "socks" (see [PROXY_TYPE_HTTP]/[PROXY_TYPE_SOCKS]). */
    const val PROXY_TYPE = "proxy_type"
    const val PROXY_TYPE_HTTP = "http"
    const val PROXY_TYPE_SOCKS = "socks"

    /** Proxy host (String), e.g. 127.0.0.1 for a local Clash/V2RayNG. */
    const val PROXY_HOST = "proxy_host"

    /** Proxy port (Int). */
    const val PROXY_PORT = "proxy_port"

    /** Proxy auth username (String, optional). */
    const val PROXY_USERNAME = "proxy_username"

    /** Proxy auth password (String, optional). Stored as-is in prefs. */
    const val PROXY_PASSWORD = "proxy_password"
}
