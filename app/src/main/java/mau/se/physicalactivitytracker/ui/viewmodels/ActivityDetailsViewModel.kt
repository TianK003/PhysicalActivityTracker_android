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

        // Compute magnitude for each accelerometer reading
        val magnitudes = accelerometerData.map { accel ->
            sqrt(
                accel.x.toDouble().pow(2) +
                accel.y.toDouble().pow(2) +
                accel.z.toDouble().pow(2)
            )
        }

        // Apply low-pass filter to smooth the data
        val alpha = 0.5
        val filtered = mutableListOf(magnitudes[0])
        for (i in 1 until magnitudes.size) {
            filtered.add(alpha * filtered[i-1] + (1 - alpha) * magnitudes[i])
        }

        // Detect peaks indicating steps
        var stepCount = 0
        // Empirically determined
        val threshold = 9
        // Minimum time between steps (ms) - max stride 200 per minute
        val minStepInterval = 300L
        var lastStepTime = accelerometerData.first().timestamp

        for (i in 1 until filtered.size - 1) {
            val prev = filtered[i-1]
            val current = filtered[i]
            val next = filtered[i+1]
            val timeDiff = accelerometerData[i].timestamp - lastStepTime

            if (current > prev && current > next && current > threshold && timeDiff >= minStepInterval) {
                stepCount++
                lastStepTime = accelerometerData[i].timestamp
            }
        }

        return stepCount
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