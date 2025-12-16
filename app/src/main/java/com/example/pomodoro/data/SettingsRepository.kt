package com.example.pomodoro.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pomodoro_settings")

class SettingsRepository(private val context: Context) {

    val settingsFlow: Flow<PomodoroSettings> = context.dataStore.data.map { preferences ->
        PomodoroSettings(
            workMinutes = preferences[KEY_WORK] ?: DEFAULT.workMinutes,
            breakMinutes = preferences[KEY_BREAK] ?: DEFAULT.breakMinutes,
            autoStart = preferences[KEY_AUTO_START] ?: DEFAULT.autoStart
        )
    }

    suspend fun saveSettings(settings: PomodoroSettings) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WORK] = settings.workMinutes
            preferences[KEY_BREAK] = settings.breakMinutes
            preferences[KEY_AUTO_START] = settings.autoStart
        }
    }

    companion object {
        private val KEY_WORK = intPreferencesKey("work_minutes")
        private val KEY_BREAK = intPreferencesKey("break_minutes")
        private val KEY_AUTO_START = booleanPreferencesKey("auto_start")
        private val DEFAULT = PomodoroSettings()
    }
}
