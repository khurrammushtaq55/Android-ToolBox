package com.mmushtaq.orm.allinone.features.sound

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun Waveform(samples: List<Float>) {
    if (samples.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data yet…")
        }
        return
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surfaceVariant)) {
        val midY = size.height / 2f
        val stepX = size.width / (samples.size - 1).coerceAtLeast(1)
        var x = 0f
        for (i in 1 until samples.size) {
            val y1 = midY - (samples[i - 1] * (midY * 0.9f))
            val y2 = midY - (samples[i] * (midY * 0.9f))
            drawLine(
                color = primaryColor,
                start = Offset(x, y1),
                end = Offset(x + stepX, y2),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            x += stepX
        }
    }
}


@Preview
@Composable
fun PreviewWaveform()
{
    val samples = List(5) { it.toFloat() }
    Waveform(samples = samples)

}