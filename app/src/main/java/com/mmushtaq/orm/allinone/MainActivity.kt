package com.mmushtaq.orm.allinone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmushtaq.orm.allinone.features.compass.CompassScreen
import com.mmushtaq.orm.allinone.features.converter.ConverterScreen
import com.mmushtaq.orm.allinone.features.level.LevelScreen
import com.mmushtaq.orm.allinone.features.ruler.RulerScreen
import com.mmushtaq.orm.allinone.features.sound.SoundScreen
import com.mmushtaq.orm.allinone.features.torch.TorchScreen

// --- Routes for navigation ---
object Routes {
    const val HOME = "home"
    const val TORCH = "torch"
    const val COMPASS = "compass"
    const val LEVEL = "level"
    const val RULER = "ruler"
    const val SOUND = "sound"
    const val CONVERTER = "converter"
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind system bars (status/navigation) for modern look
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Let window insets flow to Compose
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    // If you have your own theme, replace with ToolboxTheme {}
    MaterialTheme {
        // Optionally adjust system bar icon contrast based on theme
        SideEffect {
            // Nothing required here for now; keep for future tweaks
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        addHome(navController)
        addTorch()
        addCompass()
        addLevel()
        addRuler()
        addSound()
        addConverter()
    }
}

// --- Nav graph split into small helpers for readability ---

private fun NavGraphBuilder.addHome(navController: NavHostController) {
    composable(Routes.HOME) {
        HomeScreen(onOpen = { route -> navController.navigate(route) })
    }
}

private fun NavGraphBuilder.addTorch() {
    composable(Routes.TORCH) {
        TorchScreen()
    }
}

private fun NavGraphBuilder.addCompass() {
    composable(Routes.COMPASS) {
        CompassScreen()
    }
}

private fun NavGraphBuilder.addLevel() {
    composable(Routes.LEVEL) {
        LevelScreen()
    }
}

private fun NavGraphBuilder.addRuler() {
    composable(Routes.RULER) {
         RulerScreen()
    }
}

private fun NavGraphBuilder.addSound() {
    composable(Routes.SOUND) {
        SoundScreen()
    }
}

private fun NavGraphBuilder.addConverter() {
    composable(Routes.CONVERTER) {
         ConverterScreen()
    }
}
