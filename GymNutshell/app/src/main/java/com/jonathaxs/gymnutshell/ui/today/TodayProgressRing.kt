package com.jonathaxs.gymnutshell.ui.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jonathaxs.gymnutshell.core.domain.ProgressColors

/**
 * Anel de progresso do hero — porte de TodayProgressRingView (iOS), desenhado em Canvas.
 * Trilha de fundo + arco colorido começando no topo (-90°), com % no centro.
 * A cor segue ProgressColors (vermelho→laranja→verde→ciano→azul).
 */
@Composable
fun TodayProgressRing(
    progress: Float,
    percent: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 128.dp,
    strokeWidth: Dp = 12.dp,
) {
    val ringColor = Color(ProgressColors.ringArgb(progress.toDouble()))
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "ringProgress",
    )

    Box(modifier = modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            // Trilha de fundo (círculo completo)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // Arco de progresso (começa no topo)
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animated * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.titleLarge,
            color = ringColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
