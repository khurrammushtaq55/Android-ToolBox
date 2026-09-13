package com.mmushtaq.orm.allinone.features.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TimerTool { STOPWATCH, TIMER }

data class StopwatchUiState(
    val tool: TimerTool = TimerTool.STOPWATCH,
    val elapsedMs: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<Long> = emptyList(),
    val timerTotalMs: Long = 60_000L,
    val timerRemainingMs: Long = 60_000L,
    val isTimerRunning: Boolean = false,
    val timerFinished: Boolean = false
)

class StopwatchTimerViewModel : ViewModel() {

    private val _state = MutableStateFlow(StopwatchUiState())
    val state: StateFlow<StopwatchUiState> = _state

    private var tickJob: Job? = null
    private var startedAtMs: Long = 0L
    private var baseElapsedMs: Long = 0L

    fun selectTool(tool: TimerTool) = _state.update { it.copy(tool = tool) }

    // ---- Stopwatch ----
    fun startStopwatch() {
        if (_state.value.isRunning) return
        startedAtMs = System.currentTimeMillis()
        baseElapsedMs = _state.value.elapsedMs
        _state.update { it.copy(isRunning = true) }
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive && _state.value.isRunning) {
                val now = System.currentTimeMillis()
                _state.update { it.copy(elapsedMs = baseElapsedMs + (now - startedAtMs)) }
                delay(30L)
            }
        }
    }

    fun pauseStopwatch() {
        tickJob?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    fun lap() {
        if (!_state.value.isRunning) return
        _state.update { it.copy(laps = it.laps + it.elapsedMs) }
    }

    fun resetStopwatch() {
        tickJob?.cancel()
        _state.update { it.copy(elapsedMs = 0L, isRunning = false, laps = emptyList()) }
    }

    // ---- Timer ----
    fun setTimerDuration(ms: Long) {
        if (_state.value.isTimerRunning) return
        val clamped = ms.coerceIn(1000L, 24 * 3600 * 1000L)
        _state.update { it.copy(timerTotalMs = clamped, timerRemainingMs = clamped, timerFinished = false) }
    }

    fun startTimer() {
        val s = _state.value
        if (s.isTimerRunning || s.timerRemainingMs <= 0L) return
        startedAtMs = System.currentTimeMillis()
        baseElapsedMs = s.timerTotalMs - s.timerRemainingMs
        _state.update { it.copy(isTimerRunning = true, timerFinished = false) }
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive && _state.value.isTimerRunning) {
                val now = System.currentTimeMillis()
                val elapsed = baseElapsedMs + (now - startedAtMs)
                val remaining = (_state.value.timerTotalMs - elapsed).coerceAtLeast(0L)
                _state.update { it.copy(timerRemainingMs = remaining) }
                if (remaining <= 0L) {
                    _state.update { it.copy(isTimerRunning = false, timerFinished = true) }
                    break
                }
                delay(30L)
            }
        }
    }

    fun pauseTimer() {
        tickJob?.cancel()
        _state.update { it.copy(isTimerRunning = false) }
    }

    fun resetTimer() {
        tickJob?.cancel()
        _state.update { it.copy(timerRemainingMs = it.timerTotalMs, isTimerRunning = false, timerFinished = false) }
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}

fun formatMs(ms: Long, showMillis: Boolean = true): String {
    val totalMs = ms.coerceAtLeast(0L)
    val h = totalMs / 3_600_000
    val m = (totalMs % 3_600_000) / 60_000
    val s = (totalMs % 60_000) / 1000
    val cs = (totalMs % 1000) / 10
    return when {
        h > 0 -> "%02d:%02d:%02d".format(h, m, s)
        showMillis -> "%02d:%02d.%02d".format(m, s, cs)
        else -> "%02d:%02d".format(m, s)
    }
}