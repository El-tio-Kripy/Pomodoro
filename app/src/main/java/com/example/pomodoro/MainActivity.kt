package com.example.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pomodoro.timer.TimerPhase
import com.example.pomodoro.timer.TimerState

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val timerState by viewModel.timerState.collectAsState()
            val formattedTime by viewModel.formattedTime.collectAsState()
            PomodoroApp(
                timerState = timerState,
                formattedTime = formattedTime,
                onStart = { viewModel.start() },
                onStop = { viewModel.stop() },
                onReset = { viewModel.reset() }
            )
        }
    }
}

@Composable
fun PomodoroApp(
    timerState: TimerState,
    formattedTime: String,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            TimerContent(
                timerState = timerState,
                formattedTime = formattedTime,
                onStart = onStart,
                onStop = onStop,
                onReset = onReset
            )
        }
    }
}

@Composable
fun TimerContent(
    timerState: TimerState,
    formattedTime: String,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit
) {
    val completedInCycle = timerState.pomodoroIndex % timerState.longBreakInterval
    val displayedCompleted = if (completedInCycle == 0 && timerState.pomodoroIndex > 0) {
        timerState.longBreakInterval
    } else {
        completedInCycle
    }
    val phaseLabel = when (timerState.phase) {
        TimerPhase.Work -> "Trabajo"
        TimerPhase.ShortBreak -> "Descanso corto"
        TimerPhase.LongBreak -> "Descanso largo"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fase actual",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = phaseLabel,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pomodoros completados: $displayedCompleted/${timerState.longBreakInterval}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Iniciar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Detener")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Reset")
        }
    }
}
