package com.example.pomodoro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.firebase.FirebaseRepository
import com.example.pomodoro.timer.TimerEngine
import com.example.pomodoro.timer.TimerState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseRepository(application.applicationContext)
    private val timerEngine = TimerEngine(viewModelScope)

    val timerState: StateFlow<TimerState> = timerEngine.state
    val formattedTime = timerEngine.formattedTime

    init {
        viewModelScope.launch {
            repository.ensureAuthenticated()
        }

        viewModelScope.launch {
            timerEngine.phaseCompletions.collect { completion ->
                repository.logPhaseCompletion(
                    completion.phase,
                    completion.durationSeconds,
                    completion.pomodoroIndex
                )
            }
        }
    }

    fun start() = timerEngine.start()

    fun stop() = timerEngine.stop()

    fun reset() = timerEngine.reset()
}
