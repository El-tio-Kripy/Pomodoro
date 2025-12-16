package com.example.pomodoro.timer

data class TimerConfig(
    val workDurationSeconds: Int = 25 * 60,
    val shortBreakDurationSeconds: Int = 5 * 60,
    val longBreakDurationSeconds: Int = 15 * 60,
    val longBreakInterval: Int = 4
)

data class TimerState(
    val phase: TimerPhase,
    val remainingSeconds: Int,
    val pomodoroIndex: Int,
    val isRunning: Boolean,
    val longBreakInterval: Int
)

data class PhaseCompletion(
    val phase: TimerPhase,
    val durationSeconds: Int,
    val pomodoroIndex: Int
)
