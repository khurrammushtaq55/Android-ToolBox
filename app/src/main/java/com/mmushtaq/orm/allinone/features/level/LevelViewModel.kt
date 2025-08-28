package com.mmushtaq.orm.allinone.features.level


import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class LevelViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {
    private val mgr = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val grav = mgr.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val accel = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


    // Smoothed gravity vector (m/s^2)
    private var gX = 0f
    private var gY = 0f
    private var gZ = 9.81f
    private val alpha = 0.12f // EMA smoothing; higher = snappier


    // Public state
    var pitchDeg by mutableStateOf(0f) // rotation around X (tilt forward/back)
        private set
    var rollDeg by mutableStateOf(0f) // rotation around Y (tilt left/right)
        private set
    var faceUp by mutableStateOf(true) // is device roughly flat (screen up)
        private set


    // Calibration offsets
    var offsetPitch by mutableStateOf(0f)
        private set
    var offsetRoll by mutableStateOf(0f)
        private set


    fun start() {
        if (grav != null) mgr.registerListener(this, grav, SensorManager.SENSOR_DELAY_GAME)
        else accel?.let { mgr.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stop() {
        mgr.unregisterListener(this)
    }


    override fun onSensorChanged(e: SensorEvent) {
        when (e.sensor.type) {
            Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER -> {
// EMA low‑pass to estimate gravity vector
                gX = lerp(gX, e.values[0], alpha)
                gY = lerp(gY, e.values[1], alpha)
                gZ = lerp(gZ, e.values[2], alpha)


// Determine if the device is reasonably face‑up
                faceUp = abs(gZ) > 6.5f // ~> 35° from horizontal threshold


// Compute pitch/roll from gravity vector
// pitch = atan2(-gx, sqrt(gy^2 + gz^2))
// roll = atan2(gy, gz)
                val pitch =
                    Math.toDegrees(atan2(-gX.toDouble(), sqrt((gY * gY + gZ * gZ).toDouble())))
                        .toFloat()
                val roll = Math.toDegrees(atan2(gY.toDouble(), gZ.toDouble())).toFloat()


                pitchDeg = normalizeAngle(pitch - offsetPitch)
                rollDeg = normalizeAngle(roll - offsetRoll)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    fun calibrateHere() {
        offsetPitch += pitchDeg
        offsetRoll += rollDeg
// Clamp to -180..180 to avoid drift
        offsetPitch = normalizeAngle(offsetPitch)
        offsetRoll = normalizeAngle(offsetRoll)
    }

    fun resetCalibration() {
        offsetPitch = 0f; offsetRoll = 0f
    }


    private fun lerp(old: Float, new: Float, a: Float) = old + a * (new - old)
    private fun normalizeAngle(d: Float): Float {
        var x = d
        while (x > 180f) x -= 360f
        while (x <= -180f) x += 360f
        return x
    }
}