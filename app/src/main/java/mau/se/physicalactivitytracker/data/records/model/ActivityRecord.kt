package mau.se.physicalactivitytracker.data.records.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "activity_records")
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    val date: Date,
    var stepCount: Int,
    var elapsedTimeMs: Long,
    var gpsFilePath: String,
    var inertialFilePath: String,
    // distance can be calculated later
    var distanceMeters: Double? = null
)