package com.example.pomodoro

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.data.SettingsRepository
import com.example.pomodoro.timer.TimerEngine
import com.example.pomodoro.timer.TimerState
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private var currentSettings = settingsRepository.loadSettings()
    private val timerEngine = TimerEngine(viewModelScope, currentSettings)

    val state: StateFlow<TimerState> = timerEngine.state

    fun toggleRunning() {
        if (state.value.isRunning) {
            timerEngine.pause()
        } else {
            timerEngine.start()
        }
    }

    fun stop() {
        timerEngine.stop()
    }

    fun skipPhase() {
        timerEngine.skip()
    }

    fun refreshSettings() {
        val latest = settingsRepository.loadSettings()
        currentSettings = latest
        timerEngine.refreshSettings(latest)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = SettingsRepository(context.applicationContext)
                    @Suppress("UNCHECKED_CAST")
                    return TimerViewModel(repository) as T
                }
            }
        }
    }
}
