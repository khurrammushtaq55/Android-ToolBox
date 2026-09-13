package com.mmushtaq.orm.allinone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        // enableEdgeToEdge() alone handles decorFitsSystemWindows(false) AND
        // sets correct system bar contrast enforcement for the current theme.
        // Do NOT also call WindowCompat.setDecorFitsSystemWindows manually —
        // the duplicate call is what triggers the "may not display for all
        // users" warning on API 35+.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    MaterialTheme {
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

private fun NavGraphBuilder.addHome(navController: NavHostController) {
    composable(Routes.HOME) {
        HomeScreen(onOpen = { route -> navController.navigate(route) })
    }
}

private fun NavGraphBuilder.addTorch() {
    composable(Routes.TORCH) { TorchScreen() }
}

private fun NavGraphBuilder.addCompass() {
    composable(Routes.COMPASS) { CompassScreen() }
}

private fun NavGraphBuilder.addLevel() {
    composable(Routes.LEVEL) { LevelScreen() }
}

private fun NavGraphBuilder.addRuler() {
    composable(Routes.RULER) { RulerScreen() }
}

private fun NavGraphBuilder.addSound() {
    composable(Routes.SOUND) { SoundScreen() }
}

private fun NavGraphBuilder.addConverter() {
    composable(Routes.CONVERTER) { ConverterScreen() }
}