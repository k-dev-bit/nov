package com.example.ui.screens.voice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.coordination.AiCoordinationEngine
import com.example.ai.models.ChatMessage
import com.example.ai.models.ProviderResult
import com.example.data.local.PreferencesManager
import com.example.deviceactions.CommandParser
import com.example.deviceactions.DeviceActionExecutor
import com.example.voice.NovaVoiceService
import com.example.voice.VoiceManager
import com.example.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = PreferencesManager(application)
    private val actionExecutor = DeviceActionExecutor(application)
    private val coordinationEngine = AiCoordinationEngine(preferences, actionExecutor)

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _assistantReply = MutableStateFlow("")
    val assistantReply: StateFlow<String> = _assistantReply.asStateFlow()

    val voiceManager: VoiceManager = VoiceManager(
        context = application,
        onSpeakingFinished = {
            if (preferences.continuousVoice.value) {
                startListening()
            }
        },
        onSpeechRecognized = { heardText ->
            handleHeardSpeech(heardText)
        }
    )

    val voiceState: StateFlow<VoiceState> = voiceManager.voiceState
    val audioRms: StateFlow<Float> = voiceManager.audioRms
    val partialText: StateFlow<String> = voiceManager.partialText
    val errorMessage: StateFlow<String?> = voiceManager.errorMessage

    fun startListening() {
        val lang = preferences.speechLanguage.value
        voiceManager.startListening(lang)
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun toggleListening() {
        if (voiceState.value == VoiceState.LISTENING) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun toggleContinuousVoice() {
        val newState = !preferences.continuousVoice.value
        preferences.setContinuousVoice(newState)
        if (newState && voiceState.value == VoiceState.IDLE) {
            startListening()
        }
    }

    fun toggleBackgroundVoice() {
        val newState = !preferences.backgroundVoice.value
        preferences.setBackgroundVoice(newState)
        if (newState) {
            NovaVoiceService.startService(getApplication())
        } else {
            NovaVoiceService.stopService(getApplication())
        }
    }

    fun setLanguage(langCode: String) {
        preferences.setSpeechLanguage(langCode)
    }

    fun setSpeechSpeed(speed: Float) {
        preferences.setSpeechSpeed(speed)
    }

    private fun handleHeardSpeech(query: String) {
        _transcript.value = query
        voiceManager.setThinking()

        viewModelScope.launch {
            val command = CommandParser.parse(query)

            if (command.isDeviceAction && command.actionType != null) {
                val actionResult = actionExecutor.execute(command.actionType, command.target)
                val reply = actionResult.message
                _assistantReply.value = reply
                voiceManager.speak(
                    text = actionResult.message,
                    speed = preferences.speechSpeed.value,
                    language = preferences.speechLanguage.value
                )
            } else {
                val result = coordinationEngine.processUserRequest(
                    userPrompt = query,
                    history = listOf(ChatMessage("user", query))
                )

                val reply = when (result) {
                    is ProviderResult.Success -> result.text
                    is ProviderResult.Error -> "I encountered an issue: ${result.message}"
                }
                _assistantReply.value = reply
                voiceManager.speak(
                    text = reply,
                    speed = preferences.speechSpeed.value,
                    language = preferences.speechLanguage.value
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
