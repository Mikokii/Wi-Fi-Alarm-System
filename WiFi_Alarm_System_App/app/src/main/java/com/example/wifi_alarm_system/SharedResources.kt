package com.example.wifi_alarm_system

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import java.util.UUID

object SharedResources {
    val connectionMaker = ConnectionMaker()
    val deviceUUID = getUUID()

    private fun getUUID(): String? {
        return try {
            val deviceId = UUID.randomUUID().toString()
            deviceId
        } catch (e: Exception) {
            null
        }
    }
}