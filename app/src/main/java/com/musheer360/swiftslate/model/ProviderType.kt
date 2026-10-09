package com.musheer360.swiftslate.model

object ProviderType {
    const val GEMINI = "gemini"
    const val GROQ = "groq"
    const val CUSTOM = "custom"
    const val DEEPSEEK = "deepseek"
    const val QWEN = "qwen"
    const val ZHIPU = "zhipu"
    const val KIMI = "kimi"

    private val VALID = setOf(GEMINI, GROQ, CUSTOM, DEEPSEEK, QWEN, ZHIPU, KIMI)
    fun sanitize(value: String?): String = if (value in VALID) value!! else GEMINI
}
