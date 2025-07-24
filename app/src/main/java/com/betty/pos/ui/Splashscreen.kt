package com.betty.pos.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.betty.pos.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val splashColors = listOf(
        Color(0xFFFFC0CB), // Rosa chicle
        Color(0xFFD1B2FF), // Morado pastel
        Color(0xFFFFF176), // Amarillo suave
        Color(0xFFA7FFEB)  // Verde menta
    )

    val backgroundColor = remember { splashColors.random() }
    var visible by remember { mutableStateOf(true) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600), label = "alpha"
    )

    LaunchedEffect(true) {
        delay(2500)
        visible = false
        delay(600)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .graphicsLayer(alpha = alpha),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.betty_mascota),
            contentDescription = "Mascota de Betty",
            modifier = Modifier.size(180.dp)
        )
    }
}