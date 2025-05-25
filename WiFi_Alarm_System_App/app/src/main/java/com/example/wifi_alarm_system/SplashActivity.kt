package com.example.wifi_alarm_system

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
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
    val imageViewState = remember { mutableStateOf<ImageView?>(null) }

    LaunchedEffect(true) {
        val view = imageViewState.value
        if (view != null) {
            val animatorX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 0.5f).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
            }
            val animatorY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 0.5f).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
            }

            animatorX.start()
            animatorY.start()
        }
        delay(1200)
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
            AndroidView(
                factory = {
                    ImageView(it).apply {
                        setImageResource(R.drawable.icon2)
                        scaleX = 0f
                        scaleY = 0f
                        imageViewState.value = this
                        layoutParams = ViewGroup.LayoutParams(200, 200)

                        outlineProvider = object : ViewOutlineProvider() {
                            override fun getOutline(view: View, outline: Outline) {
                                val size = view.width.coerceAtMost(view.height)
                                outline.setOval(0, 0, size, size)
                            }
                        }
                        clipToOutline = true
                    }
                },
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(70.dp))
            CircularProgressIndicator()
        }
    }
}
