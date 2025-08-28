package com.mmushtaq.orm.allinone.features.sound

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundScreen(
    modifier: Modifier = Modifier,
    viewModel: SoundViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {


    val ui by viewModel.state.collectAsStateWithLifecycle()
    var hasMicPermission by remember { mutableStateOf(false) }
    val permission = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.RECORD_AUDIO else Manifest.permission.RECORD_AUDIO

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasMicPermission = granted }

    LaunchedEffect(Unit) {
        // Fire the permission request immediately when entering screen
        launcher.launch(permission)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sound Meter") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (hasMicPermission) viewModel.toggle() else launcher.launch(permission)
                },
                icon = {
                    Icon(
                        if (ui.isMeasuring) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null
                    )
                },
                text = { Text(if (ui.isMeasuring) "Stop" else "Start") }
            )
        }
    ) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Big gauge
            LevelGauge(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height(220.dp),
                valueDb = ui.rmsDb,
                peakDb = ui.peakDb
            )

            // Readouts
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill("RMS", "${ui.rmsDb.roundToInt()} dB")
                StatPill("Peak", "${ui.peakDb.roundToInt()} dB")
                StatPill("Status", ui.statusMsg)
            }

            // Waveform
            Card(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Waveform(ui.waveform)
            }

            // Calibration
            Card(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Calibration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Use this to nudge readings to match a known source (e.g., a 60 dB conversation). " +
                                "This is a relative meter; values are approximate.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${ui.calibrationOffset.roundToInt()} dB", Modifier.width(56.dp))
                        Slider(
                            value = ui.calibrationOffset,
                            onValueChange = { viewModel.setCalibration(it) },
                            valueRange = -20f..20f,
                            steps = 0,
                            modifier = Modifier.weight(1f)
                        )
                        Text("±20 dB")
                    }
                }
            }

            // Tips
            Text(
                text = "Tip: Hold the phone still, keep the mic unobstructed, and avoid touching the bottom mic for steadier readings.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
