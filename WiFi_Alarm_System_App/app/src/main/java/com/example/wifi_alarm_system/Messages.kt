package com.example.wifi_alarm_system

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

object Messages {
    var movementMessages by mutableStateOf(listOf<Int>())
    var startingMessage by mutableIntStateOf(-1)
    var onMessage by mutableIntStateOf(-1)
    var lastOnMessage by mutableIntStateOf(-1)
    var connectionResult by mutableStateOf("")
    var connectionError by mutableStateOf(false)
    var soundMessage by mutableIntStateOf(-1)
}