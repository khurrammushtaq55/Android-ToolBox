package com.mmushtaq.orm.allinone.features.torch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PermissionCard(text: String, onGrant: () -> Unit) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(text)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGrant) { Text("Grant permission") }
        }
    }
}

@Preview
@Composable
private fun PermissionCardPreview() {
    PermissionCard("Grant camera permission to control the flashlight.") {}
}