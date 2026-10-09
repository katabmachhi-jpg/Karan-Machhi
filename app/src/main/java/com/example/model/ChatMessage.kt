package com.example.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val language: LanguageType = LanguageType.ENGLISH,
    val toolResult: ToolActionResult? = null,
    val rawVoiceCommand: String? = null
)
