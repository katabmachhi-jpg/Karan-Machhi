package com.example.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class AudioRecordingManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    fun startRecording(): Boolean {
        stopRecording()
        try {
            outputFile = File(context.cacheDir, "rec_${System.currentTimeMillis()}.m4a")

            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mr.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }

            recorder = mr
            _isRecording.value = true
            _durationSeconds.value = 0

            timerJob = scope.launch {
                var seconds = 0
                while (isActive && _isRecording.value) {
                    delay(1000)
                    seconds++
                    _durationSeconds.value = seconds
                    recorder?.let {
                        try {
                            val maxAmp = it.maxAmplitude
                            _amplitude.value = (maxAmp / 32768f).coerceIn(0f, 1f)
                        } catch (_: Exception) {}
                    }
                }
            }
            return true
        } catch (_: Exception) {
            _isRecording.value = false
            return false
        }
    }

    fun stopRecording(): ByteArray? {
        timerJob?.cancel()
        _isRecording.value = false
        _amplitude.value = 0f

        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null

        return try {
            outputFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    val bytes = ByteArray(file.length().toInt())
                    FileInputStream(file).use { it.read(bytes) }
                    bytes
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun cancel() {
        timerJob?.cancel()
        _isRecording.value = false
        _amplitude.value = 0f
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}
