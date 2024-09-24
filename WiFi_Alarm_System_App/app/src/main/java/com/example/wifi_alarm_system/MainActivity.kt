package com.example.wifi_alarm_system

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.wifi_alarm_system.Messages.connectionError
import com.example.wifi_alarm_system.Messages.connectionResult
import com.example.wifi_alarm_system.Messages.lastOnMessage
import com.example.wifi_alarm_system.Messages.movementMessages
import com.example.wifi_alarm_system.Messages.onMessage
import com.example.wifi_alarm_system.Messages.soundMessage
import com.example.wifi_alarm_system.Messages.startingMessage
import java.util.UUID
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private val connectionMaker = ConnectionMaker()
    private val deviceUUID = getUUID()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WiFi_Alarm_SystemTheme {
                HandleConnectionButton()
                HandleMessages()
                HandleSettingsButton()
                HandleSoundButton()
            }
        }
    }

    @Composable
    private fun HandleConnectionButton(){
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
                Button(onClick = handleConnectionButtonAction(permissionLauncher)) {
                    Text(text = "Connect and Subscribe")
                }
                Spacer(modifier = Modifier.height(16.dp))
                DisplayConnectionResult()
            }
        }
    }
    private fun handleConnectionButtonAction(permissionLauncher: ManagedActivityResultLauncher<String, Boolean>): () -> Unit =
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            // Connect and subscribe to the topic
            connectionResult = connectionMaker.connectAndSubscribe { message ->
                when {
                    "movement" in message -> { updateMovementMessages(message) }
                    "starting" in message -> { updateStartingMessage(message) }
                    "ON" in message -> { updateOnMessage(message) }
                    "sound" in message -> { updateSoundMessage(message) }
                    else -> { connectionError = true }
                }
            }

        }
    @Composable
    private fun DisplayConnectionResult(){
        // Display the connection result
        if (connectionResult.isEmpty()) {
            Text(text = "No Connection")
        }
        else {
            Text(text = "Connection Result: $connectionResult")
        }
    }
    private fun updateMovementMessages(message: String) {
        if (message.length >= 2) {
            movementMessages =
                (movementMessages + message[message.length - 2].toString()
                    .toInt()).takeLast(5)
            connectionError = false
        } else {
            connectionError = true
        }
    }
    private fun updateStartingMessage(message: String) {
        if (message.length >= 2) {
            startingMessage = message[message.length - 2].toString().toInt()
            connectionError = false
        } else {
            connectionError = true
        }
    }
    private fun updateOnMessage(message: String){
        if (message.length >= 2) {
            lastOnMessage = onMessage
            onMessage = message[message.length - 2].toString().toInt()
            connectionError = false
        } else {
            connectionError = true
        }
    }
    private fun updateSoundMessage(message: String){
        if (message.length >= 2) {
            soundMessage = message[message.length - 2].toString().toInt()
            connectionError = false
        } else {
            connectionError = true
        }
    }

    @Composable
    private fun HandleMessages(){
        val service = NotificationService(applicationContext)
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .absoluteOffset(0.dp, 100.dp),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (connectionError){
                    Text(text = "ERROR")
                } else if(onMessage == 1){
                    Text(text = "Device is ON")
                    if (startingMessage == 1){
                        Text(text = "Alarm system is starting")
                    } else {
                        HandleMovementMessages()
                    }
                } else if (onMessage == 0){
                    Text(text = "Device is OFF")
                    if (lastOnMessage == 1){
                        vibrateThreeTimes(false)
                    }
                } else {
                    Text(text = "Device is not connected")
                }
            }
        }
    }

    @Composable
    private fun HandleMovementMessages(){
        val service = NotificationService(applicationContext)
        movementMessages.asReversed().forEach { movementMessage ->
            when (movementMessage) {
                1 -> { Text(text = "Movement detected") }
                0 -> { Text(text = "No movement detected") }
                else -> { Text(text = "ERROR") }
            }
        }
        if (movementMessages.isNotEmpty()) {
            if (movementMessages.last() == 1) {
                vibrate(true)
            }
        }
    }

    @Composable
    private fun HandleSettingsButton(){
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
    }

    @Composable
    private fun HandleSoundButton(){
        if (connectionResult.isNotEmpty() && startingMessage == 0 && onMessage == 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, end = 32.dp), // Adjust the padding as needed
                contentAlignment = Alignment.BottomEnd // Align content
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = {
                        publishSoundMessage()
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

    private fun publishSoundMessage(){
        soundMessage = soundMessage xor 1
        val json = JSONObject()
        json.put("deviceID", "$deviceUUID")
        json.put("sound", soundMessage)
        val mqttMessage = json.toString()
        connectionMaker.publishMessage("sound", mqttMessage)
    }

    private fun getUUID(): String? {
        return try {
            val deviceId = UUID.randomUUID().toString()
            deviceId
        } catch (e: Exception) {
            null // Handle exception
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun vibrate(alarmActivated: Boolean){
        val intent = Intent(this, VibrationService::class.java)
        intent.putExtra("isAlarmActivated", alarmActivated) // or false based on your condition
        startForegroundService(intent) // Start the service
    }

    private fun vibrateThreeTimes(alarmActivated: Boolean) {
        CoroutineScope(Dispatchers.Main).launch{
            for (i in 1..3){
                vibrate(alarmActivated)
                delay(1000)
            }
        }
    }
}
