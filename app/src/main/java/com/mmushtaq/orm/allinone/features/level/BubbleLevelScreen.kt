package com.mmushtaq.orm.allinone.features.level


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelScreen(vm: LevelViewModel = viewModel()) {
    val haptics = LocalHapticFeedback.current

// Track “level” state to fire a single haptic when entering the window
    var wasLevel by remember { mutableStateOf(false) }
    val threshold = 0.8f // degrees within which we consider perfectly level


    DisposableEffect(Unit) { vm.start(); onDispose { vm.stop() } }


// Animate bubble position based on degrees → pixels mapping (done inside canvas)
    val pitch by remember { derivedStateOf { vm.pitchDeg } }
    val roll by remember { derivedStateOf { vm.rollDeg } }
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Bubble Level") }) }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))


            LevelDial(
                pitchDeg = pitch,
                rollDeg = roll,
                diameter = 280.dp,
                onLevelCrossed = { isNowLevel ->
                    if (isNowLevel && !wasLevel) {
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }
                    wasLevel = isNowLevel
                },
                faceUp = vm.faceUp,
                thresholdDeg = threshold
            )


// Readouts
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (vm.faceUp) "${pitch.toInt()}° / ${roll.toInt()}°" else "Hold device flat",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (vm.faceUp) "Pitch / Roll" else "Place the phone face‑up to use 2‑axis level",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


// Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = { vm.calibrateHere() }) { Text("Calibrate here") }
                OutlinedButton(onClick = { vm.resetCalibration() }) { Text("Reset") }
            }
        }
    }
}

@Preview
@Composable
fun BubbleScreenPreview() {
    LevelScreen()
}
