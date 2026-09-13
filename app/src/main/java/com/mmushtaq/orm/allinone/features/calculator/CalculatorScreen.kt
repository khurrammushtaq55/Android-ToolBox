package com.mmushtaq.orm.allinone.features.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    vm: CalculatorViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val ui by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calculator") },
                actions = {
                    TextButton(onClick = vm::toggleScientific) {
                        Text(if (ui.isScientific) "Basic" else "Scientific")
                    }
                }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize().padding(12.dp)) {
            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (ui.expression.isNotBlank()) {
                    Text(
                        ui.expression,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    ui.display,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(8.dp))

            if (ui.isScientific) {
                ScientificRow(vm, ui)
                Spacer(Modifier.height(8.dp))
            }

            CalculatorGrid(vm)
        }
    }
}

@Composable
private fun ScientificRow(vm: CalculatorViewModel, ui: CalculatorUiState) {
    val buttons: List<Pair<String, () -> Unit>> = listOf(
        "sin" to { vm.onTrig { x -> sin(x) } },
        "cos" to { vm.onTrig { x -> cos(x) } },
        "tan" to { vm.onTrig { x -> tan(x) } },
        "√" to { vm.onUnary { x -> sqrt(x) } },
        "x²" to { vm.onUnary { x -> x * x } },
        "log" to { vm.onUnary { x -> log10(x) } },
        "ln" to { vm.onUnary { x -> ln(x) } },
        "1/x" to { vm.onUnary { x -> 1.0 / x } },
        "π" to { vm.onUnary { _ -> PI } },
        (if (ui.isDegreeMode) "DEG" else "RAD") to { vm.toggleDegreeMode() },
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier.height(96.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(buttons) { (label, action) ->
            OutlinedButton(onClick = action, modifier = Modifier.fillMaxWidth()) {
                Text(label, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CalcButton(label: String, isAccent: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (isAccent) {
        Button(onClick = onClick, modifier = modifier.aspectRatio(1.3f)) {
            Text(label, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
    } else {
        FilledTonalButton(onClick = onClick, modifier = modifier.aspectRatio(1.3f)) {
            Text(label, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CalculatorGrid(vm: CalculatorViewModel) {
    val rows: List<List<Triple<String, () -> Unit, Boolean>>> = listOf(
        listOf(
            Triple("C", { vm.onClear() }, false),
            Triple("⌫", { vm.onBackspace() }, false),
            Triple("%", { vm.onPercent() }, false),
            Triple("÷", { vm.onOperator(CalcOp.DIV) }, true),
        ),
        listOf(
            Triple("7", { vm.onDigit("7") }, false),
            Triple("8", { vm.onDigit("8") }, false),
            Triple("9", { vm.onDigit("9") }, false),
            Triple("×", { vm.onOperator(CalcOp.MUL) }, true),
        ),
        listOf(
            Triple("4", { vm.onDigit("4") }, false),
            Triple("5", { vm.onDigit("5") }, false),
            Triple("6", { vm.onDigit("6") }, false),
            Triple("−", { vm.onOperator(CalcOp.SUB) }, true),
        ),
        listOf(
            Triple("1", { vm.onDigit("1") }, false),
            Triple("2", { vm.onDigit("2") }, false),
            Triple("3", { vm.onDigit("3") }, false),
            Triple("+", { vm.onOperator(CalcOp.ADD) }, true),
        ),
        listOf(
            Triple("±", { vm.onToggleSign() }, false),
            Triple("0", { vm.onDigit("0") }, false),
            Triple(".", { vm.onDigit(".") }, false),
            Triple("=", { vm.onEquals() }, true),
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, action, accent) ->
                    CalcButton(label, accent, action, Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview
@Composable
fun CalculatorScreenPreview() {
    CalculatorScreen()
}