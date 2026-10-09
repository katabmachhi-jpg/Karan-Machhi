package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.LanguageType
import com.example.ui.theme.BrightViolet
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ToolSuccess
import com.example.viewmodel.MyraViewModel

@Composable
fun TranscriberScreen(viewModel: MyraViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isRecording by viewModel.audioRecorder.isRecording.collectAsState()
    val recordDuration by viewModel.audioRecorder.durationSeconds.collectAsState()
    val amplitude by viewModel.audioRecorder.amplitude.collectAsState()

    val isTranscribing by viewModel.isTranscribingAudio.collectAsState()
    val transcriptionResult by viewModel.transcriptionResult.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startAudioRecording()
        } else {
            Toast.makeText(context, "Microphone permission is required to record audio", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleRecordClick() {
        if (isRecording) {
            viewModel.stopAndTranscribeAudio()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                viewModel.startAudioRecording()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightSurface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("transcriber_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Surface(
            color = MidnightElevated,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Audio Transcription Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Powered by gemini-3.5-transcribe model for authentic multilingual speech recognition in Hindi, Gujarati, Hinglish, and English.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Center Recorder Section
        Box(contentAlignment = Alignment.Center) {
            // Animated Outer Pulse Ring
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size((120 + (amplitude * 50)).dp)
                        .clip(CircleShape)
                        .background(NeonPink.copy(alpha = 0.25f))
                )
            }

            FloatingActionButton(
                onClick = { handleRecordClick() },
                containerColor = if (isRecording) NeonPink else ElectricViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(90.dp)
                    .testTag("record_audio_fab")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop & Transcribe" else "Record Audio",
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isRecording) "Recording: ${recordDuration}s (Tap to Finish)" else "Tap to start recording speech",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = if (isRecording) NeonPink else TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isTranscribing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NeonCyan, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Transcribing with gemini-3.5-transcribe...", color = NeonCyan, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Transcription Result Display
        if (transcriptionResult != null) {
            val result = transcriptionResult!!
            Surface(
                color = MidnightCard,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (result.isSuccess) ToolSuccess.copy(alpha = 0.4f) else NeonPink.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ToolSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LANG: ${result.detectedLanguage.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ToolSuccess
                            )
                        }

                        Text(
                            text = result.modelUsed,
                            fontSize = 10.sp,
                            color = BrightViolet,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = result.transcript,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(result.transcript))
                                Toast.makeText(context, "Transcript copied", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val lang = LanguageType.fromDisplayName(result.detectedLanguage)
                                viewModel.speechSynthesis.speak(result.transcript, lang)
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", tint = BrightViolet)
                        }
                    }
                }
            }
        }
    }
}
