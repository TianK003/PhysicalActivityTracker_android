package mau.se.physicalactivitytracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import mau.se.physicalactivitytracker.data.records.db.AppDatabase
import mau.se.physicalactivitytracker.data.records.repository.ActivityRepository

class MapViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            val activityRecordDao = AppDatabase.getDatabase(application).activityRecordDao()
            val gson = Gson()
            val activityRepository = ActivityRepository(activityRecordDao, application, gson)

            return MapViewModel(application, activityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
