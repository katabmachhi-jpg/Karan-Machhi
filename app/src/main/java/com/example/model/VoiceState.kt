package com.example.model

enum class VoiceState(val statusLabel: String) {
    IDLE("Ready"),
    LISTENING("Listening to voice..."),
    PROCESSING("Thinking..."),
    SPEAKING("Speaking..."),
    ERROR("Error occurred")
}
