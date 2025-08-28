package com.mmushtaq.orm.allinone.features.torch

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class TorchViewModel(app: Application) : AndroidViewModel(app) {
    private var ctx = app.applicationContext
    private val camMgr = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    // Prefer back camera with flash
    private val torchCamId: String? = camMgr.cameraIdList.firstOrNull { id ->
        val ch = camMgr.getCameraCharacteristics(id)
        val hasFlash = ch.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        val isBack = ch.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        hasFlash && isBack
    } ?: camMgr.cameraIdList.firstOrNull { id ->
        camMgr.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }

    // Public state
    var hasFlash by mutableStateOf(torchCamId != null)
        private set
    var isOn by mutableStateOf(false)
        private set
    var isSos by mutableStateOf(false)
        private set

    private var sosJob: Job? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCamId) isOn = enabled
        }
    }

    init { camMgr.registerTorchCallback(torchCallback, null) }

    override fun onCleared() {
        super.onCleared()
        camMgr.unregisterTorchCallback(torchCallback)
        stopSOS()
        // Safety: ensure OFF when VM cleared
        setTorch(false)
        ctx=null
    }

    fun toggle() {
        setTorch(!isOn)
    }

    fun setTorch(on: Boolean) {
        val id = torchCamId ?: return
        try {
            camMgr.setTorchMode(id, on)
            isOn = on
        } catch (_: SecurityException) {
            // Permission not granted; UI will handle
        } catch (_: Exception) {}
    }

    fun startSOS() {
        if (isSos || !hasFlash) return
        isSos = true
        sosJob = viewModelScope.launch {
            // Morse: ... --- ...  (short=180ms on, long=540ms on, gap=180ms)
            val short = 180L
            val long = 540L
            val gap = 180L
            val letterGap = 420L
            val wordGap = 840L
            val pattern = listOf(
                // S
                short to gap, short to gap, short to letterGap,
                // O
                long to gap, long to gap, long to letterGap,
                // S
                short to gap, short to gap, short to wordGap
            )
            while (isActive && isSos) {
                for ((onDur, offDur) in pattern) {
                    setTorch(true)
                    delay(onDur)
                    setTorch(false)
                    delay(offDur)
                    if (!isSos) break
                }
            }
        }
    }

    fun stopSOS(turnOffTorch: Boolean = false) {
        if (!isSos) {
            // nothing to cancel; respect caller's intent
            if (turnOffTorch) setTorch(false)
            return
        }
        isSos = false
        sosJob?.cancel(); sosJob = null
        if (turnOffTorch) setTorch(false)
    }

}
