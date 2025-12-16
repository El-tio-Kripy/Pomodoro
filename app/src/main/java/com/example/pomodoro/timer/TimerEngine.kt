package com.example.pomodoro.timer

import android.os.SystemClock
import com.example.pomodoro.data.PomodoroSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max

class TimerEngine(
    private val coroutineScope: CoroutineScope,
    initialSettings: PomodoroSettings
) {
    private var settings = initialSettings
    private val _state = MutableStateFlow(TimerState.initial(settings))
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun start() {
        if (_state.value.isRunning) return
        if (_state.value.remainingSeconds <= 0) {
            handlePhaseCompletion()
            return
        }
        launchTimer()
    }

    fun stop() {
        cancelTimer()
        _state.value = TimerState.initial(settings)
    }

    fun resetPhase() {
        cancelTimer()
        _state.update {
            it.copy(
                remainingSeconds = durationForPhase(it.phase),
                isRunning = false
            )
        }
    }

    fun updateSettings(newSettings: PomodoroSettings) {
        settings = newSettings
        cancelTimer()
        _state.value = TimerState.initial(settings)
    }

    private fun launchTimer() {
        val targetTime = SystemClock.elapsedRealtime() + _state.value.remainingSeconds * 1000L
        _state.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (isActive) {
                val millisLeft = targetTime - SystemClock.elapsedRealtime()
                val newRemaining = ceil(millisLeft / 1000.0)
                    .toInt()
                    .coerceAtLeast(0)

                if (newRemaining != _state.value.remainingSeconds) {
                    _state.update { it.copy(remainingSeconds = newRemaining) }
                }

                if (millisLeft <= 0) break
                delay(200)
            }
            if (isActive) {
                handlePhaseCompletion()
            }
        }
    }

    private fun handlePhaseCompletion() {
        cancelTimer()
        val nextPhase = if (_state.value.phase == TimerPhase.WORK) {
            TimerPhase.BREAK
        } else {
            TimerPhase.WORK
        }
        _state.value = TimerState(
            phase = nextPhase,
            remainingSeconds = durationForPhase(nextPhase),
            isRunning = false,
            settings = settings
        )
        if (settings.autoStart) {
            start()
        }
    }

    private fun durationForPhase(phase: TimerPhase): Int {
        val minutes = when (phase) {
            TimerPhase.WORK -> settings.workMinutes
            TimerPhase.BREAK -> settings.breakMinutes
        }
        return max(1, minutes) * SECONDS_IN_MINUTE
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    companion object {
        private const val SECONDS_IN_MINUTE = 60
    }
}
