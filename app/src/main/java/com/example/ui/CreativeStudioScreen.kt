package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.viewmodel.MyraViewModel

@Composable
fun CreativeStudioScreen(viewModel: MyraViewModel) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Music, 1: Images

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightSurface)
            .testTag("creative_studio_screen")
    ) {
        // Studio Mode Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = DeepSpace,
            contentColor = TextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                    color = if (selectedSection == 0) GlowAmber else NeonPink
                )
            }
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Music Generation", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                },
                selectedContentColor = GlowAmber,
                unselectedContentColor = TextMuted
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Image Studio", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                },
                selectedContentColor = NeonPink,
                unselectedContentColor = TextMuted
            )
        }

        if (selectedSection == 0) {
            MusicGenerationView(viewModel = viewModel)
        } else {
            ImageStudioView(viewModel = viewModel)
        }
    }
}

@Composable
private fun MusicGenerationView(viewModel: MyraViewModel) {
    val selectedModel by viewModel.selectedMusicModel.collectAsState()
    val isGenerating by viewModel.isGeneratingMusic.collectAsState()
    val musicResult by viewModel.generatedMusicResult.collectAsState()

    val isPlaying by viewModel.musicPlayer.isPlaying.collectAsState()
    val progressMs by viewModel.musicPlayer.progressMs.collectAsState()
    val durationMs by viewModel.musicPlayer.durationMs.collectAsState()
    val trackTitle by viewModel.musicPlayer.currentTrackTitle.collectAsState()

    var musicPrompt by remember { mutableStateOf("") }

    val musicPresets = listOf(
        "Indian fusion acoustic flute & sitar melody",
        "Calm morning meditation ambient chime",
        "Upbeat electronic cyberpunk synthwave",
        "Relaxing lo-fi study beat with soft piano"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Model Selection
        item {
            Surface(
                color = MidnightElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "MUSIC MODEL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ModelOptionChip(
                            modelId = "lyria-3-clip-preview",
                            label = "Short Clip (30s)",
                            isSelected = selectedModel == "lyria-3-clip-preview",
                            onClick = { viewModel.selectMusicModel("lyria-3-clip-preview") },
                            modifier = Modifier.weight(1f)
                        )
                        ModelOptionChip(
                            modelId = "lyria-3-pro-preview",
                            label = "Full Track (Pro)",
                            isSelected = selectedModel == "lyria-3-pro-preview",
                            onClick = { viewModel.selectMusicModel("lyria-3-pro-preview") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Music Prompt Input
        item {
            Surface(
                color = MidnightCard,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Describe the track you want to create",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = musicPrompt,
                        onValueChange = { musicPrompt = it },
                        placeholder = { Text("E.g. Peaceful Indian classical flute with tabla groove...", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("music_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowAmber,
                            unfocusedBorderColor = BrightViolet.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = MidnightElevated,
                            unfocusedContainerColor = MidnightElevated
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset prompt chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(musicPresets) { preset ->
                            Surface(
                                color = MidnightElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, BrightViolet.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        musicPrompt = preset
                                    }
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FilledTonalButton(
                        onClick = {
                            if (musicPrompt.isNotBlank()) {
                                viewModel.generateMusic(musicPrompt)
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GlowAmber,
                            contentColor = DeepSpace
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("generate_music_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = DeepSpace,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating with $selectedModel...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Music with $selectedModel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Live Audio Player Card
        item {
            if (musicResult != null) {
                Surface(
                    color = MidnightCard,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlowAmber.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = trackTitle ?: "Generated Audio",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Model: ${musicResult?.modelUsed}",
                                    fontSize = 11.sp,
                                    color = GlowAmber
                                )
                            }
                            IconButton(
                                onClick = { viewModel.musicPlayer.togglePlayPause() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(GlowAmber)
                                    .testTag("play_pause_music_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = DeepSpace,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val maxDuration = if (durationMs > 0) durationMs.toFloat() else 1000f
                        Slider(
                            value = progressMs.toFloat().coerceIn(0f, maxDuration),
                            onValueChange = { viewModel.musicPlayer.seekTo(it.toInt()) },
                            valueRange = 0f..maxDuration,
                            colors = SliderDefaults.colors(
                                thumbColor = GlowAmber,
                                activeTrackColor = GlowAmber,
                                inactiveTrackColor = MidnightElevated
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${progressMs / 1000}s",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "${durationMs / 1000}s",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageStudioView(viewModel: MyraViewModel) {
    val isGenerating by viewModel.isGeneratingImage.collectAsState()
    val imageResult by viewModel.generatedImageResult.collectAsState()
    val aspectRatio by viewModel.imageAspectRatio.collectAsState()

    var imagePrompt by remember { mutableStateOf("") }

    val imagePresets = listOf(
        "Futuristic neon AI voice assistant orb with holographic rings",
        "Cyberpunk cityscape at sunset in electric violet and cyan",
        "Traditional Indian rangoli festival with glowing lamps",
        "Minimalist 3D robotic companion in modern workspace"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = MidnightElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "MODEL: gemini-3.1-flash-image-preview",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aspect Ratio",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1:1" to "Square", "16:9" to "Landscape", "9:16" to "Portrait").forEach { (ratio, label) ->
                            val isSelected = aspectRatio == ratio
                            Surface(
                                color = if (isSelected) NeonPink.copy(alpha = 0.25f) else MidnightCard,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSelected) NeonPink else BrightViolet.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setImageAspectRatio(ratio) }
                            ) {
                                Text(
                                    text = "$ratio ($label)",
                                    fontSize = 11.sp,
                                    color = if (isSelected) NeonPink else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Surface(
                color = MidnightCard,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Create or Edit Image with Text Prompt",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imagePrompt,
                        onValueChange = { imagePrompt = it },
                        placeholder = { Text("E.g. A futuristic glowing cyber assistant with audio waves...", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = BrightViolet.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = MidnightElevated,
                            unfocusedContainerColor = MidnightElevated
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(imagePresets) { preset ->
                            Surface(
                                color = MidnightElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, BrightViolet.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable { imagePrompt = preset }
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FilledTonalButton(
                        onClick = {
                            if (imagePrompt.isNotBlank()) {
                                viewModel.generateImage(imagePrompt)
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NeonPink,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("generate_image_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating with gemini-3.1-flash-image-preview...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create / Edit Image", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Generated Image Preview Card
        item {
            if (imageResult?.bitmap != null) {
                Surface(
                    color = MidnightCard,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonPink.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "GENERATED IMAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = imageResult!!.bitmap!!.asImageBitmap(),
                            contentDescription = imageResult!!.prompt,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${imageResult!!.prompt}\"",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Model: ${imageResult!!.modelUsed}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelOptionChip(
    modelId: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) GlowAmber.copy(alpha = 0.2f) else MidnightCard,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (isSelected) GlowAmber else BrightViolet.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GlowAmber else TextPrimary
            )
            Text(
                text = modelId,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
