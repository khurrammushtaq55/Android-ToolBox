package com.mmushtaq.orm.allinone

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmushtaq.orm.allinone.ads.AdsInitializer
import com.mmushtaq.orm.allinone.ads.InterstitialAdManager
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
        AdsInitializer.init(this /*, testDeviceIds = listOf("85A8F6E80A77E80AEC833CA3993A7071") */)


        setContent {

            App()
        }
    }
}

@Composable
fun App() {
    // If you have your own theme, replace with ToolboxTheme {}
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController, startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        addHome(navController)
        addTorch(navController)
        addCompass(navController)
        addLevel(navController)
        addRuler(navController)
        addSound(navController)
        addConverter(navController)
    }
}

// --- Nav graph split into small helpers for readability ---

private fun NavGraphBuilder.addHome(navController: NavHostController) {
    composable(Routes.HOME) {
        HomeScreen(onOpen = { route ->
            navController.navigate(route)
        })

    }
}

private fun NavGraphBuilder.addTorch(navController: NavHostController) {
    composable(Routes.TORCH) {
        TorchScreen()
    }
}

private fun NavGraphBuilder.addCompass(navController: NavHostController) {
    composable(Routes.COMPASS) {
        CompassScreen()
    }
}

private fun NavGraphBuilder.addLevel(navController: NavHostController) {
    composable(Routes.LEVEL) {
        LevelScreen()
    }
}

private fun NavGraphBuilder.addRuler(navController: NavHostController) {
    composable(Routes.RULER) {

        ShowAd(navController)

        RulerScreen()
    }
}

private fun NavGraphBuilder.addSound(navController: NavHostController) {
    composable(Routes.SOUND) {
        ShowAd(navController)
        SoundScreen()
    }
}

private fun NavGraphBuilder.addConverter(navController: NavHostController) {
    composable(Routes.CONVERTER) {
        ConverterScreen()
    }
}


@Composable
fun ShowAd(navController: NavHostController) {
    val activity = LocalActivity.current as Activity
    val adUnitId = stringResource(R.string.admob_interstitial_id)

    val interstitial = remember { InterstitialAdManager(activity, adUnitId) }
    LaunchedEffect(Unit) { interstitial.load() }

    BackHandler {
        if (interstitial.isReady) {
            interstitial.show {
                interstitial.load()             // preload next
                navController.popBackStack()    // navigate after ad closes
            }
        } else {
            navController.popBackStack()
        }
    }
}