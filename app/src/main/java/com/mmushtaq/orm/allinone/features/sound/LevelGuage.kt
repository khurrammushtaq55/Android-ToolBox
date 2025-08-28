package com.mmushtaq.orm.allinone.features.sound

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt


@Composable
fun LevelGauge(
    modifier: Modifier = Modifier,
    valueDb: Float,
    peakDb: Float
) {
    // Map dB [-90 .. +6] to sweep [0..1]
    fun map(db: Float): Float {
        val clamped = db.coerceIn(-90f, 6f)
        return (clamped + 90f) / 96f
    }

    val animValue = animateFloatAsState(targetValue = map(valueDb), label = "rmsAnim")
    val animPeak = animateFloatAsState(targetValue = map(peakDb), label = "peakAnim")

    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier
            .fillMaxSize()
            .padding(24.dp)) {
            val stroke = 22f
            val startAngle = 150f
            val sweepMax = 240f
            val radius = min(size.width, size.height) / 2f

            // Track
            drawArc(
                color = surfaceColor,
                startAngle = startAngle,
                sweepAngle = sweepMax,
                useCenter = false,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                style = Stroke(width = stroke)
            )

            // Peak arc
            drawArc(
                color = secondaryColor,
                startAngle = startAngle,
                sweepAngle = sweepMax * animPeak.value,
                useCenter = false,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // RMS arc
            drawArc(
                color = primaryColor,
                startAngle = startAngle,
                sweepAngle = sweepMax * animValue.value,
                useCenter = false,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${valueDb.roundToInt()} dB",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text("Peak ${peakDb.roundToInt()} dB", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
fun PreviewLevelGauge()
{
    LevelGauge(valueDb = 10f, peakDb = 15f)

}