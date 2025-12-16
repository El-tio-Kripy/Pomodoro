package com.example.pomodoro.timer

import com.example.pomodoro.data.PomodoroSettings

data class TimerState(
    val phase: TimerPhase,
    val remainingSeconds: Int,
    val isRunning: Boolean,
    val settings: PomodoroSettings
) {
    companion object {
        fun initial(settings: PomodoroSettings) = TimerState(
            phase = TimerPhase.WORK,
            remainingSeconds = settings.workMinutes * SECONDS_IN_MINUTE,
            isRunning = false,
            settings = settings
        )

        private const val SECONDS_IN_MINUTE = 60
    }
}
