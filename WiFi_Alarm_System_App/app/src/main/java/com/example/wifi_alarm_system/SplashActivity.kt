package com.example.wifi_alarm_system

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

//CLASS WITH IMPLEMENTATION OF THE STARTING ANIMATION
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(true) {
        delay(200)
        onTimeout()
    }
    val rotationDegree = animateFloatAsState(
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 2000)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .absoluteOffset(0.dp, (-45).dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.icon2),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer(rotationZ = rotationDegree.value)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(70.dp))
            CircularProgressIndicator()
        }
    }
}
