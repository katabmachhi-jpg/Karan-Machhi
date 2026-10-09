package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.ai.GroundedResponse
import com.example.ai.ImageGenerationResult
import com.example.ai.LanguageDetector
import com.example.ai.LocalCommandEngine
import com.example.ai.MusicGenerationResult
import com.example.ai.TranscriptionResult
import com.example.media.AudioRecordingManager
import com.example.media.MusicPlayerManager
import com.example.model.ChatMessage
import com.example.model.LanguageType
import com.example.model.ToolActionResult
import com.example.model.VoiceState
import com.example.tools.ConnectedToolInfo
import com.example.tools.DeviceToolManager
import com.example.voice.SpeechSynthesisManager
import com.example.voice.VoiceRecognizerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    VOICE_ASSISTANT("Voice AI"),
    GEMINI_CHAT("Chatbot"),
    SEARCH_GROUNDING("Search AI"),
    CREATIVE_STUDIO("Music & Art"),
    TRANSCRIBER("Transcribe")
}

class MyraViewModel(application: Application) : AndroidViewModel(application) {

    val deviceTools = DeviceToolManager(application.applicationContext)
    val voiceRecognizer = VoiceRecognizerManager(application.applicationContext)
    val speechSynthesis = SpeechSynthesisManager(application.applicationContext)
    val musicPlayer = MusicPlayerManager(application.applicationContext)
    val audioRecorder = AudioRecordingManager(application.applicationContext)

    private val localCommandEngine = LocalCommandEngine(deviceTools)
    private val geminiService = GeminiService()

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.VOICE_ASSISTANT)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // 1. Voice Assistant State
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _currentLanguage = MutableStateFlow(LanguageType.ENGLISH)
    val currentLanguage: StateFlow<LanguageType> = _currentLanguage.asStateFlow()

    private val _lastToolResult = MutableStateFlow<ToolActionResult?>(null)
    val lastToolResult: StateFlow<ToolActionResult?> = _lastToolResult.asStateFlow()

    private val _connectedTools = MutableStateFlow<List<ConnectedToolInfo>>(emptyList())
    val connectedTools: StateFlow<List<ConnectedToolInfo>> = _connectedTools.asStateFlow()

    // Live Voice Mode (gemini-3.8-live)
    private val _isLiveVoiceMode = MutableStateFlow(false)
    val isLiveVoiceMode: StateFlow<Boolean> = _isLiveVoiceMode.asStateFlow()

    // 2. Multi-turn Gemini Chatbot State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _selectedChatModel = MutableStateFlow("gemini-3.5-flash")
    val selectedChatModel: StateFlow<String> = _selectedChatModel.asStateFlow()

    private val _selectedRole = MutableStateFlow("Personal Assistant")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    private val _isChatbotResponding = MutableStateFlow(false)
    val isChatbotResponding: StateFlow<Boolean> = _isChatbotResponding.asStateFlow()

    // 3. Search Grounding State (gemini-3.5-flash + googleSearch)
    private val _groundedResponses = MutableStateFlow<List<GroundedResponse>>(emptyList())
    val groundedResponses: StateFlow<List<GroundedResponse>> = _groundedResponses.asStateFlow()

    private val _isSearchingWithGrounding = MutableStateFlow(false)
    val isSearchingWithGrounding: StateFlow<Boolean> = _isSearchingWithGrounding.asStateFlow()

    // 4. Music Generation State (lyria-3-clip-preview / lyria-3-pro-preview)
    private val _selectedMusicModel = MutableStateFlow("lyria-3-clip-preview")
    val selectedMusicModel: StateFlow<String> = _selectedMusicModel.asStateFlow()

    private val _isGeneratingMusic = MutableStateFlow(false)
    val isGeneratingMusic: StateFlow<Boolean> = _isGeneratingMusic.asStateFlow()

    private val _generatedMusicResult = MutableStateFlow<MusicGenerationResult?>(null)
    val generatedMusicResult: StateFlow<MusicGenerationResult?> = _generatedMusicResult.asStateFlow()

    // 5. Image Creation & Editing State (gemini-3.1-flash-image-preview)
    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _generatedImageResult = MutableStateFlow<ImageGenerationResult?>(null)
    val generatedImageResult: StateFlow<ImageGenerationResult?> = _generatedImageResult.asStateFlow()

    private val _imageAspectRatio = MutableStateFlow("1:1")
    val imageAspectRatio: StateFlow<String> = _imageAspectRatio.asStateFlow()

    // 6. Audio Transcription State (gemini-3.5-transcribe)
    private val _isTranscribingAudio = MutableStateFlow(false)
    val isTranscribingAudio: StateFlow<Boolean> = _isTranscribingAudio.asStateFlow()

    private val _transcriptionResult = MutableStateFlow<TranscriptionResult?>(null)
    val transcriptionResult: StateFlow<TranscriptionResult?> = _transcriptionResult.asStateFlow()

    // 7. GitHub & Download Links State
    private val _githubRepoUrl = MutableStateFlow(DeviceToolManager.DEFAULT_GITHUB_URL)
    val githubRepoUrl: StateFlow<String> = _githubRepoUrl.asStateFlow()

    // Speech Flow Shortcuts
    val isListening: StateFlow<Boolean> = voiceRecognizer.isListening
    val isSpeaking: StateFlow<Boolean> = speechSynthesis.isSpeaking
    val isMuted: StateFlow<Boolean> = speechSynthesis.isMuted
    val speechRate: StateFlow<Float> = speechSynthesis.speechRate
    val pitch: StateFlow<Float> = speechSynthesis.pitch
    val audioAmplitude: StateFlow<Float> = voiceRecognizer.audioAmplitude
    val partialTranscript: StateFlow<String> = voiceRecognizer.partialText

    init {
        // Welcome message for Voice Assistant
        _messages.value = listOf(
            ChatMessage(
                text = "Namaste! I am MYRA AI, your smart voice assistant. I understand Hindi, Gujarati, Hinglish, and English. Say \"YouTube kholo\" or \"Flashlight on karo\".",
                isUser = false,
                language = LanguageType.ENGLISH
            )
        )

        // Initial message for Chatbot studio
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Hello! I am your multi-turn Gemini Assistant. You can switch between gemini-3.1-pro-preview for deep tasks, gemini-3.5-flash for general tasks, and gemini-3.1-flash-lite-preview for quick queries.",
                isUser = false,
                language = LanguageType.ENGLISH
            )
        )

        refreshConnectedTools()

        voiceRecognizer.onFinalResult = { recognizedText ->
            processCommand(recognizedText)
        }

        viewModelScope.launch {
            combine(
                voiceRecognizer.isListening,
                speechSynthesis.isSpeaking,
                _voiceState
            ) { listening, speaking, current ->
                when {
                    listening -> VoiceState.LISTENING
                    speaking -> VoiceState.SPEAKING
                    current == VoiceState.PROCESSING -> VoiceState.PROCESSING
                    else -> VoiceState.IDLE
                }
            }.collect { resolvedState ->
                if (_voiceState.value != VoiceState.PROCESSING || resolvedState != VoiceState.IDLE) {
                    _voiceState.value = resolvedState
                }
            }
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleLiveVoiceMode() {
        val newState = !_isLiveVoiceMode.value
        _isLiveVoiceMode.value = newState
        if (newState) {
            startListening()
        } else {
            stopListening()
        }
    }

    fun refreshConnectedTools() {
        _connectedTools.value = deviceTools.getConnectedToolsInfo()
    }

    fun startListening() {
        speechSynthesis.stop()
        voiceRecognizer.clearError()
        _voiceState.value = VoiceState.LISTENING
        voiceRecognizer.startListening(_currentLanguage.value)
    }

    fun stopListening() {
        voiceRecognizer.stopListening()
        _voiceState.value = VoiceState.IDLE
    }

    fun toggleVoiceListening() {
        if (voiceRecognizer.isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    // Voice assistant command processing
    fun processCommand(commandText: String) {
        if (commandText.isBlank()) return

        val detectedLang = LanguageDetector.detect(commandText)
        _currentLanguage.value = detectedLang

        val userMsg = ChatMessage(
            text = commandText,
            isUser = true,
            language = detectedLang
        )
        _messages.value = _messages.value + userMsg
        _voiceState.value = VoiceState.PROCESSING

        viewModelScope.launch {
            val localEval = localCommandEngine.evaluate(commandText)

            if (localEval != null && localEval.handledLocally) {
                delay(150)
                _lastToolResult.value = localEval.toolResult
                refreshConnectedTools()

                val assistantMsg = ChatMessage(
                    text = localEval.responseText,
                    isUser = false,
                    language = localEval.detectedLanguage,
                    toolResult = localEval.toolResult
                )
                _messages.value = _messages.value + assistantMsg
                speechSynthesis.speak(localEval.responseText, localEval.detectedLanguage)
                _voiceState.value = VoiceState.IDLE
            } else {
                val modelToUse = if (_isLiveVoiceMode.value) "gemini-3.8-live" else "gemini-3.5-flash"
                val history = _messages.value.map { it.text to it.isUser }
                val grounded = geminiService.generateResponse(
                    prompt = commandText,
                    detectedLanguage = detectedLang,
                    history = history,
                    model = modelToUse
                )

                val assistantMsg = ChatMessage(
                    text = grounded.text,
                    isUser = false,
                    language = detectedLang
                )
                _messages.value = _messages.value + assistantMsg
                speechSynthesis.speak(grounded.text, detectedLang)
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    // --- 2. Multi-turn Gemini Chatbot Feature ---
    fun selectChatModel(model: String) {
        _selectedChatModel.value = model
    }

    fun selectRole(role: String) {
        _selectedRole.value = role
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        val lang = LanguageDetector.detect(text)
        val userMsg = ChatMessage(text = text, isUser = true, language = lang)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatbotResponding.value = true

        viewModelScope.launch {
            val roleInstruction = when (_selectedRole.value) {
                "Code Expert" -> "You are a world-class Senior Software Engineer and coding mentor. Give clean, production-ready, step-by-step code and architectural insights."
                "Creative Writer" -> "You are an imaginative creative writer, storyteller, and ideation partner. Craft engaging narratives, scripts, and original ideas."
                "Gujarati & Hindi Specialist" -> "You are a master linguistic specialist in Gujarati and Hindi literature, idioms, cultural context, and conversational nuances."
                else -> "You are MYRA AI, a warm, helpful, multilingual personal assistant."
            }

            val history = _chatMessages.value.map { it.text to it.isUser }
            val response = geminiService.generateResponse(
                prompt = text,
                detectedLanguage = lang,
                history = history,
                model = _selectedChatModel.value,
                roleInstruction = roleInstruction
            )

            val botMsg = ChatMessage(text = response.text, isUser = false, language = lang)
            _chatMessages.value = _chatMessages.value + botMsg
            _isChatbotResponding.value = false
        }
    }

    // --- 3. Search Grounding Feature (gemini-3.5-flash + googleSearch) ---
    fun runSearchGrounding(query: String) {
        if (query.isBlank()) return
        _isSearchingWithGrounding.value = true

        viewModelScope.launch {
            val lang = LanguageDetector.detect(query)
            val result = geminiService.generateResponse(
                prompt = query,
                detectedLanguage = lang,
                model = "gemini-3.5-flash",
                useSearchGrounding = true
            )
            _groundedResponses.value = listOf(result) + _groundedResponses.value
            _isSearchingWithGrounding.value = false
        }
    }

    // --- 4. Music Generation Feature (lyria-3-clip-preview / lyria-3-pro-preview) ---
    fun selectMusicModel(model: String) {
        _selectedMusicModel.value = model
    }

    fun generateMusic(prompt: String) {
        if (prompt.isBlank()) return
        _isGeneratingMusic.value = true

        viewModelScope.launch {
            val result = geminiService.generateMusic(
                prompt = prompt,
                model = _selectedMusicModel.value
            )
            _generatedMusicResult.value = result
            _isGeneratingMusic.value = false

            // Auto-play audio if generated
            result.audioBytes?.let { bytes ->
                musicPlayer.playAudioData(bytes, result.title)
            }
        }
    }

    // --- 5. Image Creation & Editing Feature (gemini-3.1-flash-image-preview) ---
    fun setImageAspectRatio(ratio: String) {
        _imageAspectRatio.value = ratio
    }

    fun generateImage(prompt: String) {
        if (prompt.isBlank()) return
        _isGeneratingImage.value = true

        viewModelScope.launch {
            val result = geminiService.generateOrEditImage(
                prompt = prompt,
                aspectRatio = _imageAspectRatio.value
            )
            _generatedImageResult.value = result
            _isGeneratingImage.value = false
        }
    }

    // --- 6. Audio Transcription Feature (gemini-3.5-transcribe) ---
    fun startAudioRecording(): Boolean {
        return audioRecorder.startRecording()
    }

    fun stopAndTranscribeAudio() {
        val audioBytes = audioRecorder.stopRecording()
        if (audioBytes == null || audioBytes.isEmpty()) {
            return
        }
        _isTranscribingAudio.value = true

        viewModelScope.launch {
            val result = geminiService.transcribeAudio(
                audioBytes = audioBytes,
                mimeType = "audio/mp4"
            )
            _transcriptionResult.value = result
            _isTranscribingAudio.value = false
        }
    }

    fun replayMessage(message: ChatMessage) {
        speechSynthesis.speak(message.text, message.language)
    }

    fun stopSpeaking() {
        speechSynthesis.stop()
    }

    fun toggleMute() {
        speechSynthesis.toggleMute()
    }

    fun setSpeechRate(rate: Float) {
        speechSynthesis.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        speechSynthesis.setPitch(pitch)
    }

    fun testVoiceDelivery(language: LanguageType = _currentLanguage.value) {
        val sample = when (language) {
            LanguageType.GUJARATI -> "નમસ્તે! હું MYRA AI છું. મારી વૉઇસ ડિલિવરી એકદમ કુદરતી અને મધુર છે."
            LanguageType.HINDI -> "नमस्ते! मैं MYRA AI हूँ। मैं आपके साथ एक सहज, मधुर और स्वाभाविक आवाज़ में बात करती हूँ।"
            LanguageType.HINGLISH -> "Hello! Main MYRA AI hoon. Meri awaaz ekdum natural, warm aur clear hai."
            LanguageType.ENGLISH -> "Hello! I am MYRA AI. My voice delivery is smooth, clear, and conversational."
        }
        speechSynthesis.speak(sample, language)
    }

    fun toggleFlashlight() {
        val result = deviceTools.toggleFlashlight()
        _lastToolResult.value = result
        val text = if (result.isSuccess) {
            if (deviceTools.getFlashlightState()) "Flashlight on kar rahi hoon." else "Flashlight band kar rahi hoon."
        } else {
            "Flashlight feature is not available on this device."
        }
        val assistantMsg = ChatMessage(
            text = text,
            isUser = false,
            language = LanguageType.HINGLISH,
            toolResult = result
        )
        _messages.value = _messages.value + assistantMsg
        speechSynthesis.speak(text, LanguageType.HINGLISH)
        refreshConnectedTools()
    }

    fun setLanguage(lang: LanguageType) {
        _currentLanguage.value = lang
    }

    fun clearHistory() {
        speechSynthesis.stop()
        _messages.value = emptyList()
        _lastToolResult.value = null
    }

    fun clearChatbotHistory() {
        _chatMessages.value = emptyList()
    }

    // GitHub & Download Link Actions
    fun updateGithubRepoUrl(url: String) {
        val clean = url.trim()
        if (clean.isNotBlank()) {
            _githubRepoUrl.value = clean
        }
    }

    fun openGitHubRepo() {
        val result = deviceTools.openGitHub(_githubRepoUrl.value)
        _lastToolResult.value = result
    }

    fun openGitHubReleases() {
        val result = deviceTools.openGitHubReleases(_githubRepoUrl.value)
        _lastToolResult.value = result
    }

    fun downloadSourceZip() {
        val result = deviceTools.downloadSourceCode(_githubRepoUrl.value)
        _lastToolResult.value = result
    }

    fun getReleasesUrl(): String {
        val base = _githubRepoUrl.value
        return if (base.endsWith("/")) "${base}releases" else "$base/releases"
    }

    fun getSourceZipUrl(): String {
        val base = _githubRepoUrl.value
        return if (base.endsWith("/")) "${base}archive/refs/heads/main.zip" else "$base/archive/refs/heads/main.zip"
    }

    fun getCloneCommand(): String {
        val base = _githubRepoUrl.value.removeSuffix("/")
        val repo = if (base.endsWith(".git")) base else "$base.git"
        return "git clone $repo"
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognizer.destroy()
        speechSynthesis.destroy()
        musicPlayer.release()
        audioRecorder.cancel()
    }
}
