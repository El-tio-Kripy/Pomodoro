package com.example.pomodoro.timer

import com.example.pomodoro.data.PomodoroSettings
import kotlin.math.max

data class TimerState(
    val phase: TimerPhase,
    val remainingSeconds: Int,
    val isRunning: Boolean,
    val completedWorkSessions: Int,
    val settings: PomodoroSettings
) {
    val currentWorkIndex: Int
        get() = when (phase) {
            TimerPhase.WORK -> completedWorkSessions + 1
            else -> max(1, completedWorkSessions)
        }

    companion object {
        fun initial(settings: PomodoroSettings) = TimerState(
            phase = TimerPhase.WORK,
            remainingSeconds = settings.workMinutes * SECONDS_IN_MINUTE,
            isRunning = false,
            completedWorkSessions = 0,
            settings = settings
        )

        private const val SECONDS_IN_MINUTE = 60
    }
}
