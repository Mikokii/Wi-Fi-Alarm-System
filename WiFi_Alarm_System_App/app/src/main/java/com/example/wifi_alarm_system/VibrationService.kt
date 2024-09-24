package com.example.wifi_alarm_system

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.wifi_alarm_system.NotificationService

class VibrationService : Service() {

    private lateinit var notificationService: NotificationService

    override fun onCreate() {
        super.onCreate()
        notificationService = NotificationService(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isAlarmActivated = intent?.getBooleanExtra("isAlarmActivated", true) ?: true

        // Use the appropriate notification
        val notification = if (isAlarmActivated) {
            notificationService.getActivationNotification()
        } else {
            notificationService.getOffNotification()
        }

        // Start the foreground service with the chosen notification
        startForeground(3, notification)

        // Trigger vibration
        vibrate()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }
}
