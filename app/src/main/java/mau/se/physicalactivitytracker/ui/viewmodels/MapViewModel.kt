package mau.se.physicalactivitytracker.ui.viewmodels

import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.app.Application
import android.content.Context
import android.widget.Toast
import android.location.Location
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
import java.util.Date

/**
 * ViewModel for the MapScreen.
 * Handles the logic for starting, stopping, and managing activity recording.
 *
 * @param application The application context, used for things like Toasts.
 * @param activityRepository The repository for accessing activity data.
 */
class MapViewModel(
    private val application: Application, // AndroidViewModel can also be used if only context is needed
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

    data class TemporaryActivityData(
        val elapsedTimeMs: Long,
        val stepCount: Int,
        val gpsPoints: List<LocationPoint>,
        val inertialData: InertialSensorData,
        val activityStartTime: Long,
        val distanceMeters: Double
    )

    fun stopActivity() {
        if (_isRecording.value) {
            _isRecording.value = false
            val endTime = System.currentTimeMillis()
            val elapsedTimeMs = endTime - activityStartTime

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
                0.0 // Replace with actual calculation if needed
            )
            _showNameDialog.value = true
        }
    }

    fun saveActivity(name: String) {
        val data = _temporaryActivityData ?: return

        viewModelScope.launch {
            activityRepository.addActivity(
                name = name,
                date = Date(data.activityStartTime),
                stepCount = data.stepCount,
                elapsedTimeMs = data.elapsedTimeMs,
                distanceMeters = data.distanceMeters,
                gpsPoints = data.gpsPoints,
                inertialData = data.inertialData
            ).also { recordId ->
                // Handle success/failure
            }
            _temporaryActivityData = null
            _showNameDialog.value = false
        }
    }

    fun cancelSaveActivity() {
        _temporaryActivityData = null
        _showNameDialog.value = false
    }

    private val _elapsedTimeMs = MutableStateFlow<Long?>(null)
    val elapsedTimeMs: StateFlow<Long?> = _elapsedTimeMs.asStateFlow()

    private val _stepCount = MutableStateFlow<Int?>(null)
    val stepCount: StateFlow<Int?> = _stepCount.asStateFlow()

    // Private MutableStateFlow to hold the recording state
    private val _isRecording = MutableStateFlow(false)
    // Public StateFlow to observe the recording state from the UI
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // To store start time of the activity
    private var activityStartTime: Long = 0L

    // Placeholder lists for sensor data
    // In a real implementation, these would be populated by sensor listeners
    private val collectedGpsPoints = mutableListOf<LocationPoint>()
    private val collectedAccelerometerData = mutableListOf<AccelerometerData>()
    private val collectedGyroscopeData = mutableListOf<GyroscopeData>()
    private val collectedStepDetectorEvents = mutableListOf<StepDetectorEvent>()

    fun startActivity(context: Context) {
        if (!_isRecording.value) {
            // Check permissions first
            val requiredPermissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION
            ).filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }

            if (requiredPermissions.isNotEmpty()) {
                _permissionState.value = PermissionState.Required(requiredPermissions)
                return
            }

            // Proceed with initialization if permissions are granted
            initializeSensors(context)
        }
    }

    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val denied = permissions.filter { !it.value }.keys
        if (denied.isEmpty()) {
            _permissionState.value = PermissionState.Granted
        } else {
            _permissionState.value = PermissionState.Denied(
                "Required permissions denied: ${denied.joinToString()}"
            )
        }
    }

    private fun initializeSensors(context: Context) {
        sensorDataManager = SensorDataManager(context)
        locationManager = LocationManager(context)

        _isRecording.value = true
        activityStartTime = System.currentTimeMillis()

        collectedGpsPoints.clear()
        collectedAccelerometerData.clear()
        collectedGyroscopeData.clear()
        collectedStepDetectorEvents.clear()

        startSensorCollection()
        startLocationCollection()
        startTimeTracking()

        Toast.makeText(application, "Recording started", Toast.LENGTH_SHORT).show()
    }

    private fun startSensorCollection() {
        sensorDataManager.startListening()

        // Collect accelerometer data
        viewModelScope.launch {
            sensorDataManager.accelerometerChannel.consumeAsFlow().collect { data ->
                collectedAccelerometerData.add(data)
            }
        }

        // Collect gyroscope data
        viewModelScope.launch {
            sensorDataManager.gyroscopeChannel.consumeAsFlow().collect { data ->
                collectedGyroscopeData.add(data)
            }
        }

        // Collect step detector events
        viewModelScope.launch {
            sensorDataManager.stepDetectorChannel.consumeAsFlow().collect { event ->
                collectedStepDetectorEvents.add(event)
            }
        }
    }

    private fun startLocationCollection() {
        locationManager.startLocationUpdates()

        viewModelScope.launch {
            locationManager.locationChannel.consumeAsFlow().collect { location ->
                collectedGpsPoints.add(location)
            }
        }
    }

    private fun startTimeTracking() {
        elapsedTimeJob = viewModelScope.launch {
            while (isRecording.value) {
                _elapsedTimeMs.value = System.currentTimeMillis() - activityStartTime
                delay(1000)
            }
        }
    }

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
