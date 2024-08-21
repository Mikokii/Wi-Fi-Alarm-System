package com.example.wifi_alarm_system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wifi_alarm_system.ui.theme.WiFi_Alarm_SystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val service = NotificationService(applicationContext)
        val connectionMaker = ConnectionMaker()
        setContent {
            WiFi_Alarm_SystemTheme {

                var messages by remember { mutableStateOf(listOf<String>()) }
                var connectionResult by remember { mutableStateOf("") }

                Box(modifier = Modifier.fillMaxSize().padding(16.dp)){
                    Column {
                        Button(onClick = {
                            // Connect and subscribe to the topic
                            connectionResult = connectionMaker.connectAndSubscribe { message ->
                                // Update the list of messages
                                messages = (messages + message).takeLast(3)
                            }
                            // Show the latest message in the notification
                            service.showNotification(Counter.value)
                        }) {
                            Text(text = "Connect and Subscribe")
                        }

                        // Display the connection result
                        if (connectionResult.isNotEmpty()) {
                            Text(text = "Connection Result: $connectionResult")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display the last three messages
                        Text(text = "Last 3 Messages:")
                        messages.forEachIndexed { index, message ->
                            Text(text = "${index + 1}: $message")
                        }
                    }
                }
            }
        }
    }
}

