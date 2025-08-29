package com.mmushtaq.orm.allinone.features.ruler

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mmushtaq.orm.allinone.R
import com.mmushtaq.orm.allinone.ads.BannerAd
import com.mmushtaq.orm.allinone.core.widgets.UnitToggle
import com.mmushtaq.orm.allinone.core.widgets.format
import com.mmushtaq.orm.allinone.core.widgets.pxToUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulerScreen(
    modifier: Modifier = Modifier,
    viewModel: RulerViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()

    // Provide real device DPI
    val config = LocalConfiguration.current
    LaunchedEffect(config.densityDpi) {
        viewModel.setDensityDpi(config.densityDpi)
    }

    val scroll = rememberScrollState()
    val density = LocalDensity.current

    // Visual sizing
    val trackHeight = 140.dp
    val contentWidthDp = with(density) { ui.contentWidthPx.toDp() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ruler") })
        },
        bottomBar = {
            BannerAd(
            )
        }) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Live readout row
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth().padding(10.dp)
            )
            {
                UnitToggle(unit = ui.unit, onChange = viewModel::setUnit)
            }
            ReadoutRow(
                unit = ui.unit,
                start = pxToUnit(ui.startMarkerPx - ui.zeroOffsetPx, ui),
                end = pxToUnit(ui.endMarkerPx - ui.zeroOffsetPx, ui)
            )

            // The ruler itself (scrollable)
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(trackHeight + 56.dp)
            ) {
                // Scrollable canvas
                Box(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scroll)
                ) {
                    RulerCanvas(
                        width = contentWidthDp,
                        height = trackHeight,
                        scrollX = with(density) { scroll.value.toFloat() },
                        ui = ui
                    )
                }

                // Two draggable markers (absolute in content space; visually offset by scroll)
                Marker(
                    label = "A",
                    xPx = ui.startMarkerPx,
                    scrollX = scroll.value.toFloat(),
                    colorAlpha = 1f
                ) { delta ->
                    viewModel.setMarkers(
                        startPx = (ui.startMarkerPx + delta).coerceIn(
                            0f,
                            ui.contentWidthPx
                        )
                    )
                }

                Marker(
                    label = "B",
                    xPx = ui.endMarkerPx,
                    scrollX = scroll.value.toFloat(),
                    colorAlpha = 0.85f
                ) { delta ->
                    viewModel.setMarkers(
                        endPx = (ui.endMarkerPx + delta).coerceIn(
                            0f,
                            ui.contentWidthPx
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calibration card
            CalibrationCard(
                unit = ui.unit,
                scale = ui.scaleFactor,
                onScale = viewModel::setScaleFactor,
                onReset = viewModel::reset
            )

            Spacer(Modifier.height(8.dp))

            AssistanceText()
        }
    }

    LaunchedEffect(ui.startMarkerPx, ui.endMarkerPx) { viewModel.ensureMarkerBounds() }
}

@Composable
private fun ReadoutRow(unit: RulerUnit, start: Float, end: Float) {
    val dist = (end - start)
    val (label, decimals) = when (unit) {
        RulerUnit.CM -> "cm" to 2
        RulerUnit.INCH -> "in" to 3
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Start: ${start.format(decimals)} $label",
            style = MaterialTheme.typography.titleMedium
        )
        Text("End: ${end.format(decimals)} $label", style = MaterialTheme.typography.titleMedium)
        Text(
            "Δ: ${dist.format(decimals)} $label",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}



@Composable
private fun AssistanceText() {
    Text(
        "Tip: Drag markers A and B to measure. Swipe horizontally to explore more length. Use calibration if your phone’s DPI is slightly off.",
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun RulerCanvas(
    width: Dp,
    height: Dp,
    scrollX: Float,
    ui: RulerUiState
) {
    val density = LocalDensity.current
    val bg = MaterialTheme.colorScheme.surface
    val tick = MaterialTheme.colorScheme.onSurface
    val textStyle = MaterialTheme.typography.labelLarge

    val pxPerStep: Float
    val minorSteps: Int
    val labelEvery: Int

    when (ui.unit) {
        RulerUnit.CM -> {
            // Draw major at 1 cm, mid at 0.5 cm, minor at 0.1 cm
            pxPerStep =
                ui.pxPerCm * 0.1f * ui.scaleFactor / ui.scaleFactor  // (already applied in pxPerUnit)
            minorSteps = 10
            labelEvery = 10 // label every 1 cm (10 x 0.1)
        }

        RulerUnit.INCH -> {
            // Major at 1", mid at 1/2, quarter & eighth minors
            pxPerStep = ui.pxPerInch / 8f // 1/8" granularity
            minorSteps = 8
            labelEvery = 8 // label every 1"
        }
    }

    val canvasWidthPx = with(density) { width.toPx() }
    val canvasHeightPx = with(density) { height.toPx() }

    // Visible range in the content space:
    val leftPx = scrollX
    val rightPx = scrollX + canvasWidthPx

    // Start/end index for tick generation (content space, step-based)
    val startIndex = floor(((leftPx - ui.zeroOffsetPx) / pxPerStep) - 2).toInt()
    val endIndex = ceil(((rightPx - ui.zeroOffsetPx) / pxPerStep) + 2).toInt()

    Canvas(
        Modifier
            .width(width)
            .height(height)
            .background(bg)
    ) {
        // Baseline
        drawLine(
            color = tick,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f
        )

        for (i in startIndex..endIndex) {
            val x = ui.zeroOffsetPx + i * pxPerStep - leftPx
            if (x < -20f || x > size.width + 20f) continue

            val (h, thick) = when (ui.unit) {
                RulerUnit.CM -> {
                    // i % 10 == 0 -> 1cm; i % 5 == 0 -> 0.5cm; else 0.1cm
                    when {
                        i % 10 == 0 -> 0.75f to 3f
                        i % 5 == 0 -> 0.55f to 2.5f
                        else -> 0.35f to 2f
                    }
                }

                RulerUnit.INCH -> {
                    // i % 8 == 0 -> 1"; %4 -> 1/2"; %2 -> 1/4"; else 1/8"
                    when {
                        i % 8 == 0 -> 0.75f to 3f
                        i % 4 == 0 -> 0.6f to 2.5f
                        i % 2 == 0 -> 0.45f to 2.2f
                        else -> 0.32f to 2f
                    }
                }
            }

            val yTop = size.height * (1f - h)
            drawLine(
                color = tick,
                start = Offset(x, yTop),
                end = Offset(x, size.height),
                strokeWidth = thick
            )

            // Labels on major ticks
            if (i % labelEvery == 0) {
                val value = i / labelEvery
                val label = when (ui.unit) {
                    RulerUnit.CM -> value.toString()
                    RulerUnit.INCH -> value.toString()
                }
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(200, 0, 0, 0)
                        textSize = 32f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    drawText(label, x, max(24f, yTop - 8f), paint)
                }
            }
        }
    }
}

@Composable
private fun Marker(
    label: String,
    xPx: Float,
    scrollX: Float,
    colorAlpha: Float,
    onDragDeltaPx: (Float) -> Unit
) {
    val density = LocalDensity.current
    val dragState = rememberDraggableState { delta -> onDragDeltaPx(delta) }

    val visibleX = xPx - scrollX
    val xDp = with(density) { visibleX.toDp() }

    Box(
        Modifier
            .fillMaxHeight()
            .padding(top = 8.dp)
            .offset { IntOffset(xDp.roundToPx(), 0) }
            .width(28.dp)
            .draggable(
                orientation = Orientation.Horizontal,
                state = dragState
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Head
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = colorAlpha),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 28.dp)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    label,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // Stem
        Spacer(
            Modifier
                .padding(top = 28.dp)
                .width(3.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = colorAlpha))
        )
    }
}

@Preview
@Composable
fun RulerScreenPreview() {
    RulerScreen()
}