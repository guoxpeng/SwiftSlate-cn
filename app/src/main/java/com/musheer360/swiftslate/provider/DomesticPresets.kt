package com.musheer360.swiftslate.provider

/**
 * Preset OpenAI-compatible endpoints for Chinese domestic AI providers.
 *
 * One tap fills the Custom provider's endpoint (plus a sensible default model
 * when the model field is still empty), so users don't have to type or look up
 * URLs by hand. All four are OpenAI-compatible chat-completions endpoints, which
 * is exactly what the Custom provider speaks.
 *
 * Kept as data, not string resources: these are brand names and URLs, identical
 * in every locale.
 */
object DomesticPresets {

    data class Preset(
        /** Display label (brand name). */
        val label: String,
        /** Endpoint base URL, filled into the Custom endpoint field. */
        val endpoint: String,
        /** Default model, filled into the Custom model field when it is empty. */
        val defaultModel: String,
    )

    val ALL: List<Preset> = listOf(
        Preset(
            label = "DeepSeek",
            endpoint = "https://api.deepseek.com/v1",
            defaultModel = "deepseek-chat",
        ),
        Preset(
            label = "通义千问",
            endpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            defaultModel = "qwen-plus",
        ),
        Preset(
            label = "智谱 GLM",
            endpoint = "https://open.bigmodel.cn/api/paas/v4/",
            defaultModel = "glm-4",
        ),
        Preset(
            label = "Kimi",
            endpoint = "https://api.moonshot.cn/v1",
            defaultModel = "moonshot-v1-8k",
        ),
    )
}
