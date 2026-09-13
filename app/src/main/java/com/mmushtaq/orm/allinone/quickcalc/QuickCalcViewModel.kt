package com.mmushtaq.orm.allinone.features.quickcalc

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Period

enum class QuickCalcTool { PERCENTAGE, TIP, BMI, AGE }
enum class HeightUnit { CM, FT_IN }
enum class WeightUnit { KG, LB }

data class QuickCalcUiState(
    val tool: QuickCalcTool = QuickCalcTool.PERCENTAGE,
    val pctPercent: String = "15",
    val pctBase: String = "200",
    val tipBill: String = "50",
    val tipPercent: Float = 15f,
    val tipSplitCount: Int = 1,

    // BMI — height
    val heightUnit: HeightUnit = HeightUnit.CM,
    val bmiHeightCm: String = "170",
    val bmiHeightFt: String = "5",
    val bmiHeightIn: String = "7",

    // BMI — weight
    val weightUnit: WeightUnit = WeightUnit.KG,
    val bmiWeightKg: String = "70",
    val bmiWeightLb: String = "154",

    val ageBirthYear: String = "2000",
    val ageBirthMonth: String = "1",
    val ageBirthDay: String = "1"
)

class QuickCalcViewModel : ViewModel() {
    private val _state = MutableStateFlow(QuickCalcUiState())
    val state: StateFlow<QuickCalcUiState> = _state

    fun selectTool(t: QuickCalcTool) = _state.update { it.copy(tool = t) }

    fun setPctPercent(v: String) = _state.update { it.copy(pctPercent = v) }
    fun setPctBase(v: String) = _state.update { it.copy(pctBase = v) }

    fun setTipBill(v: String) = _state.update { it.copy(tipBill = v) }
    fun setTipPercent(v: Float) = _state.update { it.copy(tipPercent = v) }
    fun setTipSplit(count: Int) = _state.update { it.copy(tipSplitCount = count.coerceAtLeast(1)) }

    // ---- BMI: unit toggles ----
    fun setHeightUnit(u: HeightUnit) = _state.update { it.copy(heightUnit = u) }
    fun setWeightUnit(u: WeightUnit) = _state.update { it.copy(weightUnit = u) }

    fun setBmiHeightCm(v: String) = _state.update { it.copy(bmiHeightCm = v) }
    fun setBmiHeightFt(v: String) = _state.update { it.copy(bmiHeightFt = v) }
    fun setBmiHeightIn(v: String) = _state.update { it.copy(bmiHeightIn = v) }

    fun setBmiWeightKg(v: String) = _state.update { it.copy(bmiWeightKg = v) }
    fun setBmiWeightLb(v: String) = _state.update { it.copy(bmiWeightLb = v) }

    fun setAgeDate(y: String, m: String, d: String) =
        _state.update { it.copy(ageBirthYear = y, ageBirthMonth = m, ageBirthDay = d) }

    fun percentOfResult(): Double? {
        val p = _state.value.pctPercent.toDoubleOrNull() ?: return null
        val base = _state.value.pctBase.toDoubleOrNull() ?: return null
        return base * (p / 100.0)
    }

    fun tipAmount(): Double? {
        val bill = _state.value.tipBill.toDoubleOrNull() ?: return null
        return bill * (_state.value.tipPercent / 100.0)
    }

    fun tipTotalPerPerson(): Double? {
        val bill = _state.value.tipBill.toDoubleOrNull() ?: return null
        val tip = tipAmount() ?: return null
        return (bill + tip) / _state.value.tipSplitCount
    }

    /** Resolves current height input (in whichever unit is selected) to meters. */
    private fun heightInMeters(): Double? {
        val s = _state.value
        return when (s.heightUnit) {
            HeightUnit.CM -> {
                val cm = s.bmiHeightCm.toDoubleOrNull() ?: return null
                if (cm <= 0) null else cm / 100.0
            }
            HeightUnit.FT_IN -> {
                val ft = s.bmiHeightFt.toDoubleOrNull() ?: 0.0
                val inch = s.bmiHeightIn.toDoubleOrNull() ?: 0.0
                val totalInches = ft * 12.0 + inch
                if (totalInches <= 0) null else totalInches * 0.0254
            }
        }
    }

    /** Resolves current weight input (in whichever unit is selected) to kilograms. */
    private fun weightInKg(): Double? {
        val s = _state.value
        return when (s.weightUnit) {
            WeightUnit.KG -> s.bmiWeightKg.toDoubleOrNull()?.takeIf { it > 0 }
            WeightUnit.LB -> s.bmiWeightLb.toDoubleOrNull()?.takeIf { it > 0 }?.times(0.45359237)
        }
    }

    fun bmiValue(): Double? {
        val wKg = weightInKg() ?: return null
        val hM = heightInMeters() ?: return null
        return wKg / (hM * hM)
    }

    fun bmiCategory(bmi: Double): String = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }

    fun ageResult(): Period? {
        val s = _state.value
        val y = s.ageBirthYear.toIntOrNull() ?: return null
        val m = s.ageBirthMonth.toIntOrNull() ?: return null
        val d = s.ageBirthDay.toIntOrNull() ?: return null
        return try {
            val birth = LocalDate.of(y, m, d)
            val today = LocalDate.now()
            if (birth.isAfter(today)) null else Period.between(birth, today)
        } catch (_: Exception) {
            null
        }
    }
}