package com.mmushtaq.orm.allinone.features.compass


import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel


/**
 * ViewModel that exposes a smoothed azimuth (0..360) in degrees.
 * Uses ROTATION_VECTOR if present, otherwise ACCELEROMETER + MAGNETIC_FIELD.
 */
open class CompassViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {
    private val sensorMgr = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotVec = sensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accel = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnet = sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


    // Public state
    private val _azimuth = mutableStateOf(0f) // magnetic north degrees
    val azimuth: State<Float> = _azimuth


    private val _accuracy = mutableStateOf(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM)
    val accuracy: State<Int> = _accuracy


    private val R = FloatArray(9)
    private val I = FloatArray(9)
    private val orientation = FloatArray(3)
    private val accelVals = FloatArray(3)
    private val magVals = FloatArray(3)


    // Smoothing (EMA)
    private var lastDeg = 0f
    private val alpha = 0.15f // 0..1 (higher = snappier)


    // Optional calibration / declination offsets
    var userOffset by mutableStateOf(0f) // set via "Calibrate here" button
        private set
    var declinationOffset by mutableStateOf(0f) // updated via last known location (optional)
        private set


    fun start() {
        if (rotVec != null) {
            sensorMgr.registerListener(this, rotVec, SensorManager.SENSOR_DELAY_GAME)
        } else {
            accel?.let { sensorMgr.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            magnet?.let { sensorMgr.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
// Try coarse last-known location to compute magnetic declination (no Play Services needed)
        updateDeclinationFromLastKnown()
    }


    fun stop() { sensorMgr.unregisterListener(this) }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rot = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rot, event.values)
                SensorManager.getOrientation(rot, orientation)
                val deg = radToDeg(orientation[0])
                _azimuth.value = smooth(deg)
            }
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, accelVals, 0, 3)
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, magVals, 0, 3)
        }
        if (rotVec == null && SensorManager.getRotationMatrix(R, I, accelVals, magVals)) {
            SensorManager.getOrientation(R, orientation)
            val deg = radToDeg(orientation[0])
            _azimuth.value = smooth(deg)
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _accuracy.value = accuracy
    }


    private fun radToDeg(yawRad: Float): Float {
// Convert range (-pi..pi) → (0..360)
        val deg = Math.toDegrees(yawRad.toDouble()).toFloat()
        return ((deg + 360f) % 360f)
    }


    private fun smooth(target: Float): Float {
// Handle circular averaging properly by using shortest-angle delta
        var delta = target - lastDeg
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        lastDeg = (lastDeg + alpha * delta + 360f) % 360f
        return lastDeg
    }


    fun calibrateHere() { userOffset = (-azimuth.value + 360f) % 360f }


    fun clearCalibration() { userOffset = 0f }
    fun refreshDeclination() { updateDeclinationFromLastKnown() }

    private fun updateDeclinationFromLastKnown() {
        try {
            val lm = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = lm.getProviders(true)
            val loc: Location? = providers.asSequence()
                .mapNotNull { p ->
                    @Suppress("MissingPermission")
                    lm.getLastKnownLocation(p)
                }
                .maxByOrNull { it.time }
            if (loc != null) {
                val now = System.currentTimeMillis()
                val gmf = android.hardware.GeomagneticField(
                    loc.latitude.toFloat(),
                    loc.longitude.toFloat(),
                    loc.altitude.toFloat(),
                    now
                )
                declinationOffset = gmf.declination // degrees east (+) to add to magnetic for true north
            }
        } catch (_: SecurityException) {
// Location permission not granted. We'll just keep declination = 0 (magnetic north)
        } catch (_: Exception) {}
    }
}