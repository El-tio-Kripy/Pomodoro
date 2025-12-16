package com.example.pomodoro.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerEngine(
    private val scope: CoroutineScope,
    private val config: TimerConfig = TimerConfig()
) {
    private val _state = MutableStateFlow(
        TimerState(
            phase = TimerPhase.Work,
            remainingSeconds = config.workDurationSeconds,
            pomodoroIndex = 0,
            isRunning = false,
            longBreakInterval = config.longBreakInterval
        )
    )
    val state: StateFlow<TimerState> = _state

    private val _phaseCompletions = MutableSharedFlow<PhaseCompletion>(extraBufferCapacity = 1)
    val phaseCompletions = _phaseCompletions.asSharedFlow()

    private var timerJob: Job? = null

    val formattedTime: StateFlow<String> = _state
        .map { state -> formatTime(state.remainingSeconds) }
        .stateIn(scope, SharingStarted.Eagerly, formatTime(_state.value.remainingSeconds))

    fun start() {
        if (timerJob?.isActive == true) return
        startCountdown()
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _state.value = _state.value.copy(isRunning = false)
    }

    fun reset() {
        stop()
        _state.value = TimerState(
            phase = TimerPhase.Work,
            remainingSeconds = config.workDurationSeconds,
            pomodoroIndex = 0,
            isRunning = false,
            longBreakInterval = config.longBreakInterval
        )
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = scope.launch {
            _state.value = _state.value.copy(isRunning = true)
            while (isActive) {
                var remainingSeconds = _state.value.remainingSeconds
                val currentPhase = _state.value.phase
                val currentPomodoroIndex = _state.value.pomodoroIndex
                while (remainingSeconds > 0 && isActive) {
                    delay(1000L)
                    remainingSeconds -= 1
                    _state.value = _state.value.copy(remainingSeconds = remainingSeconds)
                }

                if (!isActive) break

                val completedDuration = durationForPhase(currentPhase)
                val completedPomodoroIndex = if (currentPhase == TimerPhase.Work) {
                    currentPomodoroIndex + 1
                } else {
                    currentPomodoroIndex
                }

                _phaseCompletions.emit(
                    PhaseCompletion(
                        phase = currentPhase,
                        durationSeconds = completedDuration,
                        pomodoroIndex = completedPomodoroIndex
                    )
                )

                val nextPhase = determineNextPhase(currentPhase, completedPomodoroIndex)
                val nextDuration = durationForPhase(nextPhase)
                val nextPomodoroIndex = if (currentPhase == TimerPhase.Work) {
                    completedPomodoroIndex
                } else {
                    currentPomodoroIndex
                }

                _state.value = TimerState(
                    phase = nextPhase,
                    remainingSeconds = nextDuration,
                    pomodoroIndex = nextPomodoroIndex,
                    isRunning = true,
                    longBreakInterval = config.longBreakInterval
                )
            }
        }
    }

    private fun determineNextPhase(phase: TimerPhase, pomodoroIndex: Int): TimerPhase {
        return when (phase) {
            TimerPhase.Work -> {
                if (pomodoroIndex > 0 && pomodoroIndex % config.longBreakInterval == 0) {
                    TimerPhase.LongBreak
                } else {
                    TimerPhase.ShortBreak
                }
            }

            TimerPhase.ShortBreak, TimerPhase.LongBreak -> TimerPhase.Work
        }
    }

    private fun durationForPhase(phase: TimerPhase): Int {
        return when (phase) {
            TimerPhase.Work -> config.workDurationSeconds
            TimerPhase.ShortBreak -> config.shortBreakDurationSeconds
            TimerPhase.LongBreak -> config.longBreakDurationSeconds
        }
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
