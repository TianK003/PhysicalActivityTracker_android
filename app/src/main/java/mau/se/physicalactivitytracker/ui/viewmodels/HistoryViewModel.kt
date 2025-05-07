package mau.se.physicalactivitytracker.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    // Sample data
    private val _activities = MutableStateFlow(listOf(
        ActivityData(
            date = Date(System.currentTimeMillis() - 86400000 * 4),
            distance = 680.0,
            steps = 890,
            duration = "12:34"
        ),
        ActivityData(
            date = Date(System.currentTimeMillis() - 86400000 * 3),
            distance = 2400.0,
            steps = 3200,
            duration = "45:12"
        ),
        ActivityData(
            date = Date(System.currentTimeMillis() - 86400000 * 2),
            distance = 10500.0,
            steps = 14200,
            duration = "1:23:45"
        ),
        ActivityData(
            date = Date(System.currentTimeMillis() - 86400000),
            distance = 550.0,
            steps = 720,
            duration = "09:27"
        ),
        ActivityData(
            date = Date(),
            distance = 8300.0,
            steps = 11000,
            duration = "58:39"
        )
    ))

    val activities: StateFlow<List<ActivityData>> = _activities

    // Sorting state
    enum class SortType { DATE, DISTANCE, DURATION }
    private val _sortType = MutableStateFlow(SortType.DATE)
    val sortType: StateFlow<SortType> = _sortType

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
    }

    data class ActivityData(
        val date: Date,
        val distance: Double,
        val steps: Int,
        val duration: String
    )
}