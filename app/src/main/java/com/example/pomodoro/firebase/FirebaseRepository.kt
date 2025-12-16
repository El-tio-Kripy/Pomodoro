package com.example.pomodoro.firebase

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.pomodoro.timer.TimerPhase
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository(private val context: Context) {

    private val tag = "FirebaseRepository"
    private val sessionId: String = UUID.randomUUID().toString()
    private val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: sessionId

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private fun ensureAppInitialized(): Boolean {
        if (FirebaseApp.getApps(context).isNotEmpty()) return true
        val app = FirebaseApp.initializeApp(context)
        if (app == null) {
            Log.w(tag, "Firebase is not configured. Add google-services.json to enable logging.")
            return false
        }
        return true
    }

    suspend fun ensureAuthenticated() {
        if (!ensureAppInitialized()) return
        if (auth.currentUser != null) return
        auth.signInAnonymously().await()
    }

    suspend fun logPhaseCompletion(phase: TimerPhase, durationSeconds: Int, pomodoroIndex: Int) {
        if (!ensureAppInitialized()) return
        ensureAuthenticated()
        val db = Firebase.firestore
        val payload = hashMapOf(
            "timestamp" to Timestamp.now(),
            "phase" to phase.name,
            "durationSeconds" to durationSeconds,
            "pomodoroIndex" to pomodoroIndex,
            "deviceId" to deviceId,
            "sessionId" to sessionId
        )
        db.collection("pomodoroSessions").add(payload).await()
    }
}
