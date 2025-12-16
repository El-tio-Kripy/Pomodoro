package com.example.pomodoro.data

data class PomodoroSettings(
    val workMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val autoStart: Boolean = true
)
