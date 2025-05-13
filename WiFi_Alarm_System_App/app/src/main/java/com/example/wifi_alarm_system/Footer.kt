package com.example.wifi_alarm_system

import androidx.compose.material3.Button
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.example.wifi_alarm_system.MainActivity
import com.example.wifi_alarm_system.Messages.connectionResult
import com.example.wifi_alarm_system.Messages.onMessage
import com.example.wifi_alarm_system.Messages.soundMessage
import com.example.wifi_alarm_system.Messages.startingMessage
import com.example.wifi_alarm_system.SharedResources.connectionMaker
import com.example.wifi_alarm_system.SharedResources.deviceUUID
import org.json.JSONObject

@Composable
fun FooterButtons(
    isDarkThemeState: MutableState<Boolean>,
    prefs: SharedPreferences
){
    val isDarkTheme = isDarkThemeState.value
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HandleSettingsButton()
        HandleSoundButton()
        DarkModeButton(
            isDarkTheme = isDarkTheme,
            onToggleTheme = {
                isDarkThemeState.value = !isDarkTheme
                prefs.edit().putBoolean("isDarkTheme", isDarkThemeState.value).apply()
            }
        )
    }
}

@Composable
fun HandleSettingsButton(){
    val context = LocalContext.current
    Button(onClick = {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    })
    {
        Icon(painter = painterResource(
            id = R.drawable.baseline_settings_24),
            contentDescription = "Notification Settings",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun DarkModeButton(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Button(
        onClick = onToggleTheme
    ) {
        if (isDarkTheme){
            Icon(
                painter = painterResource(id = R.drawable.ic_light_mode),
                contentDescription = "Toggle Theme",
                modifier = Modifier.size(24.dp)
            )
        }
        else {
            Icon(
                painter = painterResource(id = R.drawable.ic_dark_mode),
                contentDescription = "Toggle Theme",
                modifier = Modifier.size(24.dp)
            )
        }

    }
}

@Composable
fun HandleSoundButton(){
    var isActive by remember { mutableStateOf(false) }
    Button(onClick = {
        if (isActive) {
            publishSoundMessage()
        }
    },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
    )
    {
        if (connectionResult.isNotEmpty() && startingMessage == 0 && onMessage == 1) {
            isActive = true;

            if (soundMessage == 1) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.baseline_volume_up_24
                    ),
                    contentDescription = "Sound ON",
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    painter = painterResource(
                        id = R.drawable.baseline_volume_off_24
                    ),
                    contentDescription = "Sound OFF",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        else
        {
            isActive = false;
            Icon(
                painter = painterResource(
                    id = R.drawable.baseline_volume_off_24
                ),
                contentDescription = "Sound OFF",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun publishSoundMessage(){
    soundMessage = soundMessage xor 1
    val json = JSONObject()
    json.put("deviceID", "$deviceUUID")
    json.put("sound", soundMessage)
    val mqttMessage = json.toString()
    connectionMaker.publishMessage("sound", mqttMessage)
}