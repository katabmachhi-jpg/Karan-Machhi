package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.ChatMessage
import com.example.model.LanguageType
import com.example.model.ToolActionResult
import com.example.model.VoiceState
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GlowAmber
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ToolError
import com.example.ui.theme.ToolSuccess
import com.example.viewmodel.AppTab
import com.example.viewmodel.MyraViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyraMainScreen(viewModel: MyraViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val scope = rememberCoroutineScope()

    var showToolsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showVoiceStyleSheet by remember { mutableStateOf(false) }
    val voiceStyleSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showGitHubSheet by remember { mutableStateOf(false) }
    val githubSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val connectedTools by viewModel.connectedTools.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val githubRepoUrl by viewModel.githubRepoUrl.collectAsState()

    // Handle Android system back button when on sub-screens
    BackHandler(enabled = currentTab != AppTab.VOICE_ASSISTANT) {
        viewModel.setTab(AppTab.VOICE_ASSISTANT)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepSpace,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MYRA AI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricViolet.copy(alpha = 0.25f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (currentTab) {
                                    AppTab.VOICE_ASSISTANT -> "VOICE ASSISTANT"
                                    AppTab.GEMINI_CHAT -> "GEMINI CHAT"
                                    AppTab.SEARCH_GROUNDING -> "SEARCH GROUNDING"
                                    AppTab.CREATIVE_STUDIO -> "MUSIC & ART"
                                    AppTab.TRANSCRIBER -> "TRANSCRIBE"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSpace,
                    titleContentColor = TextPrimary
                ),
                actions = {
                    IconButton(
                        onClick = { showVoiceStyleSheet = true },
                        modifier = Modifier.testTag("open_voice_style_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice Style Tuning",
                            tint = NeonCyan
                        )
                    }

                    // GitHub & Download Link button
                    IconButton(
                        onClick = { showGitHubSheet = true },
                        modifier = Modifier.testTag("open_github_sheet_button")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_github),
                            contentDescription = "GitHub & Download Link",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier.testTag("toggle_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute voice" else "Mute voice",
                            tint = if (isMuted) TextMuted else BrightViolet
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.refreshConnectedTools()
                            showToolsSheet = true
                        },
                        modifier = Modifier.testTag("open_tools_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Device Tools",
                                tint = BrightViolet
                            )
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(ToolSuccess)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MidnightSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.VOICE_ASSISTANT,
                    onClick = { viewModel.setTab(AppTab.VOICE_ASSISTANT) },
                    icon = { Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice AI") },
                    label = { Text("Voice AI", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = MidnightCard,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.GEMINI_CHAT,
                    onClick = { viewModel.setTab(AppTab.GEMINI_CHAT) },
                    icon = { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Chatbot") },
                    label = { Text("Chatbot", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrightViolet,
                        selectedTextColor = BrightViolet,
                        indicatorColor = MidnightCard,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SEARCH_GROUNDING,
                    onClick = { viewModel.setTab(AppTab.SEARCH_GROUNDING) },
                    icon = { Icon(imageVector = Icons.Default.Language, contentDescription = "Search AI") },
                    label = { Text("Search AI", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = MidnightCard,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CREATIVE_STUDIO,
                    onClick = { viewModel.setTab(AppTab.CREATIVE_STUDIO) },
                    icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Music & Art") },
                    label = { Text("Music/Art", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GlowAmber,
                        selectedTextColor = GlowAmber,
                        indicatorColor = MidnightCard,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TRANSCRIBER,
                    onClick = { viewModel.setTab(AppTab.TRANSCRIBER) },
                    icon = { Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Transcribe") },
                    label = { Text("Transcribe", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPink,
                        selectedTextColor = NeonPink,
                        indicatorColor = MidnightCard,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.VOICE_ASSISTANT -> VoiceAssistantScreen(viewModel = viewModel)
                AppTab.GEMINI_CHAT -> ChatbotScreen(viewModel = viewModel)
                AppTab.SEARCH_GROUNDING -> SearchGroundingScreen(viewModel = viewModel)
                AppTab.CREATIVE_STUDIO -> CreativeStudioScreen(viewModel = viewModel)
                AppTab.TRANSCRIBER -> TranscriberScreen(viewModel = viewModel)
            }
        }

        // Tools Sheet
        if (showToolsSheet) {
            ToolsStatusSheet(
                sheetState = sheetState,
                connectedTools = connectedTools,
                isFlashlightOn = viewModel.deviceTools.getFlashlightState(),
                onToggleFlashlight = { viewModel.toggleFlashlight() },
                onTestBattery = { viewModel.processCommand("battery kitni hai") },
                onOpenGitHub = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showToolsSheet = false
                        showGitHubSheet = true
                    }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showToolsSheet = false
                    }
                }
            )
        }

        // Voice Style Tuning Sheet
        if (showVoiceStyleSheet) {
            VoiceStyleSheet(
                sheetState = voiceStyleSheetState,
                speechRate = speechRate,
                pitch = pitch,
                currentLanguage = currentLanguage,
                onSpeechRateChange = { viewModel.setSpeechRate(it) },
                onPitchChange = { viewModel.setPitch(it) },
                onTestVoice = { lang -> viewModel.testVoiceDelivery(lang) },
                onDismiss = {
                    scope.launch { voiceStyleSheetState.hide() }.invokeOnCompletion {
                        showVoiceStyleSheet = false
                    }
                }
            )
        }

        // GitHub & Download Link Sheet
        if (showGitHubSheet) {
            GitHubDownloadSheet(
                sheetState = githubSheetState,
                currentRepoUrl = githubRepoUrl,
                onOpenRepo = { viewModel.openGitHubRepo() },
                onOpenReleases = { viewModel.openGitHubReleases() },
                onDownloadSourceZip = { viewModel.downloadSourceZip() },
                onUpdateRepoUrl = { viewModel.updateGithubRepoUrl(it) },
                onDismiss = {
                    scope.launch { githubSheetState.hide() }.invokeOnCompletion {
                        showGitHubSheet = false
                    }
                }
            )
        }
    }
}

@Composable
private fun VoiceAssistantScreen(viewModel: MyraViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val messages by viewModel.messages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isLiveVoiceMode by viewModel.isLiveVoiceMode.collectAsState()

    var showTextInput by remember { mutableStateOf(false) }
    var textInputQuery by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice commands", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleMicClick() {
        if (isListening) {
            viewModel.stopListening()
        } else {
            val hasRecordPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasRecordPermission) {
                viewModel.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickCommands = listOf(
        "YouTube kholo",
        "Flashlight on karo",
        "GitHub download link",
        "Mujhe ek AI app ka idea do",
        "Gujarati ma vaat karo",
        "GitHub kholo",
        "WhatsApp kholo",
        "Camera shuru karo",
        "Flashlight band karo",
        "Google Maps kholo",
        "Battery kitni hai"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Hero Orb & Status Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live API Mode Toggle Pill (gemini-3.8-live)
            Surface(
                color = if (isLiveVoiceMode) NeonPink.copy(alpha = 0.2f) else MidnightCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (isLiveVoiceMode) NeonPink else BrightViolet.copy(alpha = 0.25f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.toggleLiveVoiceMode() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isLiveVoiceMode) NeonPink else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLiveVoiceMode) "LIVE VOICE ON (gemini-3.8-live)" else "Enable Live API Mode (gemini-3.8-live)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLiveVoiceMode) NeonPink else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            MyraOrb(
                voiceState = voiceState,
                audioAmplitude = audioAmplitude,
                size = 110.dp,
                onClick = {
                    if (isSpeaking) {
                        viewModel.stopSpeaking()
                    } else {
                        handleMicClick()
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (voiceState == VoiceState.PROCESSING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = NeonCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = when (voiceState) {
                        VoiceState.LISTENING -> "Listening... Speak in Hindi, Gujarati, Hinglish"
                        VoiceState.PROCESSING -> "Thinking..."
                        VoiceState.SPEAKING -> "Speaking aloud (tap orb to stop)"
                        VoiceState.IDLE -> if (isLiveVoiceMode) "Live conversation active" else "Tap mic or say a command"
                        VoiceState.ERROR -> "Ready"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (voiceState) {
                        VoiceState.LISTENING -> NeonCyan
                        VoiceState.PROCESSING -> GlowAmber
                        VoiceState.SPEAKING -> NeonPink
                        else -> TextSecondary
                    }
                )
            }

            // Real-time Audio Waveform & Pulse Intensity Visualizer
            AudioWaveVisualizer(
                isListening = isListening || voiceState == VoiceState.LISTENING,
                audioAmplitude = audioAmplitude,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Live Transcription bubble
            AnimatedVisibility(
                visible = partialTranscript.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MidnightCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = partialTranscript,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Quick Voice Commands
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickCommands) { command ->
                QuickCommandChip(label = command, onClick = { viewModel.processCommand(command) })
            }
        }

        // Messages Feed
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(message = msg, onReplay = { viewModel.replayMessage(msg) })
                }
            }
        }

        // Keyboard Input Drawer
        AnimatedVisibility(
            visible = showTextInput,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MidnightSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInputQuery,
                        onValueChange = { textInputQuery = it },
                        placeholder = { Text("Type command: YouTube kholo, flashlight on...", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("text_input_field"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInputQuery.isNotBlank()) {
                                    viewModel.processCommand(textInputQuery)
                                    textInputQuery = ""
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BrightViolet.copy(alpha = 0.35f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = MidnightElevated,
                            unfocusedContainerColor = MidnightElevated
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInputQuery.isNotBlank()) {
                                viewModel.processCommand(textInputQuery)
                                textInputQuery = ""
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ElectricViolet)
                            .testTag("send_text_command_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send command",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Voice Controls Dock
        Surface(
            color = MidnightSurface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth().shadow(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showTextInput = !showTextInput },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (showTextInput) ElectricViolet else MidnightElevated)
                        .testTag("toggle_keyboard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Toggle Keyboard Input",
                        tint = if (showTextInput) TextPrimary else TextSecondary
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp)
                ) {
                    if (isListening) {
                        Canvas(modifier = Modifier.size(76.dp)) {
                            val baseR = 31.dp.toPx()
                            val pulseR = baseR + (audioAmplitude * 16.dp.toPx())
                            drawCircle(
                                color = NeonPink.copy(alpha = (0.25f + audioAmplitude * 0.45f).coerceIn(0f, 0.7f)),
                                radius = pulseR
                            )
                            drawCircle(
                                color = NeonCyan.copy(alpha = (0.4f + audioAmplitude * 0.5f).coerceIn(0f, 0.9f)),
                                radius = pulseR + 4.dp.toPx(),
                                style = Stroke(width = (1.5f + audioAmplitude * 2f).dp.toPx())
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = { handleMicClick() },
                        containerColor = if (isListening) NeonPink else ElectricViolet,
                        contentColor = TextPrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(62.dp)
                            .testTag("main_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                val isTorchOn = viewModel.deviceTools.getFlashlightState()
                IconButton(
                    onClick = { viewModel.toggleFlashlight() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isTorchOn) GlowAmber.copy(alpha = 0.3f) else MidnightElevated)
                        .testTag("quick_flashlight_button")
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Toggle Flashlight",
                        tint = if (isTorchOn) GlowAmber else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickCommandChip(label: String, onClick: () -> Unit) {
    Surface(
        color = MidnightElevated,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BrightViolet.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, onReplay: () -> Unit) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) MidnightElevated else MidnightCard,
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 14.dp
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(
                    1.dp,
                    if (isUser) ElectricViolet.copy(alpha = 0.4f) else NeonCyan.copy(alpha = 0.25f),
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 14.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "YOU" else "MYRA AI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) NeonCyan else BrightViolet
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MidnightSurface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = message.language.displayName,
                                fontSize = 9.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (!isUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onReplay, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak message",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                if (message.toolResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ToolResultBanner(result = message.toolResult)
                }
            }
        }
    }
}

@Composable
private fun ToolResultBanner(result: ToolActionResult) {
    Surface(
        color = if (result.isSuccess) ToolSuccess.copy(alpha = 0.12f) else ToolError.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (result.isSuccess) ToolSuccess.copy(alpha = 0.35f) else ToolError.copy(alpha = 0.35f),
                RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.isSuccess) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (result.isSuccess) ToolSuccess else ToolError,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "ACTION: ${result.toolName.uppercase()} (${if (result.isSuccess) "CONFIRMED" else "NOT CONNECTED"})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.isSuccess) ToolSuccess else ToolError
                )
                Text(text = result.details, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
