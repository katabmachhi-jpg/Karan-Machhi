package com.example.ai

import com.example.model.LanguageType

object LanguageDetector {

    private val gujaratiCharRegex = Regex("[\\u0A80-\\u0AFF]")
    private val devanagariCharRegex = Regex("[\\u0900-\\u097F]")

    private val gujaratiKeywords = listOf(
        "gujarati", "kem chho", "kem cho", "su kare", "shu kare", "shu chhe", "aapo",
        "vaat karo", "tamare", "tamaru", "mane", "kholjo", "kholo ne", "nathi", "ha",
        "barabar", "saru", "tame", "hu", "chhu", "kahu"
    )

    private val hinglishKeywords = listOf(
        "kholo", "chalu", "karo", "rahi hoon", "raha hai", "mujhe", "batao", "kaise",
        "kya", "shuru", "band", "bhai", "hai", "nahi", "kar do", "open karo", "on karo",
        "off karo", "idea do", "bol rahi", "samajh", "tum", "aap", "kaun", "sunao",
        "chahiye", "karoge", "kar sakti", "karna", "pe", "se", "ho", "tha", "rahe"
    )

    fun detect(text: String): LanguageType {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return LanguageType.ENGLISH

        // Check for Gujarati script
        if (gujaratiCharRegex.containsMatchIn(trimmed)) {
            return LanguageType.GUJARATI
        }

        // Check for Devanagari Hindi script
        if (devanagariCharRegex.containsMatchIn(trimmed)) {
            return LanguageType.HINDI
        }

        val lower = trimmed.lowercase()

        // Check if Gujarati language is explicitly requested or phrases used
        if (gujaratiKeywords.any { lower.contains(it) }) {
            return LanguageType.GUJARATI
        }

        // Check for Hinglish phrases
        val words = lower.split(Regex("\\s+"))
        val hinglishCount = words.count { word -> hinglishKeywords.contains(word) }
        if (hinglishCount >= 1 || hinglishKeywords.any { lower.contains(it) }) {
            return LanguageType.HINGLISH
        }

        return LanguageType.ENGLISH
    }
}
