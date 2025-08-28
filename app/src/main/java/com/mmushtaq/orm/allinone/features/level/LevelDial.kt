package com.mmushtaq.orm.allinone.features.level

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs


@Composable
fun LevelDial(
    pitchDeg: Float,
    rollDeg: Float,
    diameter: Dp,
    thresholdDeg: Float,
    faceUp: Boolean,
    onLevelCrossed: (Boolean) -> Unit,
) {
    val radiusPx = with(LocalDensity.current) { diameter.toPx() } / 2f


// Map degrees → pixel offset (tunable sensitivity). Around ±30° maps to edge.
    val pxPerDeg = radiusPx * 0.9f / 30f


// Animated offsets for smooth UI
    val targetX = (-rollDeg * pxPerDeg).coerceIn(-radiusPx * 0.9f, radiusPx * 0.9f)
    val targetY = (pitchDeg * pxPerDeg).coerceIn(-radiusPx * 0.9f, radiusPx * 0.9f)
    val bubbleX by animateFloatAsState(
        targetX,
        label = "bubbleX",
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 180f)
    )
    val bubbleY by animateFloatAsState(
        targetY,
        label = "bubbleY",
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 180f)
    )


// Are we level within threshold?
    val isLevel = faceUp && abs(pitchDeg) <= thresholdDeg && abs(rollDeg) <= thresholdDeg
    LaunchedEffect(isLevel) { onLevelCrossed(isLevel) }


// Subtle gradient background ring
    val bg = Brush.radialGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            Color.Transparent
        )
    )

    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = Modifier
            .size(diameter)
            .background(bg)
    ) {
// Outer ring
        drawCircle(
            color = outlineColor.copy(alpha = 0.9f),
            style = Stroke(width = 4f)
        )
// Inner target ring
        drawCircle(
            color = outlineColor.copy(alpha = 0.5f),
            radius = radiusPx * 0.6f,
            style = Stroke(width = 2f)
        )
// Crosshair
        drawLine(
            color = outlineColor.copy(alpha = 0.6f),
            start = Offset(x = 0f + size.width / 2f, y = size.height * 0.05f),
            end = Offset(x = size.width / 2f, y = size.height * 0.95f),
            strokeWidth = 2f
        )
        drawLine(
            color = outlineColor.copy(alpha = 0.6f),
            start = Offset(x = size.width * 0.05f, y = size.height / 2f),
            end = Offset(x = size.width * 0.95f, y = size.height / 2f),
            strokeWidth = 2f
        )


// Bubble: keep inside ring
        // Bubble: keep inside ring
        val limit = radiusPx * 0.9f
        val bx =
            (size.width / 2f + bubbleX).coerceIn(size.width / 2f - limit, size.width / 2f + limit)
        val by = (size.height / 2f + bubbleY).coerceIn(
            size.height / 2f - limit,
            size.height / 2f + limit
        )
        val bubbleRadius = radiusPx * 0.12f
        drawCircle(
            color = if (isLevel) primaryColor else secondaryColor,
            radius = bubbleRadius,
            center = Offset(bx, by)
        )


// Center dot (goal)
        drawCircle(
            color = if (isLevel) primaryColor else surfaceColor.copy(alpha = 0.25f),
            radius = 6f,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}

@Preview
@Composable
fun BubbleLevelDialPreview() {
    LevelDial(
        pitchDeg = 0f,
        rollDeg = 0f,
        diameter = 280.dp,
        thresholdDeg = 0.8f,
        onLevelCrossed = {},
        faceUp = true
    )
}