package com.example.ai.models

enum class AiProvider(
    val id: String,
    val displayName: String,
    val defaultModel: String,
    val requiresApiKey: Boolean,
    val description: String
) {
    AUTO(
        id = "AUTO",
        displayName = "Auto Coordinator",
        defaultModel = "Intelligent Router",
        requiresApiKey = false,
        description = "Smartly detects intent, device actions, images, or chat and picks the optimal provider."
    ),
    POLLINATIONS(
        id = "POLLINATIONS",
        displayName = "Pollinations AI",
        defaultModel = "openai",
        requiresApiKey = false,
        description = "Free community AI models for text and image generation. No API key needed."
    ),
    GEMINI(
        id = "GEMINI",
        displayName = "Google Gemini",
        defaultModel = "gemini-3.5-flash",
        requiresApiKey = true,
        description = "Advanced reasoning, coding, long context, and multimodal vision by Google."
    ),
    GROQ(
        id = "GROQ",
        displayName = "Groq Cloud",
        defaultModel = "openai/gpt-oss-120b",
        requiresApiKey = true,
        description = "Ultra-low latency inference with openai/gpt-oss-120b & Llama models."
    ),
    CUSTOM(
        id = "CUSTOM",
        displayName = "Custom OpenAI API",
        defaultModel = "gpt-4o-mini",
        requiresApiKey = false,
        description = "Any OpenAI-compatible /v1/chat/completions endpoint."
    ),
    LOCAL(
        id = "LOCAL",
        displayName = "Local Inference (Ollama/LM)",
        defaultModel = "llama3.2",
        requiresApiKey = false,
        description = "Run on your PC or local Wi-Fi server (Ollama, LM Studio, llama.cpp)."
    );

    companion object {
        fun fromId(id: String): AiProvider {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: AUTO
        }
    }
}

data class ChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val imageBase64: String? = null
)

sealed class ProviderResult {
    data class Success(
        val text: String,
        val provider: AiProvider,
        val model: String,
        val isFallback: Boolean = false,
        val fallbackReason: String? = null
    ) : ProviderResult()

    data class Error(
        val message: String,
        val provider: AiProvider,
        val canRetry: Boolean = true
    ) : ProviderResult()
}
