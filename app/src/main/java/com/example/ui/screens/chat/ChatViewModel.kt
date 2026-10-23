package com.example.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.coordination.AiCoordinationEngine
import com.example.ai.models.AiProvider
import com.example.ai.models.ChatMessage
import com.example.ai.models.ProviderResult
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PreferencesManager
import com.example.deviceactions.DeviceActionExecutor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    val preferences = PreferencesManager(application)
    private val actionExecutor = DeviceActionExecutor(application)
    private val coordinationEngine = AiCoordinationEngine(preferences, actionExecutor)

    val conversations: StateFlow<List<ConversationEntity>> =
        conversationDao.getAllConversations().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var activeGenerationJob: Job? = null
    private var messageObserveJob: Job? = null

    init {
        viewModelScope.launch {
            conversations.collect { convList ->
                if (_currentConversationId.value == null && convList.isNotEmpty()) {
                    selectConversation(convList.first().id)
                } else if (convList.isEmpty()) {
                    startNewConversation()
                }
            }
        }
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        messageObserveJob?.cancel()
        messageObserveJob = viewModelScope.launch {
            messageDao.getMessagesForConversation(id).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            val newConv = ConversationEntity(
                id = newId,
                title = "New Chat",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastProvider = preferences.selectedProvider.value
            )
            conversationDao.insertConversation(newConv)
            selectConversation(newId)
        }
    }

    fun sendMessage(content: String, attachedImageUrl: String? = null) {
        val trimmed = content.trim()
        if (trimmed.isBlank() && attachedImageUrl == null) return

        val convId = _currentConversationId.value ?: return

        viewModelScope.launch {
            val userMsgId = UUID.randomUUID().toString()
            val userMessage = MessageEntity(
                id = userMsgId,
                conversationId = convId,
                role = "user",
                content = trimmed,
                imageUrl = attachedImageUrl,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(userMessage)

            // Update conversation title if first message
            val currentMsgs = _messages.value
            if (currentMsgs.isEmpty() || currentMsgs.size == 1) {
                val autoTitle = if (trimmed.length > 28) trimmed.take(28) + "..." else trimmed
                conversationDao.renameConversation(convId, autoTitle)
            }

            // Create placeholder assistant message
            val assistantMsgId = UUID.randomUUID().toString()
            val placeholder = MessageEntity(
                id = assistantMsgId,
                conversationId = convId,
                role = "assistant",
                content = "NOVA is thinking...",
                timestamp = System.currentTimeMillis() + 1,
                isStreaming = true
            )
            messageDao.insertMessage(placeholder)
            _isGenerating.value = true

            activeGenerationJob = launch {
                try {
                    val history = currentMsgs.map {
                        ChatMessage(role = it.role, content = it.content)
                    }
                    val base64Image = if (!attachedImageUrl.isNullOrBlank()) {
                        try {
                            com.example.util.ImageUtils.uriToBase64(getApplication(), android.net.Uri.parse(attachedImageUrl))
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                    val result = coordinationEngine.processUserRequest(trimmed, history, base64Image)

                    when (result) {
                        is ProviderResult.Success -> {
                            val updatedAssistant = MessageEntity(
                                id = assistantMsgId,
                                conversationId = convId,
                                role = "assistant",
                                content = result.text,
                                provider = result.provider.displayName,
                                model = result.model,
                                timestamp = System.currentTimeMillis() + 1,
                                isStreaming = false
                            )
                            messageDao.updateMessage(updatedAssistant)
                        }
                        is ProviderResult.Error -> {
                            val errAssistant = MessageEntity(
                                id = assistantMsgId,
                                conversationId = convId,
                                role = "assistant",
                                content = "⚠️ **Error**: ${result.message}",
                                provider = result.provider.displayName,
                                timestamp = System.currentTimeMillis() + 1,
                                isStreaming = false
                            )
                            messageDao.updateMessage(errAssistant)
                        }
                    }
                } catch (e: Exception) {
                    val failMsg = MessageEntity(
                        id = assistantMsgId,
                        conversationId = convId,
                        role = "assistant",
                        content = "⚠️ **Failed to complete request**: ${e.message}",
                        timestamp = System.currentTimeMillis() + 1,
                        isStreaming = false
                    )
                    messageDao.updateMessage(failMsg)
                } finally {
                    _isGenerating.value = false
                }
            }
        }
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        _isGenerating.value = false
    }

    fun regenerateResponse() {
        val lastUserMsg = _messages.value.lastOrNull { it.role == "user" } ?: return
        sendMessage(lastUserMsg.content, lastUserMsg.imageUrl)
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            conversationDao.deleteConversationById(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
            }
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            conversationDao.renameConversation(id, newTitle)
        }
    }

    fun togglePin(id: String, currentPinned: Boolean) {
        viewModelScope.launch {
            conversationDao.setPinned(id, !currentPinned)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setQuickProvider(provider: AiProvider) {
        preferences.setSelectedProvider(provider.id)
    }
}
