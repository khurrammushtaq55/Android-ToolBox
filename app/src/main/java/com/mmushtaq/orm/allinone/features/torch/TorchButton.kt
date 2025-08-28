package com.mmushtaq.orm.allinone.features.torch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TorchBigButton(
    enabled: Boolean,
    isOn: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isOn) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 150f),
        label = "torch-scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp) // give the glow room to breathe
            .drawBehind {
                // Centered radial glow
                val center = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension * 0.5f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isOn) primaryColor.copy(alpha = 0.45f) else Color.Transparent,
                            Color.Transparent
                        ),
                        center = center,
                        radius = r
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // The button
        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale } // actually use the scale
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isOn) listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ) else listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = CircleShape
                )
                .then(if (enabled) Modifier else Modifier.alpha(0.6f))
                .clickable(enabled = enabled, onClick = onClick)
                .semantics {
                    contentDescription = if (isOn) "Turn flashlight off" else "Turn flashlight on"
                }
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.FlashlightOn,
                    contentDescription = null,
                    tint = if (isOn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isOn) "Tap to turn OFF" else "Tap to turn ON",
                    color = if (isOn) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewTorchButton()
{
    TorchBigButton(
        enabled = true,
        isOn = true,
        onClick = {}
    )
}