/*
 Comentarios traducidos automáticamente al español (traducción parcial;
 revisa manualmente para asegurar precisión técnica y contexto).
*/

package com.example.pomodorotimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pomodorotimer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var timer = Timer()
    private val channelId = "pomodoroTimer"
    lateinit var wakeLock: PowerManager.WakeLock
    private var completed = 0

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun sendNotification() {
        with(NotificationManagerCompat.from(this)) {
            cancel(completed - 1)
        }

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Session $completed Completed")
                .setContentText("Yippee ki yay")
                .setSmallIcon(R.drawable.timericon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()


        with(NotificationManagerCompat.from(this)) {
            notify(completed, notification)
        }
    }

    private fun makeToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP, 0, 200)
        toast.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.textViewCompleted.text = completed.toString()

        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "pomodoroTimer::wakeLock"
        )

        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val savedWorkTimer = sharedPref.getInt("WORK", 0)
        val savedBreakTimer = sharedPref.getInt("BREAK", 0)

        binding.editTextPomodoro.setText(savedWorkTimer.toString())
        binding.editTextBreak.setText(savedBreakTimer.toString())

        Log.i("sharedPref", "work $savedWorkTimer")
        Log.i("sharedPref", "break $savedBreakTimer")

        timer.workTimer = savedWorkTimer
        timer.breakTimer = savedBreakTimer
        timer.loadWorkTimer()
        binding.textViewCountdown.text = timer.displayTime()
        setTimerTextColor()

        binding.fabPlay.setOnClickListener {
            Log.i("timerapp", "clicked timer start")
            if (timer.isCounting) {
                Log.i("timerapp", "ignore duplicate starting request")
                makeToast("Already started")
                return@setOnClickListener
            }
            startTimer()
        }

        binding.fabPause.setOnClickListener {
            Log.i("timerapp", "clicked timer pause")
            if (timer.isCounting) {
                makeToast("Pause timer")
                destroyTimer()
                timer.isCounting = false
                timer.isPause = true
            } else {
                makeToast("Already pause")
            }
        }

        binding.fabStop.setOnClickListener {
            Log.i("timerapp", "clicked timer stop(cancel)")
            makeToast("Cancel timer")
            if (timer.isCounting) {
                destroyTimer()
            }
            timer.resetTimer()
            timer.loadWorkTimer()
            binding.textViewCountdown.text = timer.displayTime()
            setTimerTextColor()
        }

        binding.buttonSet.setOnClickListener {
            Log.i("timerapp", "clicked set button")

            val workTime = binding.editTextPomodoro.text.toString()
            val breakTime = binding.editTextBreak.text.toString()

            val editor = sharedPref.edit()
            editor.putInt("WORK", workTime.toInt())
            editor.putInt("BREAK", breakTime.toInt())
            editor.apply()

            timer.workTimer = if (workTime == "") 2 else workTime.toInt()
            timer.breakTimer = if (breakTime == "") 2 else breakTime.toInt()

            Log.i(
                "timerapp",
                "workTimer set to ${timer.workTimer}, breakTimer set to ${timer.breakTimer}"
            )

            if (!timer.isCounting && !timer.isPause) {
                timer.loadWorkTimer()
                binding.textViewCountdown.text = timer.displayTime()
                binding.textViewCountdown.setTextColor(ContextCompat.getColor(this, R.color.colorWork))
                makeToast("Current session: Work ${timer.workTimer} min, break ${timer.breakTimer} min")
            } else {
                makeToast("Next session: Work ${timer.workTimer} min, break ${timer.breakTimer} min")
            }

            binding.editTextPomodoro.setText(timer.workTimer.toString())
            binding.editTextBreak.setText(timer.breakTimer.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_timer, menu)
        return true
    }

    private val br: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handleCountDown(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("timerapp", "on resume")
        // CAMBIO FINAL: Se añade el tercer parámetro "RECEIVER_NOT_EXPORTED".
        // Esto soluciona el SecurityException en versiones modernas de Android.
        registerReceiver(br, IntentFilter(ForegroundService.COUNTDOWN_BR), RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        Log.i("timerapp", "on destroy")
        unregisterReceiver(br)
        destroyTimer()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        Log.i("timerapp", "on pause")
    }



    override fun onStop() {
        Log.i("timerapp", "on Stop")
        super.onStop()
    }

    private fun destroyTimer(){
        stopService(Intent(this, ForegroundService::class.java))
        if (wakeLock.isHeld){
            wakeLock.release()
        }
    }


    override fun onBackPressed() {
        Log.i("timerapp", "on back button")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun startTimer() {
        wakeLock.acquire()

        when (timer.workState) {
            WorkState.Break -> {
                if (!timer.isPause) {
                    timer.loadBreakTimer()
                }
            }
            WorkState.Work -> {
                if (!timer.isCounting && !timer.isPause){
                    timer.loadWorkTimer()
                    Log.i("timerapp", "start a new timer with  ${timer.displayTime()}")
                    makeToast("start a new timer with  ${timer.displayTime()}")
                }else{
                    Log.i("timerapp", "resume timer from  ${timer.displayTime()}")
                    makeToast("Resume with  ${timer.displayTime()}")
                }
            }
        }

        timer.isPause = false
        timer.isCounting = true
        setTimerTextColor()

        val serviceIntent = Intent(this, ForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun handleCountDown(intent: Intent) {
        if (intent.hasExtra("toCount") && !intent.hasExtra("forceStopped")) {
            timer.minusOneSecond()
            binding.textViewCountdown.text = timer.displayTime()
            Log.i("timerapp", timer.displayTime())

            if (!timer.isCounting) {
                destroyTimer()
                when (timer.workState) {
                    WorkState.Work -> {
                        completed++
                        binding.textViewCompleted.text = completed.toString()
                        sendNotification()
                        timer.workState = WorkState.Break
                        startTimer()
                    }
                    WorkState.Break -> {
                        timer.workState = WorkState.Work
                        startTimer()
                    }
                }
            }
        }
    }

    private fun setTimerTextColor() {
        when (timer.workState) {
            WorkState.Break -> binding.textViewCountdown.setTextColor(ContextCompat.getColor(this, R.color.colorBreak))
            WorkState.Work -> binding.textViewCountdown.setTextColor(ContextCompat.getColor(this, R.color.colorWork))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("timeLeftInSecond", timer.toSeconds())
        outState.putInt("workState", timer.workState.ordinal)
        outState.putBoolean("isCounting", timer.isCounting)
        outState.putBoolean("isPause", timer.isPause)
        outState.putInt("workTimer", timer.workTimer)
        outState.putInt("breakTimer", timer.breakTimer)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        timer.restoreFromSeconds(savedInstanceState.getLong("timeLeftInSecond"))
        timer.workState = WorkState.getValueFromInt(savedInstanceState.getInt("workState"))
        timer.isPause = savedInstanceState.getBoolean("isPause")
        timer.isCounting = savedInstanceState.getBoolean("isCounting")
        timer.workTimer = savedInstanceState.getInt("workTimer")
        timer.breakTimer = savedInstanceState.getInt("breakTimer")
        Log.i("Restore Instance", "timeLeftInSeconds = ${timer.toSeconds()}")
        binding.textViewCountdown.text = timer.displayTime()
        setTimerTextColor()
        if (timer.isCounting) {
            startTimer()
        }
    }
}
