package com.mmushtaq.orm.allinone.features.torch

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mmushtaq.orm.allinone.core.widgets.hasCameraPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorchScreen(vm: TorchViewModel = viewModel()) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // Permission state...
    var hasCameraPerm by remember { mutableStateOf(hasCameraPermission(ctx)) }
    val reqPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPerm = granted
        if (!granted) vm.setTorch(false)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    // 🔒 Unified cleanup: turn OFF torch/SOS on background & when leaving screen
    DisposableEffect(lifecycleOwner, vm) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) {
                vm.stopSOS()
                vm.setTorch(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            vm.stopSOS()
            vm.setTorch(false)
        }
    }
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Flashlight") }) }) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(12.dp))

            // Elegant glowing button
            TorchBigButton(
                enabled = vm.hasFlash && hasCameraPerm,
                isOn = vm.isOn,
                onClick = {
                    if (!vm.hasFlash) return@TorchBigButton
                    if (!hasCameraPerm) {
                        reqPerm.launch(android.Manifest.permission.CAMERA)
                        return@TorchBigButton
                    }

                    if (vm.isSos) vm.stopSOS()   // cancel pulses, keep current torch state
                    else vm.toggle()             // normal toggle

                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            )

            // Status / messages
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when {
                    !vm.hasFlash -> {
                        Text(
                            "No camera flash available on this device.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    !hasCameraPerm -> {
                        PermissionCard(
                            text = "Grant camera permission to control the flashlight.",
                            onGrant = { reqPerm.launch(android.Manifest.permission.CAMERA) }
                        )
                    }

                    else -> {
                        Text(
                            if (vm.isOn) "Torch is ON" else "Torch is OFF",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Keep device cool — prolonged use may warm the camera.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = {
                        if (!hasCameraPerm) {
                            reqPerm.launch(android.Manifest.permission.CAMERA)
                        } else {
                            if (!vm.isSos) vm.startSOS() else vm.stopSOS(turnOffTorch = true)
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    enabled = vm.hasFlash
                ) { Text(if (vm.isSos) "Stop SOS" else "SOS") }

                OutlinedButton(
                    onClick = {
                        if (!hasCameraPerm) reqPerm.launch(android.Manifest.permission.CAMERA) else {
                            vm.setTorch(false)
                        }
                    },
                    enabled = vm.hasFlash
                ) { Text("Turn Off") }
            }
        }
    }
}

@Preview
@Composable
fun TorchScreenPreview() {
    TorchScreen()
}