package com.example.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.models.AiProvider
import com.example.ai.network.AiNetworkClient
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = PreferencesManager(application)
    private val db = AppDatabase.getInstance(application)

    private val _testStatus = MutableStateFlow<Map<String, Pair<Boolean, String>>>(emptyMap())
    val testStatus: StateFlow<Map<String, Pair<Boolean, String>>> = _testStatus.asStateFlow()

    private val _isTesting = MutableStateFlow<String?>(null)
    val isTesting: StateFlow<String?> = _isTesting.asStateFlow()

    fun testProvider(provider: AiProvider) {
        viewModelScope.launch {
            _isTesting.value = provider.id
            val (key, url, model) = when (provider) {
                AiProvider.GEMINI -> Triple(preferences.getEffectiveGeminiKey(), "", "gemini-3.5-flash")
                AiProvider.GROQ -> Triple(preferences.groqApiKey.value, "https://api.groq.com/openai/v1", preferences.groqModel.value)
                AiProvider.POLLINATIONS -> Triple("", "https://text.pollinations.ai/", "openai")
                AiProvider.CUSTOM -> Triple(preferences.customApiKey.value, preferences.customApiUrl.value, preferences.customModel.value)
                AiProvider.LOCAL -> Triple("", preferences.localServerUrl.value, preferences.localModel.value)
                else -> Triple("", "", "")
            }

            val result = AiNetworkClient.testConnection(provider, key, url, model)
            val currentMap = _testStatus.value.toMutableMap()
            currentMap[provider.id] = result
            _testStatus.value = currentMap
            _isTesting.value = null
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            db.conversationDao().deleteAllConversations()
        }
    }

    fun clearSecrets() {
        preferences.clearAllSecrets()
    }
}
