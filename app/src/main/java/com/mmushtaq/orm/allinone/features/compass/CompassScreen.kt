package com.mmushtaq.orm.allinone.features.compass

import android.content.Context
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mmushtaq.orm.allinone.R
import com.mmushtaq.orm.allinone.ads.BannerAd
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(vm: CompassViewModel = viewModel()) {
    val azMag by vm.azimuth
    val accuracy by vm.accuracy
    val ctx = LocalContext.current


// True North toggle state (persist later via DataStore if you like)
    var useTrueNorth by rememberSaveable { mutableStateOf(false) }


// Permission plumbing for COARSE location (enough for declination)
    val hasLocation = remember { mutableStateOf(hasCoarseLocation(ctx)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocation.value = granted
        if (granted) vm.refreshDeclination()
    }


// Apply user + declination offsets, wrap 0..360
    val appliedDeclination = if (useTrueNorth && hasLocation.value) vm.declinationOffset else 0f
    val heading = ((azMag + vm.userOffset + appliedDeclination) % 360f + 360f) % 360f


// Animate dial rotation for smooth UI
    val animated by animateFloatAsState(targetValue = heading, label = "needle")


    DisposableEffect(Unit) { vm.start(); onDispose { vm.stop() } }


    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Compass") })
    },
        bottomBar = {
            BannerAd(
            )
        }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))


// Dial
            CompassDial(
                degrees = animated,
                diameter = 280.dp,
                tickEvery = 15,
                majorEvery = 45
            )


// Readout + status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${heading.toInt()}°",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold
                )
                val accText = when (accuracy) {
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> "Low accuracy / interference"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Accuracy: Low"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Accuracy: Medium"
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Accuracy: High"
                    else -> "Accuracy: Unknown"
                }
                Text(
                    accText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }


// True North toggle + request permission if needed
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use True North")
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = useTrueNorth,
                        onCheckedChange = { checked ->
                            useTrueNorth = checked
                            if (checked && !hasLocation.value) {
                                permissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            } else if (checked && hasLocation.value) {
                                vm.refreshDeclination()
                            }
                        }
                    )
                }
                if (useTrueNorth && !hasLocation.value) {
                    Text(
                        "Allow approximate location to correct for magnetic declination.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


// Calibration actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = { vm.calibrateHere() }) { Text("Calibrate here") }
                OutlinedButton(onClick = { vm.clearCalibration() }) { Text("Reset") }
            }


// Status hint
            Text(
                text = when {
                    useTrueNorth && hasLocation.value && vm.declinationOffset.absoluteValue >= 0.1f ->
                        "Showing true north (declination ${"%+.1f".format(vm.declinationOffset)}°)"

                    useTrueNorth && !hasLocation.value ->
                        "Location not granted — showing magnetic north"

                    else -> "Showing magnetic north"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun hasCoarseLocation(ctx: Context): Boolean {
    return androidx.core.content.ContextCompat.checkSelfPermission(
        ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

@Composable
fun CompassDial(
    degrees: Float,
    diameter: Dp,
    tickEvery: Int = 15,
    majorEvery: Int = 45
) {
    val radiusPx = with(LocalDensity.current) { diameter.toPx() } / 2f
    val outlineColor = MaterialTheme. colorScheme.outline
    val onSurfaceColor = MaterialTheme. colorScheme.onSurface

    Canvas(modifier = Modifier.size(diameter)) {
// Outer ring
        drawCircle(
            color = outlineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )


// Ticks & labels
        for (d in 0 until 360 step tickEvery) {
            val isMajor = d % majorEvery == 0
            val len = if (isMajor) 18f else 10f
            val stroke = if (isMajor) 4f else 2f
            val angleRad = Math.toRadians(d.toDouble() - 90)
            val cos = cos(angleRad).toFloat()
            val sin = sin(angleRad).toFloat()
            val p1 = Offset(radiusPx + cos * (radiusPx - 8f), radiusPx + sin * (radiusPx - 8f))
            val p2 = Offset(
                radiusPx + cos * (radiusPx - 8f - len),
                radiusPx + sin * (radiusPx - 8f - len)
            )
            drawLine(
                color = onSurfaceColor,
                start = p1,
                end = p2,
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            if (isMajor) {
                val label = when (d) {
                    0 -> "E"; 90 -> "S"; 180 -> "W"; 270 -> "N"; else -> d.toString()
                }
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    isAntiAlias = true
                }
                val tw = paint.measureText(label)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    radiusPx + cos * (radiusPx - 42f) - tw / 2f,
                    radiusPx + sin * (radiusPx - 42f) + 10f,
                    paint
                )
            }
        }


// Rotating needle (degrees 0 = North). Dial rotates opposite to heading so red needle points to North.
        rotate(degrees = -degrees) {
            val needleLen = radiusPx - 24f
            drawLine(
                color = onSurfaceColor,
                start = Offset(radiusPx, radiusPx),
                end = Offset(radiusPx, radiusPx - needleLen),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.4f),
                start = Offset(radiusPx, radiusPx),
                end = Offset(radiusPx, radiusPx + needleLen * 0.45f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Preview
@Composable
private fun CompassScreenPreview() {
    CompassScreen()
}