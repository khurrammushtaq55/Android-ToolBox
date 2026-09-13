package com.mmushtaq.orm.allinone.features.stopwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
fun StopwatchTimerScreen(
    vm: StopwatchTimerViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val ui by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Stopwatch & Timer") }) }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            TabRow(selectedTabIndex = ui.tool.ordinal) {
                Tab(
                    selected = ui.tool == TimerTool.STOPWATCH,
                    onClick = { vm.selectTool(TimerTool.STOPWATCH) },
                    text = { Text("Stopwatch") }
                )
                Tab(
                    selected = ui.tool == TimerTool.TIMER,
                    onClick = { vm.selectTool(TimerTool.TIMER) },
                    text = { Text("Timer") }
                )
            }

            when (ui.tool) {
                TimerTool.STOPWATCH -> StopwatchBody(ui, vm)
                TimerTool.TIMER -> TimerBody(ui, vm)
            }
        }
    }
}

@Composable
private fun StopwatchBody(ui: StopwatchUiState, vm: StopwatchTimerViewModel) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = formatMs(ui.elapsedMs),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!ui.isRunning) {
                FilledTonalButton(onClick = vm::startStopwatch) {
                    Text(if (ui.elapsedMs == 0L) "Start" else "Resume")
                }
            } else {
                FilledTonalButton(onClick = vm::pauseStopwatch) { Text("Pause") }
            }
            OutlinedButton(onClick = vm::lap, enabled = ui.isRunning) { Text("Lap") }
            OutlinedButton(
                onClick = vm::resetStopwatch,
                enabled = ui.elapsedMs > 0L || ui.laps.isNotEmpty()
            ) { Text("Reset") }
        }
        Spacer(Modifier.height(16.dp))
        if (ui.laps.isNotEmpty()) {
            Text("Laps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(ui.laps.size) { i ->
                    val lapIndex = ui.laps.size - i
                    val lapMs = ui.laps[lapIndex - 1]
                    ListItem(
                        headlineContent = { Text("Lap $lapIndex") },
                        trailingContent = { Text(formatMs(lapMs)) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TimerBody(ui: StopwatchUiState, vm: StopwatchTimerViewModel) {
    val presets = listOf(30_000L, 60_000L, 5 * 60_000L, 10 * 60_000L)
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = formatMs(ui.timerRemainingMs, showMillis = false),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = if (ui.timerFinished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        if (ui.timerFinished) {
            Spacer(Modifier.height(8.dp))
            Text("Time's up!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { ms ->
                OutlinedButton(onClick = { vm.setTimerDuration(ms) }, enabled = !ui.isTimerRunning) {
                    Text(formatMs(ms, showMillis = false))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Slider(
            value = ui.timerTotalMs / 1000f,
            onValueChange = { vm.setTimerDuration((it * 1000).toLong()) },
            valueRange = 5f..3600f,
            enabled = !ui.isTimerRunning
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!ui.isTimerRunning) {
                FilledTonalButton(onClick = vm::startTimer, enabled = ui.timerRemainingMs > 0L) { Text("Start") }
            } else {
                FilledTonalButton(onClick = vm::pauseTimer) { Text("Pause") }
            }
            OutlinedButton(onClick = vm::resetTimer) { Text("Reset") }
        }
    }
}

@Preview
@Composable
fun StopwatchTimerScreenPreview() {
    StopwatchTimerScreen()
}