package com.example.model

data class ToolActionResult(
    val toolName: String,
    val isSuccess: Boolean,
    val details: String,
    val iconKey: String = "tool",
    val timestamp: Long = System.currentTimeMillis()
)
