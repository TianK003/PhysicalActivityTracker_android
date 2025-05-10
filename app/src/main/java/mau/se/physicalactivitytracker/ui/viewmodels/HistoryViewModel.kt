import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import mau.se.physicalactivitytracker.WalkTrack
import mau.se.physicalactivitytracker.data.records.model.ActivityRecord
import mau.se.physicalactivitytracker.data.records.repository.ActivityRepository
import java.util.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ActivityRepository

    // Date filtering state
    private val _startDate = MutableStateFlow(getDefaultStartDate())
    val startDate: StateFlow<Date> = _startDate

    private val _endDate = MutableStateFlow(getDefaultEndDate())
    val endDate: StateFlow<Date> = _endDate

    // Sorting state
    private val _sortType = MutableStateFlow(SortType.DATE)
    val sortType: StateFlow<SortType> = _sortType

    @OptIn(ExperimentalCoroutinesApi::class)
    val activities: StateFlow<List<ActivityData>> = combine(
        combine(_startDate, _endDate) { start, end ->
            repository.getActivitiesBetweenDates(start, end)
        }.flatMapLatest { it },
        _sortType
    ) { activities, sortType ->
        activities.map { it.toActivityData() }.let { records ->
            when (sortType) {
                SortType.DATE -> records.sortedByDescending { it.date }
                SortType.DISTANCE -> records.sortedByDescending { it.distance }
                SortType.DURATION -> records.sortedByDescending { it.durationMs }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        val walkTrackApp = getApplication<WalkTrack>()
        val database = walkTrackApp.database
        repository = ActivityRepository(
            activityRecordDao = database.activityRecordDao(),
            context = walkTrackApp,
            gson = Gson()
        )
    }

    // Date management
    fun updateDates(start: Date, end: Date) {
        updateStartDate(start)
        updateEndDate(end)
    }

    private fun updateStartDate(date: Date) {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _startDate.value = calendar.time
    }

    private fun updateEndDate(date: Date) {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        _endDate.value = calendar.time
    }

    private fun getDefaultStartDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        return calendar.time
    }

    private fun getDefaultEndDate(): Date = Date()

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
    }

    enum class SortType { DATE, DISTANCE, DURATION }

    data class ActivityData(
        val id: Long,
        val name: String,
        val date: Date,
        val distance: Double,
        val steps: Int,
        val duration: String,
        val durationMs: Long
    )

    private fun ActivityRecord.toActivityData(): ActivityData {
        return ActivityData(
            id = id,
            name = name,
            date = date,
            distance = distanceMeters ?: 0.0,
            steps = stepCount,
            duration = formatDuration(elapsedTimeMs),
            durationMs = elapsedTimeMs
        )
    }

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
}