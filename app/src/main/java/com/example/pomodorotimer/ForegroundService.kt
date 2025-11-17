package com.example.pomodorotimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat


class ForegroundService : Service() {

    private var timerStarted = false
    private var timer: CountDownTimer? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (timerStarted){
            return START_NOT_STICKY
        }

        timer = object: CountDownTimer(Long.MAX_VALUE, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingMillis = millisUntilFinished
                val tickIntent = Intent(COUNTDOWN_BR).apply {
                    putExtra("toCount", remainingMillis)
                }
                sendBroadcast(tickIntent)
            }

            override fun onFinish() {
                Log.i("timerapp", "temporizador en servicio finalizado")
            }
        }.also {
            it.start()
            timerStarted = true
        }

        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_running))
                .setSmallIcon(R.drawable.timericon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        timer?.cancel()
        timerStarted = false

        val stopIntent = Intent(COUNTDOWN_BR).apply {
            putExtra("forceStopped", true)
        }
        sendBroadcast(stopIntent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sesiones Pomodoro",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val COUNTDOWN_BR = "ForegroundService.countdown_br"
        private const val NOTIFICATION_ID = 9000

    }
}