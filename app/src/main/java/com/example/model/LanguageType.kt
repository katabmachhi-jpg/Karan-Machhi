package com.example.model

enum class LanguageType(val displayName: String, val localeCode: String, val nativeName: String) {
    HINDI("Hindi", "hi-IN", "हिन्दी"),
    GUJARATI("Gujarati", "gu-IN", "ગુજરાતી"),
    HINGLISH("Hinglish", "hi-IN", "Hinglish"),
    ENGLISH("English", "en-US", "English");

    companion object {
        fun fromDisplayName(name: String): LanguageType {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: ENGLISH
        }
    }
}
