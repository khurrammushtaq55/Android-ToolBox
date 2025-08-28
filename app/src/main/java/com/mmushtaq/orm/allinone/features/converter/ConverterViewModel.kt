package com.mmushtaq.orm.allinone.features.converter

import androidx.lifecycle.ViewModel
import com.mmushtaq.orm.allinone.core.ConverterEngine
import com.mmushtaq.orm.allinone.core.UnitCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

data class ConverterUiState(
    val category: UnitCategory = UnitCategory.LENGTH,
    val fromUnitKey: String = "cm",
    val toUnitKey: String = "inch",
    val inputText: String = "1",
    val result: String = "0",
    val searchFrom: String = "",
    val searchTo: String = "",
    val decimals: Int = 4
)

class ConverterViewModel : ViewModel() {

    private val _state = MutableStateFlow(ConverterUiState())
    val state: StateFlow<ConverterUiState> = _state

    fun setCategory(cat: UnitCategory) {
        val list = ConverterEngine.unitsByCategory[cat] ?: emptyList()
        val first = list.firstOrNull()?.key ?: return
        val second = list.getOrNull(1)?.key ?: first
        _state.update { it.copy(category = cat, fromUnitKey = first, toUnitKey = second) }
        recalc()
    }

    fun setFromUnit(key: String) {
        _state.update { it.copy(fromUnitKey = key) }
        recalc()
    }

    fun setToUnit(key: String) {
        _state.update { it.copy(toUnitKey = key) }
        recalc()
    }

    fun swap() {
        _state.update { it.copy(fromUnitKey = it.toUnitKey, toUnitKey = it.fromUnitKey) }
        recalc()
    }

    fun setInput(text: String) {
        _state.update { it.copy(inputText = text) }
        recalc()
    }

    fun setSearchFrom(q: String) { _state.update { it.copy(searchFrom = q) } }
    fun setSearchTo(q: String) { _state.update { it.copy(searchTo = q) } }

    fun setDecimals(d: Int) {
        _state.update { it.copy(decimals = d.coerceIn(0, 10)) }
        recalc()
    }

    private fun recalc() {
        val s = _state.value
        val v = s.inputText.toDoubleOrNull()
        val res = if (v != null) {
            val out = ConverterEngine.convert(s.category, v, s.fromUnitKey, s.toUnitKey)
            out.roundTo(s.decimals)
        } else ""
        _state.update { it.copy(result = res) }
    }

    private fun Double.roundTo(dec: Int): String {
        val factor = 10.0.pow(dec)
        val r = kotlin.math.round(this * factor) / factor
        // Avoid "-0.0"
        val normalized = if (kotlin.math.abs(r) < 1e-12) 0.0 else r
        return "%.${dec}f".format(normalized)
    }
}
