package com.mmushtaq.orm.allinone.features.ruler

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class RulerUnit { CM, INCH }

data class RulerUiState(
    val densityDpi: Int = 440,             // fallback, will be replaced by runtime value
    val unit: RulerUnit = RulerUnit.CM,
    val scaleFactor: Float = 1f,           // quick calibration multiplier (0.8f..1.2f)
    val zeroOffsetPx: Float = 0f,          // if you want to nudge 0 alignment
    val contentWidthPx: Float = 6000f,     // virtual ruler length for scrolling
    val startMarkerPx: Float = 200f,       // absolute X in the content space (px)
    val endMarkerPx: Float = 800f
) {
    val pxPerInch: Float get() = densityDpi.toFloat()
    val pxPerCm: Float get() = pxPerInch / 2.54f

    val pxPerUnit: Float
        get() = when (unit) {
            RulerUnit.CM -> pxPerCm * scaleFactor
            RulerUnit.INCH -> pxPerInch * scaleFactor
        }
}

class RulerViewModel : ViewModel() {

    private val _state = MutableStateFlow(RulerUiState())
    val state: StateFlow<RulerUiState> = _state

    fun setDensityDpi(dpi: Int) = _state.update { it.copy(densityDpi = dpi.coerceAtLeast(120)) }

    fun setUnit(unit: RulerUnit) = _state.update { it.copy(unit = unit) }

    fun setScaleFactor(scale: Float) = _state.update { it.copy(scaleFactor = scale.coerceIn(0.8f, 1.2f)) }

    fun nudgeZeroOffset(pxDelta: Float) = _state.update { it.copy(zeroOffsetPx = it.zeroOffsetPx + pxDelta) }

    fun setMarkers(startPx: Float? = null, endPx: Float? = null) = _state.update {
        it.copy(
            startMarkerPx = startPx ?: it.startMarkerPx,
            endMarkerPx = endPx ?: it.endMarkerPx
        )
    }

    fun ensureMarkerBounds() = _state.update {
        val cw = it.contentWidthPx
        it.copy(
            startMarkerPx = it.startMarkerPx.coerceIn(0f, cw),
            endMarkerPx = it.endMarkerPx.coerceIn(0f, cw)
        )
    }

    fun reset() = _state.update { it.copy(scaleFactor = 1f, zeroOffsetPx = 0f) }
}
