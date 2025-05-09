package mau.se.physicalactivitytracker.ui.viewmodels

import android.app.Application
import kotlinx.coroutines.flow.combine
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mau.se.physicalactivitytracker.WalkTrack
import mau.se.physicalactivitytracker.data.records.model.ActivityRecord
import mau.se.physicalactivitytracker.data.records.repository.ActivityRepository
import java.util.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ActivityRepository

    init {
        val database = (application as WalkTrack).database
        repository = ActivityRepository(
            activityRecordDao = database.activityRecordDao(),
            context = application,
            gson = Gson()
        )
    }

    private val _sortType = MutableStateFlow(SortType.DATE)
    val sortType: StateFlow<SortType> = _sortType

    val activities: StateFlow<List<ActivityData>> = repository.getAllActivityRecords()
        .map { records ->
            records.map { it.toActivityData() }
        }
        .combine(_sortType) { records: List<ActivityData>, sortType: SortType ->
            when (sortType) {
                SortType.DATE -> records.sortedByDescending { it.date }
                SortType.DISTANCE -> records.sortedByDescending { it.distance }
                SortType.DURATION -> records.sortedByDescending { it.durationMs }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
    }

    enum class SortType { DATE, DISTANCE, DURATION }

    data class ActivityData(
        val name: String,
        val date: Date,
        val distance: Double,
        val steps: Int,
        val duration: String,
        val durationMs: Long
    )
}

// Extension function to convert ActivityRecord to ActivityData
private fun ActivityRecord.toActivityData(): HistoryViewModel.ActivityData {
    return HistoryViewModel.ActivityData(
        name = name,
        date = date,
        distance = distanceMeters ?: 0.0,
        steps = stepCount,
        duration = formatDuration(elapsedTimeMs),
        durationMs = elapsedTimeMs
    )
}

// Duration formatting helper
private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000 % 60
    val minutes = millis / (1000 * 60) % 60
    val hours = millis / (1000 * 60 * 60)

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}