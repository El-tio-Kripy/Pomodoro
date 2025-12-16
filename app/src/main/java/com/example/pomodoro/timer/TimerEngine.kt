package com.example.pomodoro.timer

import android.os.SystemClock
import com.example.pomodoro.data.PomodoroSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
            handlePhaseCompletion(shouldContinue = true)
            return
        }
        launchTimer()
    }

    fun pause() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(isRunning = false) }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _state.value = TimerState.initial(settings)
    }

    fun skip() {
        val wasRunning = _state.value.isRunning
        timerJob?.cancel()
        timerJob = null
        handlePhaseCompletion(shouldContinue = wasRunning)
    }

    fun refreshSettings(newSettings: PomodoroSettings) {
        settings = newSettings
        timerJob?.cancel()
        timerJob = null
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
                handlePhaseCompletion(shouldContinue = true)
            }
        }
    }

    private fun handlePhaseCompletion(shouldContinue: Boolean) {
        val current = _state.value
        val nextState = when (current.phase) {
            TimerPhase.WORK -> advanceFromWork(current)
            TimerPhase.BREAK_SHORT, TimerPhase.BREAK_LONG -> advanceFromBreak(current)
        }
        _state.value = nextState
        if (shouldContinue) {
            start()
        }
    }

    private fun advanceFromWork(current: TimerState): TimerState {
        val completedWorks = current.completedWorkSessions + 1
        val isLongBreak = completedWorks % settings.longBreakInterval == 0
        val nextPhase = if (isLongBreak) TimerPhase.BREAK_LONG else TimerPhase.BREAK_SHORT
        return TimerState(
            phase = nextPhase,
            remainingSeconds = durationForPhase(nextPhase),
            isRunning = false,
            completedWorkSessions = completedWorks,
            settings = settings
        )
    }

    private fun advanceFromBreak(current: TimerState): TimerState {
        val resetCount = if (current.phase == TimerPhase.BREAK_LONG) 0 else current.completedWorkSessions
        return TimerState(
            phase = TimerPhase.WORK,
            remainingSeconds = durationForPhase(TimerPhase.WORK),
            isRunning = false,
            completedWorkSessions = resetCount,
            settings = settings
        )
    }

    private fun durationForPhase(phase: TimerPhase): Int {
        val minutes = when (phase) {
            TimerPhase.WORK -> settings.workMinutes
            TimerPhase.BREAK_SHORT -> settings.shortBreakMinutes
            TimerPhase.BREAK_LONG -> settings.longBreakMinutes
        }
        return max(1, minutes) * SECONDS_IN_MINUTE
    }

    companion object {
        private const val SECONDS_IN_MINUTE = 60
    }
}
