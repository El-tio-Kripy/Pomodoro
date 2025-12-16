package com.example.pomodoro.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val preferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val settingsFlow = MutableStateFlow(loadSettings())

    fun loadSettings(): PomodoroSettings {
        val work = preferences.getInt(KEY_WORK_MINUTES, DEFAULT.workMinutes)
        val shortBreak = preferences.getInt(KEY_SHORT_BREAK, DEFAULT.shortBreakMinutes)
        val longBreak = preferences.getInt(KEY_LONG_BREAK, DEFAULT.longBreakMinutes)
        val interval = preferences.getInt(KEY_LONG_BREAK_INTERVAL, DEFAULT.longBreakInterval)

        return PomodoroSettings(
            workMinutes = work.coerceAtLeast(1),
            shortBreakMinutes = shortBreak.coerceAtLeast(1),
            longBreakMinutes = longBreak.coerceAtLeast(1),
            longBreakInterval = interval.coerceAtLeast(1)
        )
    }

    fun saveSettings(settings: PomodoroSettings) {
        preferences.edit()
            .putInt(KEY_WORK_MINUTES, settings.workMinutes)
            .putInt(KEY_SHORT_BREAK, settings.shortBreakMinutes)
            .putInt(KEY_LONG_BREAK, settings.longBreakMinutes)
            .putInt(KEY_LONG_BREAK_INTERVAL, settings.longBreakInterval)
            .apply()

        settingsFlow.value = settings
    }

    fun observeSettings(): StateFlow<PomodoroSettings> = settingsFlow.asStateFlow()

    companion object {
        private const val PREFS_NAME = "pomodoro_settings"
        private const val KEY_WORK_MINUTES = "work_minutes"
        private const val KEY_SHORT_BREAK = "short_break_minutes"
        private const val KEY_LONG_BREAK = "long_break_minutes"
        private const val KEY_LONG_BREAK_INTERVAL = "long_break_interval"

        private val DEFAULT = PomodoroSettings()
    }
}
