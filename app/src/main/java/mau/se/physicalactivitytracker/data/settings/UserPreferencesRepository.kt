package mau.se.physicalactivitytracker.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val USE_IMPERIAL_UNITS = booleanPreferencesKey("use_imperial_units")

        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_UNITS = false
    }

    val languagePreference: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[SELECTED_LANGUAGE] ?: DEFAULT_LANGUAGE
        }

    val unitsPreference: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[USE_IMPERIAL_UNITS] ?: DEFAULT_UNITS
        }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = languageCode
        }
    }

    suspend fun setUseImperialUnits(useImperial: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_IMPERIAL_UNITS] = useImperial
        }
    }
}