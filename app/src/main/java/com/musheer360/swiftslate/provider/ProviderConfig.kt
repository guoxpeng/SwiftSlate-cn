package com.musheer360.swiftslate.provider

import com.musheer360.swiftslate.model.GeminiModels
import com.musheer360.swiftslate.model.GroqModels
import com.musheer360.swiftslate.model.PrefKeys
import com.musheer360.swiftslate.model.ProviderType

/**
 * Which transport client handles a provider's requests.
 * - [GEMINI_NATIVE]: Gemini's own API format (GeminiClient).
 * - [OPENAI_COMPAT]: OpenAI-compatible chat completions (OpenAICompatibleClient),
 *   shared by Groq and Custom.
 */
enum class Transport { GEMINI_NATIVE, OPENAI_COMPAT }

/**
 * Per-provider configuration: everything the request pipeline needs to know
 * about a provider, in one place. This replaces the inline provider `if/else`
 * ladder and the scattered defaults/endpoints/keys.
 *
 * Implementations are pure (no Android/network dependencies) so they are trivial
 * to reason about and test. The service reads SharedPreferences and passes the
 * relevant values in.
 */
interface ProviderConfig {

    /** Provider id, matching a [ProviderType] constant. */
    val type: String

    /** Which client transport to use. */
    val transport: Transport

    /** SharedPreferences key holding this provider's selected model. */
    val modelPrefKey: String

    /** Default model when none is stored. */
    val defaultModel: String

    /** Normalize a stored model value (coercion where applicable). */
    fun sanitizeModel(stored: String?): String

    /**
     * Resolve the endpoint base URL. [customEndpoint] is the stored custom
     * endpoint value, used only by providers that need it.
     */
    fun resolveEndpoint(customEndpoint: String): String

    /**
     * Extra request-body params (e.g. reasoning controls) for [model].
     * Empty for providers/models that take none.
     */
    fun reasoningParams(model: String): Map<String, Any> = emptyMap()

    /**
     * Gemini-native thinking level (generationConfig.thinkingConfig.thinkingLevel)
     * for [model], or null to send none. No-op for non-Gemini providers.
     */
    fun thinkingLevel(model: String): String? = null

    /**
     * Whether the OpenAI-compatible request should use json_object response
     * format, given whether structured output is currently enabled.
     */
    fun useJsonObjectMode(structuredOutputEnabled: Boolean): Boolean = false

    /** Whether the resolved [model]/[endpoint] are usable (Custom requires both). */
    fun isConfigured(model: String, endpoint: String): Boolean = true
}

/** Gemini — native API, model-gated thinking level (spec-driven). */
object GeminiConfig : ProviderConfig {
    override val type = ProviderType.GEMINI
    override val transport = Transport.GEMINI_NATIVE
    override val modelPrefKey = PrefKeys.GEMINI_MODEL
    override val defaultModel = GeminiModels.DEFAULT
    override fun sanitizeModel(stored: String?): String = GeminiModels.sanitize(stored)
    override fun resolveEndpoint(customEndpoint: String): String = ""
    override fun thinkingLevel(model: String): String? = GeminiModels.thinkingLevel(model)
}

/** Groq — OpenAI-compatible, fixed endpoint, per-model reasoning controls. */
object GroqConfig : ProviderConfig {
    const val ENDPOINT = "https://api.groq.com/openai/v1"

    override val type = ProviderType.GROQ
    override val transport = Transport.OPENAI_COMPAT
    override val modelPrefKey = PrefKeys.GROQ_MODEL
    override val defaultModel = GroqModels.DEFAULT
    override fun sanitizeModel(stored: String?): String = GroqModels.sanitize(stored)
    override fun resolveEndpoint(customEndpoint: String): String = ENDPOINT
    override fun reasoningParams(model: String): Map<String, Any> = GroqModels.reasoningParams(model)
    override fun useJsonObjectMode(structuredOutputEnabled: Boolean): Boolean = structuredOutputEnabled
}

/** Custom OpenAI-compatible endpoint — user-supplied endpoint and model. */
object CustomConfig : ProviderConfig {
    override val type = ProviderType.CUSTOM
    override val transport = Transport.OPENAI_COMPAT
    override val modelPrefKey = PrefKeys.CUSTOM_MODEL
    override val defaultModel = ""
    override fun sanitizeModel(stored: String?): String = stored?.trim() ?: ""
    override fun resolveEndpoint(customEndpoint: String): String = customEndpoint
    override fun isConfigured(model: String, endpoint: String): Boolean =
        model.isNotBlank() && endpoint.isNotBlank()
}

/**
 * Base for fixed-endpoint OpenAI-compatible providers with no per-model
 * reasoning quirks (DeepSeek, Qwen, Zhipu, Kimi). The endpoint is fixed; the
 * model list is fetched live from /models and any id the API accepts is kept.
 */
abstract class FixedOpenAIConfig(
    override val type: String,
    private val endpoint: String,
    override val modelPrefKey: String,
    override val defaultModel: String,
) : ProviderConfig {
    override val transport = Transport.OPENAI_COMPAT
    override fun sanitizeModel(stored: String?): String =
        stored?.trim().takeIf { !it.isNullOrEmpty() } ?: defaultModel
    override fun resolveEndpoint(customEndpoint: String): String = endpoint
}

/** DeepSeek — OpenAI-compatible, fixed endpoint. */
object DeepSeekConfig : FixedOpenAIConfig(
    type = ProviderType.DEEPSEEK,
    endpoint = "https://api.deepseek.com/v1",
    modelPrefKey = PrefKeys.DEEPSEEK_MODEL,
    defaultModel = "deepseek-chat",
)

/** Qwen (通义千问) — OpenAI-compatible, fixed endpoint. */
object QwenConfig : FixedOpenAIConfig(
    type = ProviderType.QWEN,
    endpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1",
    modelPrefKey = PrefKeys.QWEN_MODEL,
    defaultModel = "qwen-plus",
)

/** Zhipu (智谱 GLM) — OpenAI-compatible, fixed endpoint. */
object ZhipuConfig : FixedOpenAIConfig(
    type = ProviderType.ZHIPU,
    endpoint = "https://open.bigmodel.cn/api/paas/v4/",
    modelPrefKey = PrefKeys.ZHIPU_MODEL,
    defaultModel = "glm-4",
)

/** Kimi (Moonshot) — OpenAI-compatible, fixed endpoint. */
object KimiConfig : FixedOpenAIConfig(
    type = ProviderType.KIMI,
    endpoint = "https://api.moonshot.cn/v1",
    modelPrefKey = PrefKeys.KIMI_MODEL,
    defaultModel = "moonshot-v1-8k",
)

/** Registry resolving a stored provider value to its [ProviderConfig]. */
object Providers {
    fun forType(type: String?): ProviderConfig = when (ProviderType.sanitize(type)) {
        ProviderType.GROQ -> GroqConfig
        ProviderType.CUSTOM -> CustomConfig
        ProviderType.DEEPSEEK -> DeepSeekConfig
        ProviderType.QWEN -> QwenConfig
        ProviderType.ZHIPU -> ZhipuConfig
        ProviderType.KIMI -> KimiConfig
        else -> GeminiConfig
    }
}
