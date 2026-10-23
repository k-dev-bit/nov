package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_ai_preferences", Context.MODE_PRIVATE)

    // StateFlows for reactive UI observation
    private val _assistantName = MutableStateFlow(getStoredAssistantName())
    val assistantName: StateFlow<String> = _assistantName.asStateFlow()

    private val _selectedProvider = MutableStateFlow(getStoredProvider())
    val selectedProvider: StateFlow<String> = _selectedProvider.asStateFlow()

    private val _coordinationMode = MutableStateFlow(getStoredCoordinationMode())
    val coordinationMode: StateFlow<String> = _coordinationMode.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(getStoredGeminiKey())
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _groqApiKey = MutableStateFlow(getStoredGroqKey())
    val groqApiKey: StateFlow<String> = _groqApiKey.asStateFlow()

    private val _groqModel = MutableStateFlow(getStoredGroqModel())
    val groqModel: StateFlow<String> = _groqModel.asStateFlow()

    private val _customApiUrl = MutableStateFlow(getStoredCustomUrl())
    val customApiUrl: StateFlow<String> = _customApiUrl.asStateFlow()

    private val _customApiKey = MutableStateFlow(getStoredCustomKey())
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _customModel = MutableStateFlow(getStoredCustomModel())
    val customModel: StateFlow<String> = _customModel.asStateFlow()

    private val _localServerUrl = MutableStateFlow(getStoredLocalServerUrl())
    val localServerUrl: StateFlow<String> = _localServerUrl.asStateFlow()

    private val _localModel = MutableStateFlow(getStoredLocalModel())
    val localModel: StateFlow<String> = _localModel.asStateFlow()

    private val _speechLanguage = MutableStateFlow(getStoredSpeechLanguage())
    val speechLanguage: StateFlow<String> = _speechLanguage.asStateFlow()

    private val _speechSpeed = MutableStateFlow(getStoredSpeechSpeed())
    val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()

    private val _continuousVoice = MutableStateFlow(getStoredContinuousVoice())
    val continuousVoice: StateFlow<Boolean> = _continuousVoice.asStateFlow()

    private val _backgroundVoice = MutableStateFlow(getStoredBackgroundVoice())
    val backgroundVoice: StateFlow<Boolean> = _backgroundVoice.asStateFlow()

    private val _isOfflineOnly = MutableStateFlow(getStoredOfflineOnly())
    val isOfflineOnly: StateFlow<Boolean> = _isOfflineOnly.asStateFlow()

    private val _autoFallbackEnabled = MutableStateFlow(getStoredAutoFallback())
    val autoFallbackEnabled: StateFlow<Boolean> = _autoFallbackEnabled.asStateFlow()

    private fun getStoredAssistantName() = prefs.getString("assistant_name", "NOVA") ?: "NOVA"
    private fun getStoredProvider() = prefs.getString("selected_provider", "AUTO") ?: "AUTO"
    private fun getStoredCoordinationMode() = prefs.getString("coordination_mode", "AUTO") ?: "AUTO"
    
    fun getEffectiveGeminiKey(): String {
        val userKey = prefs.getString("gemini_api_key", "") ?: ""
        if (userKey.isNotBlank()) return userKey
        return try {
            if (BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") BuildConfig.GEMINI_API_KEY else ""
        } catch (e: Throwable) {
            ""
        }
    }

    private fun getStoredGeminiKey() = prefs.getString("gemini_api_key", "") ?: ""
    private fun getStoredGroqKey() = prefs.getString("groq_api_key", "") ?: ""
    private fun getStoredGroqModel() = prefs.getString("groq_model", "openai/gpt-oss-120b") ?: "openai/gpt-oss-120b"
    private fun getStoredCustomUrl() = prefs.getString("custom_api_url", "https://api.openai.com/v1") ?: "https://api.openai.com/v1"
    private fun getStoredCustomKey() = prefs.getString("custom_api_key", "") ?: ""
    private fun getStoredCustomModel() = prefs.getString("custom_model", "gpt-4o-mini") ?: "gpt-4o-mini"
    private fun getStoredLocalServerUrl() = prefs.getString("local_server_url", "http://192.168.1.100:11434") ?: "http://192.168.1.100:11434"
    private fun getStoredLocalModel() = prefs.getString("local_model", "llama3.2") ?: "llama3.2"
    private fun getStoredSpeechLanguage() = prefs.getString("speech_language", "en-US") ?: "en-US"
    private fun getStoredSpeechSpeed() = prefs.getFloat("speech_speed", 1.0f)
    private fun getStoredContinuousVoice() = prefs.getBoolean("continuous_voice", false)
    private fun getStoredBackgroundVoice() = prefs.getBoolean("background_voice", false)
    private fun getStoredOfflineOnly() = prefs.getBoolean("is_offline_only", false)
    private fun getStoredAutoFallback() = prefs.getBoolean("auto_fallback_enabled", true)

    fun setAssistantName(name: String) {
        prefs.edit().putString("assistant_name", name).apply()
        _assistantName.value = name
    }

    fun setSelectedProvider(provider: String) {
        prefs.edit().putString("selected_provider", provider).apply()
        _selectedProvider.value = provider
    }

    fun setCoordinationMode(mode: String) {
        prefs.edit().putString("coordination_mode", mode).apply()
        _coordinationMode.value = mode
    }

    fun setGeminiApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key).apply()
        _geminiApiKey.value = key
    }

    fun setGroqApiKey(key: String) {
        prefs.edit().putString("groq_api_key", key).apply()
        _groqApiKey.value = key
    }

    fun setGroqModel(model: String) {
        prefs.edit().putString("groq_model", model).apply()
        _groqModel.value = model
    }

    fun setCustomApiConfig(url: String, key: String, model: String) {
        prefs.edit()
            .putString("custom_api_url", url)
            .putString("custom_api_key", key)
            .putString("custom_model", model)
            .apply()
        _customApiUrl.value = url
        _customApiKey.value = key
        _customModel.value = model
    }

    fun setLocalServerConfig(url: String, model: String) {
        prefs.edit()
            .putString("local_server_url", url)
            .putString("local_model", model)
            .apply()
        _localServerUrl.value = url
        _localModel.value = model
    }

    fun setSpeechLanguage(lang: String) {
        prefs.edit().putString("speech_language", lang).apply()
        _speechLanguage.value = lang
    }

    fun setSpeechSpeed(speed: Float) {
        prefs.edit().putFloat("speech_speed", speed).apply()
        _speechSpeed.value = speed
    }

    fun setContinuousVoice(enabled: Boolean) {
        prefs.edit().putBoolean("continuous_voice", enabled).apply()
        _continuousVoice.value = enabled
    }

    fun setBackgroundVoice(enabled: Boolean) {
        prefs.edit().putBoolean("background_voice", enabled).apply()
        _backgroundVoice.value = enabled
    }

    fun setOfflineOnly(offline: Boolean) {
        prefs.edit().putBoolean("is_offline_only", offline).apply()
        _isOfflineOnly.value = offline
    }

    fun setAutoFallback(enabled: Boolean) {
        prefs.edit().putBoolean("auto_fallback_enabled", enabled).apply()
        _autoFallbackEnabled.value = enabled
    }

    fun clearAllSecrets() {
        prefs.edit()
            .remove("gemini_api_key")
            .remove("groq_api_key")
            .remove("custom_api_key")
            .apply()
        _geminiApiKey.value = ""
        _groqApiKey.value = ""
        _customApiKey.value = ""
    }
}
