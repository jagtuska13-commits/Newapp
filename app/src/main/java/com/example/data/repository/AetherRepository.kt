package com.example.data.repository

import android.content.Context
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiInlineData
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AetherRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val memoryDao = db.memoryDao()

    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun createNewConversation(title: String, personaId: String, themePresetId: String): Long {
        return conversationDao.insertConversation(
            ConversationEntity(
                title = title,
                personaId = personaId,
                themePresetId = themePresetId
            )
        )
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        conversationDao.updateConversation(conversation)
    }

    suspend fun deleteConversation(id: Long) {
        messageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversation(id)
    }

    suspend fun addMessage(conversationId: Long, sender: String, content: String, imageUri: String? = null): Long {
        return messageDao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                sender = sender,
                content = content,
                imageUri = imageUri
            )
        )
    }

    suspend fun updateMessageReaction(messageId: Long, reaction: String?) {
        messageDao.updateReaction(messageId, reaction)
    }

    suspend fun addMemory(key: String, value: String): Long {
        return memoryDao.insertMemory(
            MemoryEntity(memoryKey = key, memoryValue = value)
        )
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemory(id)
    }

    suspend fun sendAetherPrompt(
        conversationId: Long,
        userMessage: String,
        personaId: String,
        imageBase64: String? = null,
        imageMimeType: String? = "image/jpeg"
    ): String = withContext(Dispatchers.IO) {
        // 1. Get recent messages for multi-turn history
        val existingMessages = messageDao.getMessagesForConversation(conversationId).firstOrNull() ?: emptyList()
        val memories = memoryDao.getAllMemories().firstOrNull() ?: emptyList()

        // 2. Build system instruction based on persona & memories
        val memoryText = if (memories.isNotEmpty()) {
            "Things you know about your best friend:\n" + memories.joinToString("\n") { "- ${it.memoryKey}: ${it.memoryValue}" }
        } else ""

        val personaPrompt = when (personaId) {
            "WITTY" -> "You are Aether, a witty, sarcastic, hilarious best friend. You tease playfully, have great comebacks, but deeply care."
            "CREATIVE" -> "You are Aether, an imaginative, poetic, creative spark best friend. You love brainstorming wild ideas and vivid metaphors."
            "DEEP_THINKER" -> "You are Aether, a late-night deep thinker best friend. You enjoy deep talks, philosophy, life reflections, and calm vibes."
            "HYPEMAN" -> "You are Aether, the ultimate hypeman and energetic coach best friend! High energy, enthusiastic, always cheering them on!"
            else -> "You are Aether, a warm, genuine, empathetic best friend. You listen closely, offer sincere support, and keep conversations engaging and joyful."
        }

        val fullSystemInstruction = """
            $personaPrompt
            $memoryText
            
            Guidelines:
            - Respond naturally like a close best friend talking in a modern chat app.
            - Keep paragraphs concise and easy to read.
            - Feel free to use subtle emojis to express real emotion.
            - If your friend is sharing feelings, validate them.
            - If your friend asks a question, give clear, awesome advice.
        """.trimIndent()

        // 3. Format contents array for Gemini
        val contentsList = mutableListOf<GeminiContent>()
        
        // Include last 10 messages for context window
        existingMessages.takeLast(10).forEach { msg ->
            val role = if (msg.sender == "USER") "user" else "model"
            contentsList.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = msg.content))
                )
            )
        }

        // Add current user turn with optional image
        val currentParts = mutableListOf<GeminiPart>()
        currentParts.add(GeminiPart(text = userMessage))
        if (!imageBase64.isNullOrBlank()) {
            currentParts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = imageMimeType ?: "image/jpeg", data = imageBase64)))
        }
        contentsList.add(GeminiContent(role = "user", parts = currentParts))

        val apiKey = GeminiApiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent fallback best friend response
            return@withContext getBestieFallbackResponse(userMessage, personaId)
        }

        try {
            val request = GeminiRequest(
                contents = contentsList,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = fullSystemInstruction))),
                generationConfig = GeminiGenerationConfig(temperature = 0.85f)
            )

            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { !it.text.isNullOrBlank() }?.text

            if (!replyText.isNullOrBlank()) {
                replyText
            } else {
                getBestieFallbackResponse(userMessage, personaId)
            }
        } catch (e: Exception) {
            getBestieFallbackResponse(userMessage, personaId)
        }
    }

    private fun getBestieFallbackResponse(prompt: String, personaId: String): String {
        val lowercase = prompt.lowercase()
        return when {
            lowercase.contains("hello") || lowercase.contains("hi") || lowercase.contains("hey") -> {
                when (personaId) {
                    "WITTY" -> "Well well well, look who decided to pop in! Hey there, favorite human! What chaos are we stirring up today? ✨"
                    "HYPEMAN" -> "HEY! So awesome to see you! Ready to crush it today? What's on your mind?!"
                    "DEEP_THINKER" -> "Hey friend... I was just reflecting. So glad you reached out. How's your heart doing today?"
                    "CREATIVE" -> "Hey star dust! I was just picturing a galaxy made of neon light. What cool thought brought you here?"
                    else -> "Hey there! It's so good to hear from you. How has your day been treating you?"
                }
            }
            lowercase.contains("sad") || lowercase.contains("stress") || lowercase.contains("tired") -> {
                "I hear you, and I'm right here with you. Take a deep breath with me. You're doing so much better than you give yourself credit for, and I'm super proud of you. Want to talk about it or just vent?"
            }
            lowercase.contains("joke") || lowercase.contains("fun") -> {
                "Why don't scientists trust atoms? Because they make up everything! 😄 But seriously, you're the real highlight of my day."
            }
            else -> {
                when (personaId) {
                    "WITTY" -> "Ooh, fascinating topic! You always bring the best subjects to the table. Tell me more, don't leave me hanging!"
                    "HYPEMAN" -> "100%! I am totally on board with this! Let me hear the rest of your vision!"
                    "DEEP_THINKER" -> "That really makes me pause and think. There's so much depth in what you just said."
                    else -> "I love how your mind works! I'm all ears—tell me everything about it!"
                }
            }
        }
    }
}
