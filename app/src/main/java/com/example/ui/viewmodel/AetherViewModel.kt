package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.AetherRepository
import com.example.ui.components.BlobTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class AetherViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = AetherRepository(application)
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val currentMessages: StateFlow<List<MessageEntity>> = _currentMessages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speakingMessageId = MutableStateFlow<Long?>(null)
    val speakingMessageId: StateFlow<Long?> = _speakingMessageId.asStateFlow()

    private val _currentPersona = MutableStateFlow("SUPPORTIVE")
    val currentPersona: StateFlow<String> = _currentPersona.asStateFlow()

    private val _currentTheme = MutableStateFlow(BlobTheme.CYBER_NEON)
    val currentTheme: StateFlow<BlobTheme> = _currentTheme.asStateFlow()

    private val _showCreditScene = MutableStateFlow(true)
    val showCreditScene: StateFlow<Boolean> = _showCreditScene.asStateFlow()

    private val _attachedImageBase64 = MutableStateFlow<String?>(null)
    val attachedImageBase64: StateFlow<String?> = _attachedImageBase64.asStateFlow()

    private val _showMemorySheet = MutableStateFlow(false)
    val showMemorySheet: StateFlow<Boolean> = _showMemorySheet.asStateFlow()

    private val _showPersonaSheet = MutableStateFlow(false)
    val showPersonaSheet: StateFlow<Boolean> = _showPersonaSheet.asStateFlow()

    val icebreakers = listOf(
        "✨ Tell me a cool fun fact",
        "💖 I need a little encouragement",
        "💡 Help me brainstorm something wild",
        "☕ What's on your mind right now?",
        "🚀 Tell me a joke to make me laugh"
    )

    init {
        tts = TextToSpeech(application, this)
        
        viewModelScope.launch {
            conversations.collectLatest { list ->
                if (list.isEmpty()) {
                    val id = repository.createNewConversation(
                        title = "Chat with Bestie Aether",
                        personaId = _currentPersona.value,
                        themePresetId = _currentTheme.value.name
                    )
                    _currentConversationId.value = id
                    // Add initial greeting from Aether
                    repository.addMessage(
                        conversationId = id,
                        sender = "AETHER",
                        content = "Hey there! I'm Aether, your AI best friend! ✨ I'm so excited to chat. What's on your mind today?"
                    )
                } else if (_currentConversationId.value == null) {
                    _currentConversationId.value = list.first().id
                }
            }
        }

        viewModelScope.launch {
            _currentConversationId.collectLatest { id ->
                if (id != null) {
                    repository.getMessagesForConversation(id).collectLatest { msgs ->
                        _currentMessages.value = msgs
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                tts?.setPitch(1.05f)
                tts?.setSpeechRate(1.0f)
            }
        }
    }

    fun dismissCreditScene() {
        _showCreditScene.value = false
    }

    fun replayCreditScene() {
        _showCreditScene.value = true
    }

    fun selectConversation(id: Long) {
        _currentConversationId.value = id
    }

    fun createNewChat() {
        viewModelScope.launch {
            val count = (conversations.value.size + 1)
            val id = repository.createNewConversation(
                title = "Conversation #$count",
                personaId = _currentPersona.value,
                themePresetId = _currentTheme.value.name
            )
            _currentConversationId.value = id
            repository.addMessage(
                conversationId = id,
                sender = "AETHER",
                content = "Fresh start! What are we diving into today? I'm all ears! ✨"
            )
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = conversations.value.firstOrNull { it.id != id }?.id
            }
        }
    }

    fun setPersona(personaId: String) {
        _currentPersona.value = personaId
    }

    fun setTheme(theme: BlobTheme) {
        _currentTheme.value = theme
    }

    fun setAttachedImage(base64: String?) {
        _attachedImageBase64.value = base64
    }

    fun toggleMemorySheet(show: Boolean) {
        _showMemorySheet.value = show
    }

    fun togglePersonaSheet(show: Boolean) {
        _showPersonaSheet.value = show
    }

    fun addMemory(key: String, value: String) {
        viewModelScope.launch {
            if (key.isNotBlank() && value.isNotBlank()) {
                repository.addMemory(key, value)
            }
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun toggleMessageReaction(messageId: Long, reaction: String) {
        viewModelScope.launch {
            val msg = _currentMessages.value.find { it.id == messageId }
            val newReaction = if (msg?.reaction == reaction) null else reaction
            repository.updateMessageReaction(messageId, newReaction)
        }
    }

    fun sendMessage(userText: String) {
        val convId = _currentConversationId.value ?: return
        if (userText.isBlank() && _attachedImageBase64.value == null) return

        val imgBase64 = _attachedImageBase64.value
        _attachedImageBase64.value = null

        viewModelScope.launch {
            // Add user message
            repository.addMessage(
                conversationId = convId,
                sender = "USER",
                content = userText.ifBlank { "Sent an image attachment" },
                imageUri = if (imgBase64 != null) "attached_image" else null
            )

            _isThinking.value = true

            val reply = repository.sendAetherPrompt(
                conversationId = convId,
                userMessage = userText,
                personaId = _currentPersona.value,
                imageBase64 = imgBase64
            )

            _isThinking.value = false

            // Save Aether reply
            repository.addMessage(
                conversationId = convId,
                sender = "AETHER",
                content = reply
            )
        }
    }

    fun speakMessage(messageId: Long, text: String) {
        if (!isTtsReady || tts == null) return

        if (_speakingMessageId.value == messageId && _isSpeaking.value) {
            stopSpeech()
        } else {
            tts?.stop()
            _speakingMessageId.value = messageId
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aether_tts_$messageId")
            
            // Auto stop speaking state after duration estimate
            viewModelScope.launch {
                val durationMs = (text.length * 60L).coerceIn(2000L, 20000L)
                kotlinx.coroutines.delay(durationMs)
                if (_speakingMessageId.value == messageId) {
                    _isSpeaking.value = false
                    _speakingMessageId.value = null
                }
            }
        }
    }

    fun stopSpeech() {
        tts?.stop()
        _isSpeaking.value = false
        _speakingMessageId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
