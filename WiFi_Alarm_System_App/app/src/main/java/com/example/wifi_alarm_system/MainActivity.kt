package com.example.wifi_alarm_system

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.wifi_alarm_system.SharedResources.connectionMaker
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val isDarkThemeState = rememberSaveable { mutableStateOf(prefs.getBoolean("isDarkTheme", true))}
            WiFi_Alarm_SystemTheme(darkTheme = isDarkThemeState.value) {
                Scaffold(modifier = Modifier.fillMaxSize(),
                         topBar = {
                             WiFiAlarmLogo()
                         },
                         bottomBar = {
                             FooterButtons(isDarkThemeState, prefs)
                         }
                    ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center

                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .width(300.dp)
                                .height(400.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .absoluteOffset(0.dp, (-45).dp),
                            contentAlignment = Alignment.Center
                        ) {
                                HandleConnectionButton()
                                DisplayConnectionResult()
                                HandleMessages()
                        }
                    }
                }
            }
        }

    }
    @Composable
    fun WiFiAlarmLogo() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WiFi Alarm System",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp),
                fontWeight = FontWeight.Bold
            )
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
            .padding(24.dp)
            .absoluteOffset(0.dp, (-100).dp),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button( onClick = handleConnectionButtonAction(permissionLauncher),
                        modifier = Modifier
                            .width(250.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =    Color(0xFFF5BA07),
                        contentColor =      Color.White
                    )) {
                    Text(text = "CONNECT AND SUBSCRIBE")
                }
                Spacer(modifier = Modifier.height(16.dp))

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
    private fun DisplayConnectionResult() {
        // Text displayed in the box about connection status
        val text = "CONNECTION RESULT"
        val textResult = if (connectionResult.isEmpty()) {
            "no connection"
        } else {
            " $connectionResult "
        }

        Box(modifier = Modifier
            .width(250.dp)
            .height(120.dp)
            .absoluteOffset(0.dp, (150).dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center) {
            //Display topic of the box "CONNECTION RESULT"
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(15.dp),
            )
            // Column displaying current state of the connection with the server
            Column(modifier = Modifier
                .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                Text (
                    text = textResult,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    //UPDATE AN ARRAY WITH MOVEMENT STATUS FROM MQTT SERVER
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
        val textColor = MaterialTheme.colorScheme.onPrimary;
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .width(250.dp)
                .height(120.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(32.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (connectionError) {
                    Text(
                        text = "ERROR",
                        color = textColor
                    )
                } else if (onMessage == 1) {
                    Text(
                        text = "Device is ON",
                        color = textColor
                    )
                    if (startingMessage == 1) {
                        Text(
                            text = "Alarm system is starting",
                            color = textColor
                        )
                    } else {
                        HandleMovementMessages()
                    }
                } else if (onMessage == 0) {
                    Text(
                        text = "Device is OFF",
                        color = textColor
                    )
                    if (lastOnMessage == 1) {
                        vibrateThreeTimes(false)
                    }
                } else {
                    Text(
                        text = "Device is not connected",
                        color = textColor
                    )
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
