package com.example.pomodoro.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.data.PomodoroSettings
import com.example.pomodoro.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val workMinutes: String = "",
    val breakMinutes: String = "",
    val autoStart: Boolean = true,
    val saved: Boolean = false
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                _uiState.value = SettingsUiState(
                    workMinutes = settings.workMinutes.toString(),
                    breakMinutes = settings.breakMinutes.toString(),
                    autoStart = settings.autoStart,
                    saved = false
                )
            }
        }
    }

    fun onWorkMinutesChange(value: String) =
        _uiState.update { it.copy(workMinutes = value, saved = false) }

    fun onBreakMinutesChange(value: String) =
        _uiState.update { it.copy(breakMinutes = value, saved = false) }

    fun onAutoStartChange(value: Boolean) =
        _uiState.update { it.copy(autoStart = value, saved = false) }

    fun save() {
        val current = _uiState.value
        val settings = PomodoroSettings(
            workMinutes = parseMinutes(current.workMinutes, DEFAULT.workMinutes),
            breakMinutes = parseMinutes(current.breakMinutes, DEFAULT.breakMinutes),
            autoStart = current.autoStart
        )
        viewModelScope.launch {
            repository.saveSettings(settings)
            _uiState.update {
                it.copy(
                    workMinutes = settings.workMinutes.toString(),
                    breakMinutes = settings.breakMinutes.toString(),
                    autoStart = settings.autoStart,
                    saved = true
                )
            }
        }
    }

    private fun parseMinutes(value: String, fallback: Int): Int {
        return value.toIntOrNull()?.coerceAtLeast(1) ?: fallback
    }

    companion object {
        private val DEFAULT = PomodoroSettings()

        fun factory(repository: SettingsRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(repository) as T
                }
            }
        }
    }
}
