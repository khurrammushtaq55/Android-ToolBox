package com.mmushtaq.orm.allinone.features.ruler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun CalibrationCard(
    unit: RulerUnit,
    scale: Float,
    onScale: (Float) -> Unit,
    onReset: () -> Unit
) {
    Card(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Quick Calibration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Place a real ${if (unit == RulerUnit.CM) "10 cm" else "4 inch"} object on screen and adjust until ticks match.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Scale", Modifier.width(56.dp), textAlign = TextAlign.Start)
                Slider(
                    value = scale,
                    onValueChange = onScale,
                    valueRange = 0.8f..1.2f,
                    steps = 8,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(scale * 100).toInt()}%",
                    Modifier.widthIn(min = 48.dp),
                    textAlign = TextAlign.End
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onReset) { Text("Reset") }
            }
        }
    }
}

@Preview
@Composable
fun previewCalliboration(){
    CalibrationCard(
        unit = RulerUnit.CM,
        scale = 1f,
        onScale = {},
        onReset = {}
    )
}