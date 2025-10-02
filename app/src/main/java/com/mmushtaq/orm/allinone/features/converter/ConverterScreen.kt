package com.mmushtaq.orm.allinone.features.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mmushtaq.orm.allinone.R
import com.mmushtaq.orm.allinone.ads.BannerAd
import com.mmushtaq.orm.allinone.core.ConverterEngine
import com.mmushtaq.orm.allinone.core.UnitCategory
import com.mmushtaq.orm.allinone.core.widgets.CategoryRowDropDown
import com.mmushtaq.orm.allinone.core.widgets.CategoryRowHorizontal
import com.mmushtaq.orm.allinone.core.widgets.UnitPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    vm: ConverterViewModel = viewModel(),
    onBack: (() -> Unit)? = null
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Provide starter units for the initial category
    LaunchedEffect(Unit) { vm.setCategory(ui.category) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Converter") })
        },
        bottomBar = {
            BannerAd(
            )
        })
    { inner ->

        val list = ConverterEngine.unitsByCategory[ui.category].orEmpty()
        val inputVal = ui.inputText.toDoubleOrNull()
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                ResponsiveCategoryRow(
                    selected = ui.category,
                    onSelect = vm::setCategory
                )
            }

            item {
                Card(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = ui.inputText,
                            onValueChange = vm::setInput,
                            label = { Text("Value") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UnitPicker(
                                title = "From",
                                category = ui.category,
                                selectedKey = ui.fromUnitKey,
                                query = ui.searchFrom,
                                onQueryChange = vm::setSearchFrom,
                                onSelect = vm::setFromUnit,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = vm::swap,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) { Icon(Icons.Default.SwapHoriz, contentDescription = "Swap") }
                            UnitPicker(
                                title = "To",
                                category = ui.category,
                                selectedKey = ui.toUnitKey,
                                query = ui.searchTo,
                                onQueryChange = vm::setSearchTo,
                                onSelect = vm::setToUnit,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        ResultRow(
                            result = ui.result,
                            unit = ConverterEngine.units.firstOrNull { it.key == ui.toUnitKey }?.symbol
                                ?: "",
                            onCopy = { copyToClipboard(context, ui.result) }
                        )

                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Decimals", style = MaterialTheme.typography.bodyMedium)
                            Slider(
                                value = ui.decimals.toFloat(),
                                onValueChange = { vm.setDecimals(it.toInt()) },
                                valueRange = 0f..8f,
                                steps = 7,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            )
                            Text("${ui.decimals}")
                        }
                    }
                }
            }

            item {
                Text(
                    "All units in ${
                        ui.category.name.lowercase().replaceFirstChar { it.uppercase() }
                    }",
                    Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
            }

            // The table
            items(list) { target ->
                val res = inputVal?.let {
                    ConverterEngine.convert(ui.category, it, ui.fromUnitKey, target.key)
                }
                ConversionRow(
                    label = "${target.label} (${target.symbol})",
                    value = res?.let { "%.${ui.decimals}f".format(it) } ?: "",
                    onCopy = { res?.let { copyToClipboard(context, it.toString()) } }
                )
                Divider()
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
fun ResponsiveCategoryRow(selected: UnitCategory, onSelect: (UnitCategory) -> Unit) {
    BoxWithConstraints {
        if (maxWidth < 560.dp) {
            // very small screens
            CategoryRowDropDown(selected, onSelect) // Option C extracted
        } else {
            CategoryRowHorizontal(selected, onSelect)    // Option A extracted
        }
    }
}

@Composable
private fun ResultRow(result: String, unit: String, onCopy: () -> Unit) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (result.isBlank()) "—" else "$result $unit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
            }
        }
    }
}

@Composable
private fun ConversionRow(label: String, value: String, onCopy: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(if (value.isBlank()) "—" else value) },
        trailingContent = {
            IconButton(onClick = onCopy, enabled = value.isNotBlank()) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
            }
        }
    )
    HorizontalDivider(thickness = 2.dp)
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("conversion", text))
}


@Preview
@Composable
private fun ConverterScreenPreview() {
    ConverterScreen()
}