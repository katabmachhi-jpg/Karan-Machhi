package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.model.LanguageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechSynthesisManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Conversational, warm, human-like voice settings
    private val _speechRate = MutableStateFlow(0.92f) // Slightly relaxed pace to avoid rushing
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(1.02f) // Warm, friendly assistant tone
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupVoiceDefaults()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    private fun setupVoiceDefaults() {
        tts?.setSpeechRate(_speechRate.value)
        tts?.setPitch(_pitch.value)
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.7f, 1.3f)
        _speechRate.value = clamped
        tts?.setSpeechRate(clamped)
    }

    fun setPitch(newPitch: Float) {
        val clamped = newPitch.coerceIn(0.8f, 1.4f)
        _pitch.value = clamped
        tts?.setPitch(clamped)
    }

    fun toggleMute() {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        if (newMute) {
            stop()
        }
    }

    /**
     * Speaks text with a natural conversational human-like cadence, warm tone,
     * smooth pronunciation, and breathing pauses between sentences.
     */
    fun speak(text: String, language: LanguageType) {
        if (_isMuted.value || !isInitialized || text.isBlank()) return

        val locale = when (language) {
            LanguageType.GUJARATI -> Locale.forLanguageTag("gu-IN")
            LanguageType.HINDI -> Locale.forLanguageTag("hi-IN")
            LanguageType.HINGLISH -> Locale.forLanguageTag("hi-IN")
            LanguageType.ENGLISH -> Locale.forLanguageTag("en-IN")
        }

        try {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            } else {
                // Select the most natural, high-quality, human-sounding voice available for this locale
                selectBestNaturalVoice(locale)
            }

            // Apply warm, friendly speech rate and pitch
            tts?.setSpeechRate(_speechRate.value)
            tts?.setPitch(_pitch.value)

            // Prepare text: strip formatting artifacts, asterisks, hashtags for clean pronunciation
            val cleanText = text
                .replace(Regex("[*#_`~]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()

            // Split into conversational sentences to insert natural breathing pauses
            val sentences = cleanText.split(Regex("(?<=[.!?।])\\s+")).filter { it.isNotBlank() }

            if (sentences.isEmpty()) {
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "myra_${System.currentTimeMillis()}")
                }
                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "myra_${System.currentTimeMillis()}")
                return
            }

            // Speak sentence by sentence with natural pause (140ms silent utterance) between them
            val baseId = System.currentTimeMillis()
            sentences.forEachIndexed { index, sentence ->
                val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "myra_${baseId}_$index")
                }
                tts?.speak(sentence, queueMode, params, "myra_${baseId}_$index")

                // Natural pause between sentences (unless it's the final sentence)
                if (index < sentences.size - 1) {
                    tts?.playSilentUtterance(140L, TextToSpeech.QUEUE_ADD, null)
                }
            }
        } catch (_: Exception) {
            _isSpeaking.value = false
        }
    }

    /**
     * Finds and assigns the highest quality, warm human-sounding voice
     * for the locale (preferring network or high-quality female voices).
     */
    private fun selectBestNaturalVoice(locale: Locale) {
        val voices = tts?.voices ?: return
        val matchingVoices = voices.filter { it.locale.language == locale.language }

        // Preference order: high quality, natural/neural voice names
        val preferredVoice = matchingVoices.firstOrNull { voice ->
            val name = voice.name.lowercase()
            (name.contains("network") || name.contains("female") || name.contains("natural")) &&
                    voice.quality >= Voice.QUALITY_HIGH
        } ?: matchingVoices.firstOrNull { voice ->
            val name = voice.name.lowercase()
            name.contains("female") || name.contains("local")
        } ?: matchingVoices.firstOrNull()

        if (preferredVoice != null) {
            try {
                tts?.voice = preferredVoice
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}
