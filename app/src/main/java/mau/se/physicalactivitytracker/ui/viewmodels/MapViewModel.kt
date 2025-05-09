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

    private val _elapsedTimeMs = MutableStateFlow<Long?>(null)
    val elapsedTimeMs: StateFlow<Long?> = _elapsedTimeMs.asStateFlow()

    private val _stepCount = MutableStateFlow<Int?>(null)
    val stepCount: StateFlow<Int?> = _stepCount.asStateFlow()

    fun saveActivity(name: String) {
        val elapsed = _elapsedTimeMs.value ?: return
        val steps = _stepCount.value ?: return

        viewModelScope.launch {
            // Existing save logic from stopActivity() goes here
            // Update the name parameter to use the passed name
            // Clear temporary data after saving
        }
    }

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

    /**
     * Stops the current activity recording and saves the data.
     */
    fun stopActivity() {
        if (_isRecording.value) {
            _isRecording.value = false
            val endTime = System.currentTimeMillis()
            val elapsedTimeMs = endTime - activityStartTime

            // TODO: Stop sensor data collection here.

            // Create a name for the activity based on the current time
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault() // Use local timezone
            val activityName = "Walk - ${sdf.format(Date(activityStartTime))}"

            // Placeholder for step count - to be replaced with actual sensor data
            val stepCount = collectedStepDetectorEvents.size // Or from TYPE_STEP_COUNTER

            // Placeholder for distance - can be calculated from GPS points later
            val distanceMeters = 0.0 // calculateDistance(collectedGpsPoints)

            viewModelScope.launch {
                val inertialData = InertialSensorData(
                    accelerometerReadings = collectedAccelerometerData.toList(), // Make copies
                    gyroscopeReadings = collectedGyroscopeData.toList(),
                    stepDetectorEvents = collectedStepDetectorEvents.toList()
                )

                val recordId = activityRepository.addActivity(
                    name = activityName,
                    date = Date(activityStartTime),
                    stepCount = stepCount,
                    elapsedTimeMs = elapsedTimeMs,
                    distanceMeters = distanceMeters,
                    gpsPoints = collectedGpsPoints.toList(), // Make copies
                    inertialData = inertialData
                )

                if (recordId != -1L) {
                    Toast.makeText(application, "Activity saved: $activityName", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(application, "Failed to save activity", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Optional: Add a helper function to calculate distance if needed in the future
    // private fun calculateDistance(points: List<LocationPoint>): Double { ... }
}
