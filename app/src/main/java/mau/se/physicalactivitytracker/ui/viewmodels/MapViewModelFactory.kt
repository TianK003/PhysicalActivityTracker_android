package mau.se.physicalactivitytracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import mau.se.physicalactivitytracker.data.records.db.AppDatabase
import mau.se.physicalactivitytracker.data.records.repository.ActivityRepository

/**
 * Factory for creating MapViewModel instances.
 * This is necessary if your ViewModel has constructor parameters.
 *
 * @param application The application context.
 */
class MapViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given `Class`.
     *
     * @param modelClass a `Class` whose instance is requested
     * @return a newly created ViewModel
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            // Initialize dependencies for ActivityRepository
            // In a real app, you'd likely use a dependency injection framework like Hilt
            // to provide these dependencies.
            val activityRecordDao = AppDatabase.getDatabase(application).activityRecordDao()
            val gson = Gson() // Create a Gson instance
            val activityRepository = ActivityRepository(activityRecordDao, application, gson)

            return MapViewModel(application, activityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
