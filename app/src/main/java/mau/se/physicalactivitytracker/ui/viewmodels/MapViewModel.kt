package mau.se.physicalactivitytracker.ui.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mau.se.physicalactivitytracker.data.records.model.AccelerometerData
import mau.se.physicalactivitytracker.data.records.model.GyroscopeData
import mau.se.physicalactivitytracker.data.records.model.InertialSensorData
import mau.se.physicalactivitytracker.data.records.model.LocationPoint
import mau.se.physicalactivitytracker.data.records.model.StepDetectorEvent
import mau.se.physicalactivitytracker.data.records.repository.ActivityRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

    private val _showNameDialog = MutableStateFlow(false)
    val showNameDialog: StateFlow<Boolean> = _showNameDialog.asStateFlow()

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


    /**
     * Starts a new activity recording.
     * This function will eventually trigger sensor data collection.
     */
    fun startActivity() {
        if (!_isRecording.value) {
            _isRecording.value = true
            activityStartTime = System.currentTimeMillis()

            // Clear previous data (if any)
            collectedGpsPoints.clear()
            collectedAccelerometerData.clear()
            collectedGyroscopeData.clear()
            collectedStepDetectorEvents.clear()

            // TODO: Start actual GPS and inertial sensor data collection here.
            // For now, we'll just show a toast.
            Toast.makeText(application, "Recording started", Toast.LENGTH_SHORT).show()

            // Example: Add a dummy GPS point for testing if needed
            // collectedGpsPoints.add(LocationPoint(System.currentTimeMillis(), 55.60, 13.00, 10.0, 5f))
        }
    }
}
