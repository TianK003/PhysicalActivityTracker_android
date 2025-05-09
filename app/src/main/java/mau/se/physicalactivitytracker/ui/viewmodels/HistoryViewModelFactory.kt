package mau.se.physicalactivitytracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HistoryViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}