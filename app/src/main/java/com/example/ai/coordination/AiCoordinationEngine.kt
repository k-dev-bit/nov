package com.example.ai.coordination

import com.example.ai.models.AiProvider
import com.example.ai.models.ChatMessage
import com.example.ai.models.ProviderResult
import com.example.ai.network.AiNetworkClient
import com.example.data.local.PreferencesManager
import com.example.deviceactions.CommandParser
import com.example.deviceactions.DeviceActionExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiCoordinationEngine(
    private val prefs: PreferencesManager,
    private val actionExecutor: DeviceActionExecutor
) {

    suspend fun processUserRequest(
        userPrompt: String,
        history: List<ChatMessage>,
        imageBase64: String? = null
    ): ProviderResult = withContext(Dispatchers.IO) {
        // 1. Check if offline mode
        val isOffline = prefs.isOfflineOnly.value
        val command = CommandParser.parse(userPrompt)

        // 2. Local device actions always run locally (even offline!)
        if (command.isDeviceAction && command.actionType != null) {
            val result = actionExecutor.execute(command.actionType, command.target)
            return@withContext ProviderResult.Success(
                text = "⚡ **Action Executed**: ${result.message}\n" +
                        if (result.success) "Status: Verified local Android execution." else "Status: Could not complete action.",
                provider = AiProvider.AUTO,
                model = "Android Local Action Engine"
            )
        }

        // 3. Image generation shortcut intent
        if (command.isImageRequest) {
            val prompt = command.target ?: userPrompt
            val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8")
            val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=1024&height=1024&model=flux&nologo=true"
            return@withContext ProviderResult.Success(
                text = "🎨 Generated image for: **$prompt**\n\n![Generated Image]($imageUrl)\n\n*(You can also use the **Images** tab for full styling and resolution controls)*",
                provider = AiProvider.POLLINATIONS,
                model = "Flux-Image-Engine"
            )
        }

        // 4. If offline mode is enabled and user asks a non-device question
        if (isOffline) {
            // Check if local server configured
            val localUrl = prefs.localServerUrl.value
            val localModel = prefs.localModel.value
            return@withContext try {
                val response = AiNetworkClient.callOpenAiCompatible(
                    baseUrl = localUrl,
                    apiKey = "",
                    model = localModel,
                    messages = history.map { it.role to it.content } + ("user" to userPrompt),
                    systemPrompt = "You are NOVA, running in local-only offline mode."
                )
                ProviderResult.Success(
                    text = response,
                    provider = AiProvider.LOCAL,
                    model = localModel
                )
            } catch (e: Exception) {
                ProviderResult.Error(
                    message = "NOVA is in **Offline Mode**. No local model reachable at $localUrl (${e.message}). You can still use local phone commands or switch to Online/Hybrid mode in Settings.",
                    provider = AiProvider.LOCAL,
                    canRetry = true
                )
            }
        }

        // 5. Select provider based on user preference or Auto routing
        val configuredProvider = AiProvider.fromId(prefs.selectedProvider.value)
        val messagesPayload = history.takeLast(10).map { it.role to it.content } + ("user" to userPrompt)

        if (configuredProvider != AiProvider.AUTO) {
            // Specific provider chosen by user
            return@withContext executeSpecificProvider(configuredProvider, messagesPayload, userPrompt, imageBase64)
        }

        // AUTO Coordination mode:
        // Priority 1: Google Gemini (if API key available)
        // Priority 2: Groq Cloud (if API key available)
        // Priority 3: Pollinations AI (free, zero API key required!)
        val geminiKey = prefs.getEffectiveGeminiKey()
        val groqKey = prefs.groqApiKey.value

        if (geminiKey.isNotBlank()) {
            try {
                val text = AiNetworkClient.callGemini(
                    apiKey = geminiKey,
                    model = "gemini-3.5-flash",
                    messages = messagesPayload,
                    imageBase64 = imageBase64
                )
                return@withContext ProviderResult.Success(
                    text = text,
                    provider = AiProvider.GEMINI,
                    model = "gemini-3.5-flash"
                )
            } catch (e: Exception) {
                if (!prefs.autoFallbackEnabled.value) {
                    return@withContext ProviderResult.Error(
                        message = "Gemini failed: ${e.message}",
                        provider = AiProvider.GEMINI
                    )
                }
                // Fallback to Groq or Pollinations
            }
        }

        if (groqKey.isNotBlank()) {
            val groqModelName = prefs.groqModel.value
            try {
                val text = AiNetworkClient.callOpenAiCompatible(
                    baseUrl = "https://api.groq.com/openai/v1",
                    apiKey = groqKey,
                    model = groqModelName,
                    messages = messagesPayload
                )
                return@withContext ProviderResult.Success(
                    text = text,
                    provider = AiProvider.GROQ,
                    model = groqModelName,
                    isFallback = geminiKey.isNotBlank(),
                    fallbackReason = "Gemini unavailable, switched to Groq ($groqModelName)"
                )
            } catch (e: Exception) {
                if (!prefs.autoFallbackEnabled.value) {
                    return@withContext ProviderResult.Error(
                        message = "Groq failed: ${e.message}",
                        provider = AiProvider.GROQ
                    )
                }
            }
        }

        // Out-of-the-box fallback to Pollinations AI (Zero Setup, works immediately!)
        try {
            val text = AiNetworkClient.callPollinationsText(
                prompt = userPrompt,
                messages = messagesPayload,
                model = "openai"
            )
            return@withContext ProviderResult.Success(
                text = text,
                provider = AiProvider.POLLINATIONS,
                model = "openai",
                isFallback = geminiKey.isNotBlank() || groqKey.isNotBlank(),
                fallbackReason = if (geminiKey.isNotBlank()) "Primary providers unavailable, auto-routed to Pollinations" else null
            )
        } catch (e: Exception) {
            ProviderResult.Error(
                message = "All providers failed: ${e.message}. Please check your internet connection or configure API keys in Settings.",
                provider = AiProvider.AUTO
            )
        }
    }

    private suspend fun executeSpecificProvider(
        provider: AiProvider,
        messages: List<Pair<String, String>>,
        prompt: String,
        imageBase64: String? = null
    ): ProviderResult {
        return try {
            when (provider) {
                AiProvider.GEMINI -> {
                    val key = prefs.getEffectiveGeminiKey()
                    val text = AiNetworkClient.callGemini(key, "gemini-3.5-flash", messages, imageBase64)
                    ProviderResult.Success(text, AiProvider.GEMINI, "gemini-3.5-flash")
                }
                AiProvider.GROQ -> {
                    val key = prefs.groqApiKey.value
                    val modelName = prefs.groqModel.value
                    val text = AiNetworkClient.callOpenAiCompatible(
                        baseUrl = "https://api.groq.com/openai/v1",
                        apiKey = key,
                        model = modelName,
                        messages = messages
                    )
                    ProviderResult.Success(text, AiProvider.GROQ, modelName)
                }
                AiProvider.POLLINATIONS -> {
                    val text = AiNetworkClient.callPollinationsText(prompt, messages, "openai")
                    ProviderResult.Success(text, AiProvider.POLLINATIONS, "openai")
                }
                AiProvider.CUSTOM -> {
                    val text = AiNetworkClient.callOpenAiCompatible(
                        baseUrl = prefs.customApiUrl.value,
                        apiKey = prefs.customApiKey.value,
                        model = prefs.customModel.value,
                        messages = messages
                    )
                    ProviderResult.Success(text, AiProvider.CUSTOM, prefs.customModel.value)
                }
                AiProvider.LOCAL -> {
                    val text = AiNetworkClient.callOpenAiCompatible(
                        baseUrl = prefs.localServerUrl.value,
                        apiKey = "",
                        model = prefs.localModel.value,
                        messages = messages
                    )
                    ProviderResult.Success(text, AiProvider.LOCAL, prefs.localModel.value)
                }
                else -> ProviderResult.Error("Unknown provider", provider)
            }
        } catch (e: Exception) {
            if (prefs.autoFallbackEnabled.value && provider != AiProvider.POLLINATIONS) {
                // Try Pollinations as safety net
                try {
                    val fallbackText = AiNetworkClient.callPollinationsText(prompt, messages, "openai")
                    ProviderResult.Success(
                        text = fallbackText,
                        provider = AiProvider.POLLINATIONS,
                        model = "openai",
                        isFallback = true,
                        fallbackReason = "${provider.displayName} failed (${e.message}), fell back to Pollinations."
                    )
                } catch (fallbackEx: Exception) {
                    ProviderResult.Error("Error with ${provider.displayName}: ${e.message}", provider)
                }
            } else {
                ProviderResult.Error("Error with ${provider.displayName}: ${e.message}", provider)
            }
        }
    }
}
