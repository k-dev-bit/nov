package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

class VoiceManager(
    private val context: Context,
    private val onSpeakingFinished: () -> Unit = {},
    private val onSpeechRecognized: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        mainHandler.post {
            initSpeechRecognizer()
            textToSpeech = TextToSpeech(context.applicationContext, this)
        }
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceManager)
                }
            } else {
                _errorMessage.value = "Speech recognition service not pre-installed on this device. You can type commands below or grant permissions in Settings."
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to init speech recognizer: ${e.message}"
        }
    }

    fun startListening(languageCode: String = "en-US") {
        stopSpeaking()
        _errorMessage.value = null
        _partialText.value = ""

        mainHandler.post {
            if (speechRecognizer == null) {
                initSpeechRecognizer()
            }
            if (speechRecognizer == null) {
                _errorMessage.value = "Speech recognizer is unavailable. Please check microphone permissions or enter text below."
                _voiceState.value = VoiceState.ERROR
                return@post
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            try {
                speechRecognizer?.startListening(intent)
                _voiceState.value = VoiceState.LISTENING
            } catch (e: Exception) {
                _errorMessage.value = "Could not start mic: ${e.message}"
                _voiceState.value = VoiceState.ERROR
                recreateSpeechRecognizer()
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                // Ignore
            }
            if (_voiceState.value == VoiceState.LISTENING) {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    fun simulateSpeech(text: String) {
        val trimmed = text.trim()
        if (trimmed.isNotBlank()) {
            stopSpeaking()
            stopListening()
            _partialText.value = trimmed
            onSpeechRecognized(trimmed)
        }
    }

    fun speak(text: String, speed: Float = 1.0f, language: String = "en-US") {
        if (!isTtsReady || textToSpeech == null) {
            return
        }
        stopSpeaking()

        val cleanText = text
            .replace(Regex("[#*`_\\[\\]()]"), "")
            .replace(Regex("https?://\\S+"), "link")
            .take(500)

        val locale = if (language.startsWith("hi")) Locale.Builder().setLanguage("hi").setRegion("IN").build() else Locale.US
        textToSpeech?.language = locale
        textToSpeech?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))

        _voiceState.value = VoiceState.SPEAKING
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "NOVA_UTTERANCE")
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        if (_voiceState.value == VoiceState.SPEAKING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun setThinking() {
        _voiceState.value = VoiceState.THINKING
    }

    fun setIdle() {
        _voiceState.value = VoiceState.IDLE
    }

    private fun recreateSpeechRecognizer() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                initSpeechRecognizer()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun destroy() {
        mainHandler.post {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        }
    }

    // TextToSpeech.OnInitListener
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _voiceState.value = VoiceState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    if (_voiceState.value == VoiceState.SPEAKING) {
                        _voiceState.value = VoiceState.IDLE
                        mainHandler.postDelayed({
                            onSpeakingFinished()
                        }, 500)
                    }
                }

                override fun onError(utteranceId: String?) {
                    if (_voiceState.value == VoiceState.SPEAKING) {
                        _voiceState.value = VoiceState.IDLE
                    }
                }
            })
        }
    }

    // RecognitionListener
    override fun onReadyForSpeech(params: Bundle?) {
        _voiceState.value = VoiceState.LISTENING
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        _audioRms.value = rmsdB
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _voiceState.value = VoiceState.THINKING
    }

    override fun onError(error: Int) {
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Tap orb and speak clearly into microphone."
            SpeechRecognizer.ERROR_NETWORK -> "Network issue connecting to speech recognition service."
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue. Please check microphone permissions."
            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client busy. Resetting..."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required! Tap 'Grant Access' below."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy. Resetting..."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out. Tap orb to speak again."
            else -> "Speech recognizer status code: $error"
        }
        _errorMessage.value = errorMsg
        _voiceState.value = VoiceState.IDLE
        recreateSpeechRecognizer()
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val heard = matches?.firstOrNull()?.trim()
        if (!heard.isNullOrBlank()) {
            _partialText.value = heard
            onSpeechRecognized(heard)
        } else {
            _voiceState.value = VoiceState.IDLE
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            _partialText.value = text
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
