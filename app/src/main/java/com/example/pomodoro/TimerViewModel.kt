package com.example.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.data.PomodoroSettings
import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.timer.TimerEngine
import com.example.pomodoro.timer.TimerState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val timerEngine = TimerEngine(viewModelScope, PomodoroSettings())
    val state: StateFlow<TimerState> = timerEngine.state

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                timerEngine.updateSettings(settings)
            }
        }
    }

    fun start() = timerEngine.start()

    fun stop() = timerEngine.stop()

    fun reset() = timerEngine.resetPhase()

    companion object {
        fun factory(repository: SettingsRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return TimerViewModel(repository) as T
                }
            }
        }
    }
}
