package com.example.wifi_alarm_system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.wifi_alarm_system.ui.theme.WiFi_Alarm_SystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val service = NotificationService(applicationContext)
        setContent {
            WiFi_Alarm_SystemTheme {
                Box(modifier = Modifier.fillMaxSize()){
                    Button(onClick = {
                        service.showNotification(Counter.value)
                    }) {
                        Text(text = "Show notification")
                    }
                }
            }
        }
    }
}

