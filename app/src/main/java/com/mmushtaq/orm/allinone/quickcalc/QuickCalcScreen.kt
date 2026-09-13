package com.mmushtaq.orm.allinone.features.quickcalc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCalcScreen(
    vm: QuickCalcViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val ui by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Quick Calculators") }) }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = ui.tool.ordinal, edgePadding = 12.dp) {
                QuickCalcTool.entries.forEach { t ->
                    Tab(
                        selected = ui.tool == t,
                        onClick = { vm.selectTool(t) },
                        text = { Text(t.name.lowercase().replaceFirstChar { c -> c.uppercase() }) }
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                when (ui.tool) {
                    QuickCalcTool.PERCENTAGE -> PercentageBody(ui, vm)
                    QuickCalcTool.TIP -> TipBody(ui, vm)
                    QuickCalcTool.BMI -> BmiBody(ui, vm)
                    QuickCalcTool.AGE -> AgeBody(ui, vm)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(text: String) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PercentageBody(ui: QuickCalcUiState, vm: QuickCalcViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = ui.pctPercent, onValueChange = vm::setPctPercent,
            label = { Text("Percentage (%)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.pctBase, onValueChange = vm::setPctBase,
            label = { Text("Of value") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        val result = vm.percentOfResult()
        ResultCard("${ui.pctPercent}% of ${ui.pctBase} = " + (result?.let { "%.2f".format(it) } ?: "—"))
    }
}

@Composable
private fun TipBody(ui: QuickCalcUiState, vm: QuickCalcViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = ui.tipBill, onValueChange = vm::setTipBill,
            label = { Text("Bill amount") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Column {
            Text("Tip: ${ui.tipPercent.toInt()}%")
            Slider(value = ui.tipPercent, onValueChange = vm::setTipPercent, valueRange = 0f..30f, steps = 29)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Split between", Modifier.weight(1f))
            OutlinedButton(onClick = { vm.setTipSplit(ui.tipSplitCount - 1) }) { Text("−") }
            Text("${ui.tipSplitCount}", Modifier.padding(horizontal = 12.dp))
            OutlinedButton(onClick = { vm.setTipSplit(ui.tipSplitCount + 1) }) { Text("+") }
        }
        val tip = vm.tipAmount()
        val perPerson = vm.tipTotalPerPerson()
        ResultCard(
            "Tip: ${tip?.let { "%.2f".format(it) } ?: "—"}   •   Per person: ${perPerson?.let { "%.2f".format(it) } ?: "—"}"
        )
    }
}

@Composable
private fun BmiBody(ui: QuickCalcUiState, vm: QuickCalcViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Height section
        Text("Height", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = ui.heightUnit == HeightUnit.CM,
                onClick = { vm.setHeightUnit(HeightUnit.CM) },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text("cm") }
            SegmentedButton(
                selected = ui.heightUnit == HeightUnit.FT_IN,
                onClick = { vm.setHeightUnit(HeightUnit.FT_IN) },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text("ft / in") }
        }
        when (ui.heightUnit) {
            HeightUnit.CM -> OutlinedTextField(
                value = ui.bmiHeightCm, onValueChange = vm::setBmiHeightCm,
                label = { Text("Height (cm)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            HeightUnit.FT_IN -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ui.bmiHeightFt, onValueChange = vm::setBmiHeightFt,
                    label = { Text("Feet") }, singleLine = true, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ui.bmiHeightIn, onValueChange = vm::setBmiHeightIn,
                    label = { Text("Inches") }, singleLine = true, modifier = Modifier.weight(1f)
                )
            }
        }

        // Weight section
        Text("Weight", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = ui.weightUnit == WeightUnit.KG,
                onClick = { vm.setWeightUnit(WeightUnit.KG) },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text("kg") }
            SegmentedButton(
                selected = ui.weightUnit == WeightUnit.LB,
                onClick = { vm.setWeightUnit(WeightUnit.LB) },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text("lb") }
        }
        when (ui.weightUnit) {
            WeightUnit.KG -> OutlinedTextField(
                value = ui.bmiWeightKg, onValueChange = vm::setBmiWeightKg,
                label = { Text("Weight (kg)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            WeightUnit.LB -> OutlinedTextField(
                value = ui.bmiWeightLb, onValueChange = vm::setBmiWeightLb,
                label = { Text("Weight (lb)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
        }

        val bmi = vm.bmiValue()
        ResultCard(if (bmi != null) "BMI: %.1f (${vm.bmiCategory(bmi)})".format(bmi) else "BMI: —")
    }
}

@Composable
private fun AgeBody(ui: QuickCalcUiState, vm: QuickCalcViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = ui.ageBirthDay,
                onValueChange = { vm.setAgeDate(ui.ageBirthYear, ui.ageBirthMonth, it) },
                label = { Text("Day") }, singleLine = true, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = ui.ageBirthMonth,
                onValueChange = { vm.setAgeDate(ui.ageBirthYear, it, ui.ageBirthDay) },
                label = { Text("Month") }, singleLine = true, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = ui.ageBirthYear,
                onValueChange = { vm.setAgeDate(it, ui.ageBirthMonth, ui.ageBirthDay) },
                label = { Text("Year") }, singleLine = true, modifier = Modifier.weight(1.3f)
            )
        }
        val age = vm.ageResult()
        ResultCard(
            if (age != null) "${age.years} years, ${age.months} months, ${age.days} days"
            else "Enter a valid birth date"
        )
    }
}

@Preview
@Composable
fun QuickCalcScreenPreview() {
    QuickCalcScreen()
}