package mau.se.physicalactivitytracker.ui.viewmodels

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
import mau.se.physicalactivitytracker.data.records.model.LocationPoint
import mau.se.physicalactivitytracker.data.records.repository.ActivityRepository
import java.util.Date

// ActivityDetailsViewModel.kt
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
            record?.let {
                val points = activityRepository.loadGpsData(it)
                points?.let {
                    _gpsPoints.value = points
                }

                _activityDetails.value = ActivityDetails(
                    name = it.name,
                    date = it.date,
                    steps = it.stepCount,
                    duration = formatDuration(it.elapsedTimeMs)
                )
            }
        }
    }

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