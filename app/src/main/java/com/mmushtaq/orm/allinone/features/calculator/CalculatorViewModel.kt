package com.mmushtaq.orm.allinone.features.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.pow

enum class CalcOp { ADD, SUB, MUL, DIV }

data class CalculatorUiState(
    val display: String = "0",
    val expression: String = "",
    val isScientific: Boolean = false,
    val pendingValue: Double? = null,
    val pendingOp: CalcOp? = null,
    val isDegreeMode: Boolean = true,
    val newInput: Boolean = true
)

class CalculatorViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state

    fun toggleScientific() = _state.update { it.copy(isScientific = !it.isScientific) }
    fun toggleDegreeMode() = _state.update { it.copy(isDegreeMode = !it.isDegreeMode) }

    fun onDigit(d: String) {
        _state.update {
            val current = if (it.newInput || it.display == "0" || it.display == "Error") "" else it.display
            if (d == "." && current.contains(".")) return@update it
            it.copy(display = (current + d).ifEmpty { "0" }, newInput = false)
        }
    }

    fun onClear() {
        _state.update { CalculatorUiState(isScientific = it.isScientific, isDegreeMode = it.isDegreeMode) }
    }

    fun onBackspace() {
        _state.update {
            val d = it.display
            it.copy(display = if (d.length <= 1) "0" else d.dropLast(1))
        }
    }

    fun onToggleSign() {
        _state.update {
            val v = it.display.toDoubleOrNull() ?: return@update it
            it.copy(display = formatResult(-v))
        }
    }

    fun onOperator(op: CalcOp) {
        _state.update {
            val current = it.display.toDoubleOrNull() ?: 0.0
            val result = if (it.pendingOp != null && it.pendingValue != null && !it.newInput) {
                compute(it.pendingValue, current, it.pendingOp)
            } else current
            it.copy(
                pendingValue = result,
                pendingOp = op,
                expression = "${formatResult(result)} ${symbolFor(op)}",
                display = formatResult(result),
                newInput = true
            )
        }
    }

    fun onEquals() {
        _state.update {
            val current = it.display.toDoubleOrNull() ?: 0.0
            if (it.pendingOp == null || it.pendingValue == null) return@update it
            val result = compute(it.pendingValue, current, it.pendingOp)
            it.copy(display = formatResult(result), expression = "", pendingOp = null, pendingValue = null, newInput = true)
        }
    }

    fun onPercent() {
        _state.update {
            val current = it.display.toDoubleOrNull() ?: return@update it
            val base = it.pendingValue ?: current
            it.copy(display = formatResult(base * (current / 100.0)), newInput = true)
        }
    }

    fun onUnary(fn: (Double) -> Double) {
        _state.update {
            val v = it.display.toDoubleOrNull() ?: return@update it
            it.copy(display = formatResult(fn(v)), newInput = true)
        }
    }

    fun onTrig(fn: (Double) -> Double) {
        _state.update {
            val v = it.display.toDoubleOrNull() ?: return@update it
            val input = if (it.isDegreeMode) Math.toRadians(v) else v
            it.copy(display = formatResult(fn(input)), newInput = true)
        }
    }

    private fun compute(a: Double, b: Double, op: CalcOp): Double = when (op) {
        CalcOp.ADD -> a + b
        CalcOp.SUB -> a - b
        CalcOp.MUL -> a * b
        CalcOp.DIV -> if (b == 0.0) Double.NaN else a / b
    }

    private fun symbolFor(op: CalcOp) = when (op) {
        CalcOp.ADD -> "+"; CalcOp.SUB -> "−"; CalcOp.MUL -> "×"; CalcOp.DIV -> "÷"
    }

    private fun formatResult(v: Double): String {
        if (v.isNaN() || v.isInfinite()) return "Error"
        return if (v == v.toLong().toDouble() && abs(v) < 1e15) {
            v.toLong().toString()
        } else {
            "%.8f".format(v).trimEnd('0').trimEnd('.')
        }
    }
}