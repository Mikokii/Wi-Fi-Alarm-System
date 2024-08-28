package com.example.wifi_alarm_system

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.wifi_alarm_system.ui.theme.WiFi_Alarm_SystemTheme
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val service = NotificationService(applicationContext)
        val connectionMaker = ConnectionMaker()
        setContent {
            WiFi_Alarm_SystemTheme {
                var movementMessages by remember { mutableStateOf(listOf<String>()) }
                var startingMessage by remember { mutableStateOf("") }
                var onMessage by remember { mutableStateOf("") }
                var connectionResult by remember { mutableStateOf("") }
                var connectionError by remember { mutableStateOf(false) }
                var soundMessage by remember { mutableIntStateOf(0) }
                var tmpMessage by remember { mutableStateOf("") }

                val context = LocalContext.current
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED)
                    } else mutableStateOf(true)
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted -> hasNotificationPermission = isGranted}
                )
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .absoluteOffset(0.dp, (-100).dp),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            // Connect and subscribe to the topic
                            connectionResult = connectionMaker.connectAndSubscribe { message ->
                                when {
                                    "movement" in message -> {
                                        // Update the list of movement messages
                                        movementMessages = (movementMessages + message).takeLast(5)
                                        connectionError = false
                                    }
                                    "starting" in message -> {
                                        // Update the starting message
                                        startingMessage = message
                                        connectionError = false
                                    }
                                    "ON" in message -> {
                                        //Update the ON message
                                        onMessage = message
                                        connectionError = false
                                    }
                                    "sound" in message -> {
                                        tmpMessage = message
                                        //Update the sound message
                                        if (message.length >= 2){
                                            soundMessage = message[message.length-2].toString().toInt()
                                            connectionError = false
                                        } else {
                                            connectionError = true
                                        }
                                    }
                                    else -> {
                                        connectionError = true
                                    }
                                }
                            }

                        }) {
                            Text(text = "Connect and Subscribe")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (connectionResult.isEmpty()) {
                            Text(text = "No Connection")
                        }

                        // Display the connection result
                        if (connectionResult.isNotEmpty()) {
                            Text(text = "Connection Result: $connectionResult")
                        }
                    }
                }
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .absoluteOffset(0.dp, 100.dp),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (connectionError){
                            Text(text = "ERROR")
                        } else if(onMessage.isNotEmpty() && onMessage.length >= 2 && onMessage[onMessage.length-2] == '1'){
                            Text(text = "Device is ON")
                            if (startingMessage.isNotEmpty() && startingMessage.length >= 2 && startingMessage[startingMessage.length - 2] == '1'){
                                Text(text = "Alarm system is starting")
                            } else {
                                // Display the last five messages
                                movementMessages.asReversed().forEach { movementMessage ->
                                    if (movementMessage.length >= 2) {
                                        val lastMovement = movementMessage[movementMessage.length - 2]
                                        if (lastMovement == '1') {
                                            Text(text = "Movement detected")
                                        } else if (lastMovement == '0'){
                                            Text(text = "No movement detected")
                                        } else {
                                            Text(text = "ERROR")
                                        }
                                    }
                                }
                                if (movementMessages.isNotEmpty() && movementMessages.last().length >= 2) {
                                    if (movementMessages.last()[movementMessages.last().length - 2] == '1') {
                                        service.showNotification()
                                        vibrate()
                                    }
                                }
                            }
                        } else if (onMessage.isNotEmpty() && onMessage.length >= 2 && onMessage[onMessage.length-2] == '0'){
                            Text(text = "Device is OFF")
                        } else {
                            Text(text = "Device is not connected")
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp, start = 32.dp), // Adjust the bottom padding as needed
                    contentAlignment = Alignment.BottomStart // Align content at the bottom and center horizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                            }
                            startActivity(intent)
                        }) {
                            Icon(painter = painterResource(
                                id = R.drawable.baseline_settings_24),
                                contentDescription = "Notification Settings",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                if (connectionResult.isNotEmpty() && startingMessage.isNotEmpty() && startingMessage.length >= 2 && startingMessage[startingMessage.length - 2] == '0') {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp, end = 32.dp), // Adjust the padding as needed
                        contentAlignment = Alignment.BottomEnd // Align content
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(onClick = {
                                soundMessage = soundMessage xor 1
                                val json = JSONObject()
                                json.put("deviceID", "mein Handy")
                                json.put("sound", soundMessage)
                                val mqttMessage = json.toString()
                                val publishResult = connectionMaker.publishMessage("sound", mqttMessage)
                            }) {
                                if (soundMessage == 1){
                                    Icon(painter = painterResource(
                                        id = R.drawable.baseline_volume_up_24),
                                        contentDescription = "Sound ON",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                else {
                                    Icon(painter = painterResource(
                                        id = R.drawable.baseline_volume_off_24),
                                        contentDescription = "Sound OFF",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun vibrate(){
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26){
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        else{
            vibrator.vibrate(500)
        }
    }
}
