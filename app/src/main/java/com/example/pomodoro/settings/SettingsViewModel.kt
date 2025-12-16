package com.example.pomodoro.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pomodoro.data.PomodoroSettings
import com.example.pomodoro.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val workMinutes: String = "",
    val shortBreakMinutes: String = "",
    val longBreakMinutes: String = "",
    val longBreakInterval: String = "",
    val saved: Boolean = false
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentSettings()
    }

    fun onWorkMinutesChange(value: String) = updateState { it.copy(workMinutes = value, saved = false) }

    fun onShortBreakMinutesChange(value: String) =
        updateState { it.copy(shortBreakMinutes = value, saved = false) }

    fun onLongBreakMinutesChange(value: String) =
        updateState { it.copy(longBreakMinutes = value, saved = false) }

    fun onLongBreakIntervalChange(value: String) =
        updateState { it.copy(longBreakInterval = value, saved = false) }

    fun saveSettings(): PomodoroSettings {
        val current = _uiState.value
        val settings = PomodoroSettings(
            workMinutes = parsePositiveNumber(current.workMinutes, DEFAULT.workMinutes),
            shortBreakMinutes = parsePositiveNumber(current.shortBreakMinutes, DEFAULT.shortBreakMinutes),
            longBreakMinutes = parsePositiveNumber(current.longBreakMinutes, DEFAULT.longBreakMinutes),
            longBreakInterval = parsePositiveNumber(current.longBreakInterval, DEFAULT.longBreakInterval)
        )
        repository.saveSettings(settings)
        _uiState.value = SettingsUiState(
            workMinutes = settings.workMinutes.toString(),
            shortBreakMinutes = settings.shortBreakMinutes.toString(),
            longBreakMinutes = settings.longBreakMinutes.toString(),
            longBreakInterval = settings.longBreakInterval.toString(),
            saved = true
        )
        return settings
    }

    private fun loadCurrentSettings() {
        val settings = repository.loadSettings()
        _uiState.value = SettingsUiState(
            workMinutes = settings.workMinutes.toString(),
            shortBreakMinutes = settings.shortBreakMinutes.toString(),
            longBreakMinutes = settings.longBreakMinutes.toString(),
            longBreakInterval = settings.longBreakInterval.toString(),
            saved = false
        )
    }

    private fun parsePositiveNumber(value: String, fallback: Int): Int {
        return value.toIntOrNull()?.coerceAtLeast(1) ?: fallback
    }

    private fun updateState(block: (SettingsUiState) -> SettingsUiState) {
        _uiState.update(block)
    }

    companion object {
        private val DEFAULT = PomodoroSettings()

        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = SettingsRepository(context.applicationContext)
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(repository) as T
                }
            }
        }
    }
}
