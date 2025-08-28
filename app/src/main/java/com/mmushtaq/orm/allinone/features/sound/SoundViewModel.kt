package com.mmushtaq.orm.allinone.features.sound


import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

data class SoundUiState(
    val isMeasuring: Boolean = false,
    val rmsDb: Float = -120f,           // relative dBFS-ish + calibration
    val peakDb: Float = -120f,
    val calibrationOffset: Float = 0f,  // (-20..+20 dB)
    val waveform: List<Float> = emptyList(), // recent normalized samples [-1..1]
    val statusMsg: String = "Idle"
)

class SoundViewModel : ViewModel() {

    private val _state = MutableStateFlow(SoundUiState())
    val state: StateFlow<SoundUiState> = _state

    private var recordJob: Job? = null
    private var recorder: AudioRecord? = null

    fun toggle() {
        if (_state.value.isMeasuring) stop() else start()
    }

    fun setCalibration(offset: Float) = _state.update { it.copy(calibrationOffset = offset) }

    @SuppressLint("MissingPermission")
    private fun start() {
        if (recordJob != null) return

        val sampleRate = 44100
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) {
            _state.update { it.copy(statusMsg = "Microphone not available") }
            return
        }

        val bufferSize = max(minBuffer, 4096)
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.UNPROCESSED, // falls back if unsupported
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            _state.update { it.copy(statusMsg = "Failed to init mic") }
            return
        }
        recorder = audioRecord
        audioRecord.startRecording()
        _state.update { it.copy(isMeasuring = true, statusMsg = "Listening…") }

        // 🚀 Run on background thread
        recordJob = viewModelScope.launch(Dispatchers.Default) {
            // Give this thread audio priority
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            val shortBuf = ShortArray(bufferSize)
            val maxShort = 32768.0
            val waveCapacity = 512
            var smoothedRmsDb = -120.0
            var smoothedPeakDb = -120.0

            // Throttle UI updates (~25 fps)
            var lastEmit = 0L

            while (isActive) {
                // Blocking read is fine on background thread
                val n = audioRecord.read(shortBuf, 0, shortBuf.size, AudioRecord.READ_BLOCKING)
                if (n <= 0) continue

                var sumSq = 0.0
                var peak = 0.0

                val step = max(1, n / waveCapacity)
                // Reuse a small FloatArray to avoid GC churn
                val wave = FloatArray(waveCapacity.coerceAtMost(n / step))

                var wi = 0
                for (i in 0 until n) {
                    val s = shortBuf[i].toDouble()
                    val a = kotlin.math.abs(s)
                    if (a > peak) peak = a
                    sumSq += s * s
                    if (i % step == 0 && wi < wave.size) {
                        wave[wi++] = (s / maxShort).toFloat().coerceIn(-1f, 1f)
                    }
                }

                val rms = sqrt(sumSq / n)
                val dbRaw = if (rms <= 0.0) -120.0 else 20.0 * log10(rms / maxShort)
                val peakDbRaw = if (peak <= 0.0) -120.0 else 20.0 * log10(peak / maxShort)

                smoothedRmsDb = 0.85 * smoothedRmsDb + 0.15 * dbRaw
                smoothedPeakDb = 0.8 * smoothedPeakDb + 0.2 * peakDbRaw

                val cal = _state.value.calibrationOffset.toDouble()
                val showRms = (smoothedRmsDb + cal).coerceIn(-120.0, 12.0).toFloat()
                val showPeak = (smoothedPeakDb + cal).coerceIn(-120.0, 12.0).toFloat()

                val now = System.currentTimeMillis()
                if (now - lastEmit >= 40) { // ~25 updates/sec
                    lastEmit = now
                    _state.update {
                        it.copy(
                            rmsDb = showRms,
                            peakDb = showPeak,
                            waveform = wave.asList() // small list
                        )
                    }
                }
            }
        }
    }

    private fun stop() {
        recordJob?.cancel()
        recordJob = null
        recorder?.run {
            try {
                stop()
            } catch (_: Throwable) {}
            try {
                release()
            } catch (_: Throwable) {}
        }
        recorder = null
        _state.update { it.copy(isMeasuring = false, statusMsg = "Stopped") }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}