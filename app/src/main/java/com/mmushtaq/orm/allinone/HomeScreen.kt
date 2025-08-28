package com.mmushtaq.orm.allinone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpen: (String) -> Unit) {
    val tools = remember {
        listOf(
            Tool("Flashlight",  "Instant torch control",   route = "torch",     icon = Icons.Rounded.FlashlightOn),
            Tool("Compass",     "Magnetic / True North",   route = "compass",   icon = Icons.Rounded.Explore),
            Tool("Bubble Level","2-axis inclinometer",     route = "level",     icon = Icons.Rounded.BubbleChart),
            Tool("Ruler",       "Screen ruler + calibrate",route = "ruler",     icon = Icons.Rounded.Straighten),
            Tool("Sound Meter", "Relative dB meter",       route = "sound",     icon = Icons.Rounded.GraphicEq),
            Tool("Converter",   "Quick unit converter",    route = "converter", icon = Icons.Rounded.SwapHoriz),
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("All-in-One Toolbox", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            HeroHeader(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(tools) { index, tool ->
                    ToolCard(tool = tool, index = index) { onOpen(tool.route) }
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        shape = shape,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(gradient, shape)
                .padding(18.dp)
        ) {
            Column(Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = "Handy tools, zero clutter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Flashlight • Compass • Level • Ruler • Sound • Converter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
                )
            }
        }
    }
}

@Composable
private fun ToolCard(tool: Tool, index: Int, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(24.dp)

    val grad = when (index % 3) {
        0 -> Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
                MaterialTheme.colorScheme.primaryContainer
            )
        )
        1 -> Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.40f),
                MaterialTheme.colorScheme.secondaryContainer
            )
        )
        else -> Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.40f),
                MaterialTheme.colorScheme.tertiaryContainer
            )
        )
    }

    Surface(
        modifier = Modifier
            .aspectRatio(1.04f)
            .background(Color.Transparent)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = shape,
        tonalElevation = 2.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(grad)
                .padding(16.dp)
        ) {
            // Icon bubble
            Surface(shape = CircleShape) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = tool.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Titles
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(tool.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    tool.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class Tool(
    val title: String,
    val subtitle: String,
    val route: String,
    val icon: ImageVector
)


@Preview
@Composable
private fun ToolCardPreview() {
   HomeScreen {  }
}