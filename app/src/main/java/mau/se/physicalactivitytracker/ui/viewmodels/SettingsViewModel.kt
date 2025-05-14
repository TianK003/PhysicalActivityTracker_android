package mau.se.physicalactivitytracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mau.se.physicalactivitytracker.data.settings.UserPreferencesRepository


class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application)

    val languagePreference = repository.languagePreference
    val unitsPreference = repository.unitsPreference

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            repository.setLanguage(languageCode)
            // Add locale change handling here if needed
        }
    }

    fun setUseImperialUnits(useImperial: Boolean) {
        viewModelScope.launch {
            repository.setUseImperialUnits(useImperial)
        }
    }
}