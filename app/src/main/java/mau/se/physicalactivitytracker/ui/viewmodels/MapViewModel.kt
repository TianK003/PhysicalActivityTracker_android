package mau.se.physicalactivitytracker.ui.viewmodels

import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import mau.se.physicalactivitytracker.data.records.model.AccelerometerData
import mau.se.physicalactivitytracker.data.records.model.GyroscopeData
import mau.se.physicalactivitytracker.data.records.model.InertialSensorData
import mau.se.physicalactivitytracker.data.records.model.LocationPoint
import mau.se.physicalactivitytracker.data.records.model.StepDetectorEvent
import mau.se.physicalactivitytracker.data.records.repository.ActivityRepository
import mau.se.physicalactivitytracker.data.records.sensors.LocationManager
import mau.se.physicalactivitytracker.data.records.sensors.SensorDataManager
import mau.se.physicalactivitytracker.ui.services.TrackingService
import java.util.Date

class MapViewModel(
    private val application: Application,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    // permissions
    sealed class PermissionState {
        object Idle : PermissionState()
        object Granted : PermissionState()
        data class Required(val permissions: List<String>) : PermissionState()
        data class Denied(val message: String) : PermissionState()
    }
    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Idle)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    private val _showNameDialog = MutableStateFlow(false)
    val showNameDialog: StateFlow<Boolean> = _showNameDialog.asStateFlow()

    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var locationManager: LocationManager
    private var elapsedTimeJob: Job? = null

    private var _temporaryActivityData: TemporaryActivityData? = null
    // for drawing the line
    private val _gpsPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val gpsPoints: StateFlow<List<LocationPoint>> = _gpsPoints.asStateFlow()

    data class TemporaryActivityData(
        val elapsedTimeMs: Long,
        val stepCount: Int,
        val gpsPoints: List<LocationPoint>,
        val inertialData: InertialSensorData,
        val activityStartTime: Long,
        val distanceMeters: Double
    )

    private val _elapsedTimeMs = MutableStateFlow<Long?>(null)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    private var activityStartTime: Long = 0L

    // all lists of data we store
    private val collectedGpsPoints = mutableListOf<LocationPoint>()
    private val collectedAccelerometerData = mutableListOf<AccelerometerData>()
    private val collectedGyroscopeData = mutableListOf<GyroscopeData>()
    private val collectedStepDetectorEvents = mutableListOf<StepDetectorEvent>()

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startActivity(context: Context) {
        if (!_isRecording.value) {
            // Check permissions first
            val requiredPermissions = mutableListOf<String>().apply {
                // Add location and activity recognition permissions if not granted
                addAll(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ).filter { perm ->
                        ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
                    }
                )
                // Add notification permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notificationPerm = Manifest.permission.POST_NOTIFICATIONS
                    if (ContextCompat.checkSelfPermission(context, notificationPerm) != PackageManager.PERMISSION_GRANTED) {
                        add(notificationPerm)
                    }
                }
            }

            if (requiredPermissions.isNotEmpty()) {
                _permissionState.value = PermissionState.Required(requiredPermissions)
                return
            }
            // Proceed with initialization if permissions are granted
            initializeSensors(context)
        }
    }

    fun stopActivity() {
        if (_isRecording.value) {
            _isRecording.value = false
            val endTime = System.currentTimeMillis()
            val elapsedTimeMs = endTime - activityStartTime

            // Stop notification service
            val serviceIntent = Intent(application, TrackingService::class.java)
            application.stopService(serviceIntent)

            // Collect data
            val stepCount = collectedStepDetectorEvents.size
            val inertialData = InertialSensorData(
                collectedAccelerometerData.toList(),
                collectedGyroscopeData.toList(),
                collectedStepDetectorEvents.toList()
            )
            val gpsPoints = collectedGpsPoints.toList()

            sensorDataManager.stopListening()
            locationManager.stopLocationUpdates()
            elapsedTimeJob?.cancel()
            Toast.makeText(application, "Recording stopped", Toast.LENGTH_SHORT).show()

            _temporaryActivityData = TemporaryActivityData(
                elapsedTimeMs,
                stepCount,
                gpsPoints,
                inertialData,
                activityStartTime,
                0.0 // Calculated while saving
            )

            _showNameDialog.value = true
        }
    }

    fun saveActivity(name: String) {
        val data = _temporaryActivityData ?: return

        val distance = calculateDistance(_temporaryActivityData!!.gpsPoints)

        viewModelScope.launch {
            activityRepository.addActivity(
                name = name,
                date = Date(data.activityStartTime),
                stepCount = data.stepCount,
                elapsedTimeMs = data.elapsedTimeMs,
                distanceMeters = distance,
                gpsPoints = data.gpsPoints,
                inertialData = data.inertialData
            ).also { recordId ->
                // show to the user that the activity was saved
                if (recordId != -1L) {
                    Toast.makeText(application, "Activity saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(application, "Failed to save activity", Toast.LENGTH_SHORT).show()
                    Log.e("MapViewModel", "Failed to save activity")
                }
            }
            _gpsPoints.value = emptyList()
            collectedGpsPoints.clear()
            _temporaryActivityData = null
            _showNameDialog.value = false
        }
    }

    fun cancelSaveActivity() {
        _gpsPoints.value = emptyList()
        collectedGpsPoints.clear()
        _temporaryActivityData = null
        _showNameDialog.value = false
    }

    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val denied = permissions.filter { !it.value }.keys
        _permissionState.value = if (denied.isEmpty()) {
            PermissionState.Granted
        } else {
            PermissionState.Denied("Denied: ${denied.joinToString()}")
        }
    }

    private fun initializeSensors(context: Context) {
        sensorDataManager = SensorDataManager(context)
        locationManager = LocationManager(context)

        _isRecording.value = true
        activityStartTime = System.currentTimeMillis()

        collectedGpsPoints.clear()
        _gpsPoints.value = emptyList()
        collectedAccelerometerData.clear()
        collectedGyroscopeData.clear()
        collectedStepDetectorEvents.clear()

        // start the notification service
        val serviceIntent = Intent(context, TrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        startSensorCollection()
        startLocationCollection()
        startTimeTracking()

        Toast.makeText(application, "Recording started", Toast.LENGTH_SHORT).show()
    }

    private fun startSensorCollection() {
        sensorDataManager.startListening()
        // Just in case anything was left destroy all previous points
        _gpsPoints.value = emptyList()
        collectedGpsPoints.clear()

        // Collect accelerometer data
        viewModelScope.launch {
            sensorDataManager.accelerometerChannel.consumeAsFlow().collect { data ->
                Log.d("Data", "Accelerometer data collected: $data")
                collectedAccelerometerData.add(data)
            }
        }

        // Collect gyroscope data
        viewModelScope.launch {
            sensorDataManager.gyroscopeChannel.consumeAsFlow().collect { data ->
                Log.d("Data", "Gyroscope data collected: $data")
                collectedGyroscopeData.add(data)
            }
        }

        // Collect step detector events
        viewModelScope.launch {
            sensorDataManager.stepDetectorChannel.consumeAsFlow().collect { event ->
                Log.d("Data", "Step detector event collected: $event")
                collectedStepDetectorEvents.add(event)
            }
        }
    }

    // collects GPS data
    private fun startLocationCollection() {
        locationManager.startLocationUpdates()

        viewModelScope.launch {
            locationManager.locationChannel.consumeAsFlow().collect { location ->
                collectedGpsPoints.add(location)
                _gpsPoints.value = collectedGpsPoints.toList()
            }
        }
    }

    // starts counting time
    private fun startTimeTracking() {
        elapsedTimeJob = viewModelScope.launch {
            while (isRecording.value) {
                _elapsedTimeMs.value = System.currentTimeMillis() - activityStartTime
                delay(1000)
            }
        }
    }

    // calculate total distance covered in the activity
    private fun calculateDistance(points: List<LocationPoint>): Double {
        if (points.size < 2) return 0.0

        var distance = 0.0
        for (i in 1 until points.size) {
            val prev = points[i-1]
            val curr = points[i]
            val results = FloatArray(1)
            Location.distanceBetween(
                prev.latitude,
                prev.longitude,
                curr.latitude,
                curr.longitude,
                results
            )
            distance += results[0]
        }
        return distance
    }
}
