package com.example.SkyNote.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NoteFAB(
    onAddNote: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_animations")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.primary,
        targetValue = Color(0xFF00C6FF),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    // 2. Hiệu ứng nhịp thở (Pulse)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    FloatingActionButton(
        onClick = onAddNote,
        containerColor = animatedColor,
        contentColor = Color.White,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        modifier = Modifier
            .padding(16.dp)
            .size(64.dp)
            .scale(scale)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Thêm ghi chú mới",
            modifier = Modifier.size(34.dp)
        )
    }
}
