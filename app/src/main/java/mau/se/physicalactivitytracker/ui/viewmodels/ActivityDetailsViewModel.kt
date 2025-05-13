package mau.se.physicalactivitytracker.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mau.se.physicalactivitytracker.data.records.db.AppDatabase
import mau.se.physicalactivitytracker.data.records.model.AccelerometerData
import mau.se.physicalactivitytracker.data.records.model.LocationPoint
import mau.se.physicalactivitytracker.data.records.repository.ActivityRepository
import java.util.Date
import kotlin.math.pow
import kotlin.math.sqrt

class ActivityDetailsViewModel(
    private val application: Application,
    private val activityRepository: ActivityRepository,
    private val activityId: Long
) : ViewModel() {

    private val _gpsPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val gpsPoints: StateFlow<List<LocationPoint>> = _gpsPoints.asStateFlow()

    private val _activityDetails = MutableStateFlow<ActivityDetails?>(null)
    val activityDetails: StateFlow<ActivityDetails?> = _activityDetails.asStateFlow()

    init {
        loadActivityData()
    }

    private fun loadActivityData() {
        viewModelScope.launch {
            val record = activityRepository.getActivityRecordById(activityId).first()
            record?.let { activityRecord ->
                val points = activityRepository.loadGpsData(activityRecord)
                points?.let {
                    _gpsPoints.value = points
                }

                // Load inertial data and calculate steps
                val inertialData = activityRepository.loadInertialData(activityRecord)
                val calculatedSteps = inertialData?.accelerometerReadings?.let { accelData ->
                    calculateSteps(accelData)
                } ?: 0

                _activityDetails.value = ActivityDetails(
                    name = activityRecord.name,
                    date = activityRecord.date,
                    steps = activityRecord.stepCount,
                    calculatedSteps = calculatedSteps,
                    duration = formatDuration(activityRecord.elapsedTimeMs)
                )
            }
        }
    }

    private fun calculateSteps(accelerometerData: List<AccelerometerData>): Int {
        if (accelerometerData.isEmpty()) return 0

        // Compute magnitudes for each accelerometer reading
        val magnitudes = accelerometerData.map { accel ->
            sqrt(
                accel.x.toDouble().pow(2) +
                        accel.y.toDouble().pow(2) +
                        accel.z.toDouble().pow(2)
            )
        }

        // Apply better low-pass filter with more appropriate alpha
        // Alpha = 0.1 gives more weight to the previous values (more smoothing) - empirically tested over multiple walks and values
        val alpha = 0.1
        val filtered = mutableListOf(magnitudes[0])
        for (i in 1 until magnitudes.size) {
            filtered.add(alpha * magnitudes[i] + (1 - alpha) * filtered[i-1])
        }

        // Calculate the dynamic threshold based on the signal
        // This adapts to the user's walking pattern and device sensitivity
        val mean = filtered.average()
        val stdDev = calculateStdDev(filtered, mean)

        // Standard deviation helps us set an adaptive threshold
        val baseThreshold = mean + 0.9 * stdDev

        // Implement better peak detection with dynamic thresholding
        var stepCount = 0
        // determined to work quite well
        val minStepInterval = 400L
        var lastStepTime = accelerometerData.first().timestamp

        // State tracking for peak detection
        var isPotentialStep = false
        var consecutiveAboveThreshold = 0
        // Need this many points above threshold to count as potential step
        val requiredConsecutivePoints = 2

        for (i in 2 until filtered.size - 2) {
            val prev2 = filtered[i-2]
            val prev1 = filtered[i-1]
            val current = filtered[i]
            val next1 = filtered[i+1]
            val next2 = filtered[i+2]

            // Ensure minimum time between steps
            val timeDiff = accelerometerData[i].timestamp - lastStepTime

            // Non-maxima supression of sorroundings
            val isPeak = current > prev1 && current > prev2 &&
                    current > next1 && current > next2 &&
                    current > baseThreshold

            // Require the signal to stay above threshold for consecutive readings
            if (current > baseThreshold) {
                consecutiveAboveThreshold++
                if (consecutiveAboveThreshold >= requiredConsecutivePoints) {
                    isPotentialStep = true
                }
            } else {
                consecutiveAboveThreshold = 0
            }

            // Count a step when we see a peak after signal has been above threshold & enough time has passed since the last step
            if (isPeak && isPotentialStep && timeDiff >= minStepInterval) {
                stepCount++
                lastStepTime = accelerometerData[i].timestamp
                isPotentialStep = false  // Reset step tracking
                consecutiveAboveThreshold = 0
            }
        }

        return stepCount
    }

    private fun calculateStdDev(values: List<Double>, mean: Double): Double {
        if (values.size <= 1) return 0.0
        val sumSquaredDifferences = values.sumOf { (it - mean).pow(2) }
        return sqrt(sumSquaredDifferences / (values.size - 1))
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000 % 60
        val minutes = millis / (1000 * 60) % 60
        val hours = millis / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    data class ActivityDetails(
        val name: String,
        val date: Date,
        val steps: Int,
        val calculatedSteps: Int,
        val duration: String
    )

    companion object {
        fun provideFactory(application: Application, activityId: Long): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val activityRecordDao = AppDatabase.getDatabase(application).activityRecordDao()
                    val gson = Gson()
                    val activityRepository = ActivityRepository(activityRecordDao, application, gson)
                    return ActivityDetailsViewModel(application, activityRepository, activityId) as T
                }
            }
        }
    }
}