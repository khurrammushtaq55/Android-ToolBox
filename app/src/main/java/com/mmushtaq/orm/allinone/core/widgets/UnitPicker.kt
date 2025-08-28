package com.mmushtaq.orm.allinone.core.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mmushtaq.orm.allinone.core.ConverterEngine
import com.mmushtaq.orm.allinone.core.UnitCategory
import kotlin.collections.orEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPicker(
    title: String,
    category: UnitCategory,
    selectedKey: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val units = ConverterEngine.unitsByCategory[category].orEmpty()
    val filtered = units.filter {
        it.label.contains(query, ignoreCase = true) ||
                it.symbol.contains(query, ignoreCase = true) ||
                it.key.contains(query, ignoreCase = true)
    }
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search unit") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = false, // simple static box look; no popup needed since we show a filtered list below
            onExpandedChange = {}
        ) {
            OutlinedTextField(
                value = units.firstOrNull { it.key == selectedKey }
                    ?.let { "${it.label} (${it.symbol})" } ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { /* noop */ },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth()
            )
        }
        Spacer(Modifier.height(6.dp))
        // Simple list of top 4 matches to keep UI compact
        filtered.take(4).forEach { u ->
            TextButton(onClick = { onSelect(u.key) }) {
                Text("${u.label} (${u.symbol})")
            }
        }
    }
}


@Preview
@Composable
fun UnitPickerPreview()
{
    UnitPicker(
        title = "From",
        category = UnitCategory.LENGTH,
        selectedKey = "m",
        query = "m",
        onQueryChange = {},
        onSelect = {}
    )
}