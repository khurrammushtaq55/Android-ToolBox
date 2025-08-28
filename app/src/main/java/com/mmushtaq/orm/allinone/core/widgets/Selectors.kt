package com.mmushtaq.orm.allinone.core.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmushtaq.orm.allinone.core.UnitCategory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryRowDropDown(
    selected: UnitCategory,
    onSelect: (UnitCategory) -> Unit
) {
    val cats = UnitCategory.entries
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cats.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.name) },
                    onClick = {
                        onSelect(c)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Horizontal segmented category picker. */
@Composable
 fun CategoryRowHorizontal(selected: UnitCategory, onSelect: (UnitCategory) -> Unit) {
    val cats = UnitCategory.entries
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .padding(top = 8.dp, start = 12.dp, end = 12.dp)
            .fillMaxWidth()
    ) {
        cats.forEachIndexed { idx, c ->
            SegmentedButton(
                selected = selected == c,
                onClick = { onSelect(c) },
                shape = SegmentedButtonDefaults.itemShape(idx, cats.size),
                label = { Text(c.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}


