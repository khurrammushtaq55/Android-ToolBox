package com.mmushtaq.orm.allinone.core.widgets

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mmushtaq.orm.allinone.features.ruler.RulerUnit

@Composable
fun UnitToggle(unit: RulerUnit, onChange: (RulerUnit) -> Unit) {
    val options = listOf(RulerUnit.CM, RulerUnit.INCH)
    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { i, opt ->
            SegmentedButton(
                selected = unit == opt,
                onClick = { onChange(opt) },
                shape = SegmentedButtonDefaults.itemShape(i, options.size)
            ) {
                Text(if (opt == RulerUnit.CM) "cm" else "inch")
            }
        }
    }
}