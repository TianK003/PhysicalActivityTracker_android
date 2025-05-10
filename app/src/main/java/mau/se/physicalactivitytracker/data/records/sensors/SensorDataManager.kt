package mau.se.physicalactivitytracker.data.records.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.Channel
import mau.se.physicalactivitytracker.data.records.model.*

class SensorDataManager(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope    = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val accelerometerChannel = Channel<AccelerometerData>(Channel.UNLIMITED)
    val gyroscopeChannel     = Channel<GyroscopeData>(Channel.UNLIMITED)
    val stepDetectorChannel  = Channel<StepDetectorEvent>(Channel.UNLIMITED)

    /** Start streaming data (UI delay ≈ 60 Hz is fine for walking) */
    fun startListening() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let    { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        stepDetector?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    /** Always pair with stopListening to avoid battery drain */
    fun stopListening() = sensorManager.unregisterListener(this)

    /** REQUIRED override – renamed from onSensorEvent */
    override fun onSensorChanged(event: SensorEvent) {
        val ts = System.currentTimeMillis()
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER ->
                accelerometerChannel.trySend(
                    AccelerometerData(ts, event.values[0], event.values[1], event.values[2])
                )
            Sensor.TYPE_GYROSCOPE     ->
                gyroscopeChannel.trySend(
                    GyroscopeData(ts, event.values[0], event.values[1], event.values[2])
                )
            Sensor.TYPE_STEP_DETECTOR -> stepDetectorChannel.trySend(StepDetectorEvent(ts))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
