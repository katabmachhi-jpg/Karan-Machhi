package com.example

import com.example.ai.LanguageDetector
import com.example.model.LanguageType
import com.example.viewmodel.AppTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLanguageDetection() {
        assertEquals(LanguageType.GUJARATI, LanguageDetector.detect("Gujarati ma vaat karo"))
        assertEquals(LanguageType.GUJARATI, LanguageDetector.detect("કેમ છો"))
        assertEquals(LanguageType.HINDI, LanguageDetector.detect("नमस्ते आप कैसे हो"))
        assertEquals(LanguageType.HINGLISH, LanguageDetector.detect("YouTube kholo"))
        assertEquals(LanguageType.HINGLISH, LanguageDetector.detect("Flashlight on karo"))
        assertEquals(LanguageType.HINGLISH, LanguageDetector.detect("Mujhe ek AI app ka idea do"))
        assertEquals(LanguageType.ENGLISH, LanguageDetector.detect("What is the capital of France?"))
    }

    @Test
    fun testAppTabs() {
        assertEquals(5, AppTab.entries.size)
        assertTrue(AppTab.entries.contains(AppTab.VOICE_ASSISTANT))
        assertTrue(AppTab.entries.contains(AppTab.GEMINI_CHAT))
        assertTrue(AppTab.entries.contains(AppTab.SEARCH_GROUNDING))
        assertTrue(AppTab.entries.contains(AppTab.CREATIVE_STUDIO))
        assertTrue(AppTab.entries.contains(AppTab.TRANSCRIBER))
    }
}
