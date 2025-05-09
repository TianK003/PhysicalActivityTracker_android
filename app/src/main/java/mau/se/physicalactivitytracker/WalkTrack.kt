package mau.se.physicalactivitytracker

import android.app.Application
import mau.se.physicalactivitytracker.data.records.db.AppDatabase

class WalkTrack : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}