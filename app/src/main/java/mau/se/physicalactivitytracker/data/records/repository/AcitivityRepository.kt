package mau.se.physicalactivitytracker.data.records.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import mau.se.physicalactivitytracker.data.records.db.ActivityRecordDao
import mau.se.physicalactivitytracker.data.records.model.ActivityRecord
import mau.se.physicalactivitytracker.data.records.model.InertialSensorData
import mau.se.physicalactivitytracker.data.records.model.LocationPoint
import java.io.File
import java.io.IOException
import java.util.Date

/**
 * Repository for managing ActivityRecord data.
 * It abstracts data operations from ViewModels and handles interactions with
 * the DAO and file system for JSON data.
 *
 * @param activityRecordDao The Data Access Object for activity records.
 * @param context The application context, needed for file operations.
 * @param gson Gson instance for JSON serialization/deserialization.
 */
class ActivityRepository(
    private val activityRecordDao: ActivityRecordDao,
    private val context: Context,
    private val gson: Gson
) {

    // Directory within internal storage to save activity data files
    private val activityDataDir = File(context.filesDir, "activity_data")

    init {
        // Create the directory if it doesn't exist
        if (!activityDataDir.exists()) {
            activityDataDir.mkdirs()
        }
    }

    /**
     * Retrieves all activity records as a Flow.
     * @return A Flow emitting a list of all ActivityRecords.
     */
    fun getAllActivityRecords(): Flow<List<ActivityRecord>> {
        return activityRecordDao.getAllActivityRecords()
    }

    fun getActivitiesBetweenDates(start: Date, end: Date): Flow<List<ActivityRecord>> {
        return activityRecordDao.getActivityRecordsByDateRange(start.time, end.time)
    }


    /**
     * Retrieves a specific activity record by its ID.
     * @param id The ID of the activity record.
     * @return A Flow emitting the ActivityRecord if found, or null.
     */
    fun getActivityRecordById(id: Long): Flow<ActivityRecord?> {
        return activityRecordDao.getActivityRecordById(id)
    }

    /**
     * Adds a new activity, including saving its GPS and inertial data to JSON files.
     *
     * @param name Optional name for the activity.
     * @param date The start date and time of the activity.
     * @param stepCount Total steps for the activity.
     * @param elapsedTimeMs Elapsed time in milliseconds.
     * @param distanceMeters Optional distance in meters.
     * @param gpsPoints List of LocationPoint objects.
     * @param inertialData InertialSensorData object.
     * @return The ID of the newly created activity record, or -1 if an error occurred.
     */
    suspend fun addActivity(
        name: String,
        date: Date,
        stepCount: Int,
        elapsedTimeMs: Long,
        distanceMeters: Double?,
        gpsPoints: List<LocationPoint>,
        inertialData: InertialSensorData
    ): Long {
        return withContext(Dispatchers.IO) { // Perform file and DB operations on IO dispatcher
            try {
                // Create a preliminary record to get an ID (or use a placeholder ID strategy)
                // For simplicity, we'll insert a placeholder then update, or generate filenames first.
                // Let's generate filenames based on timestamp + random to avoid needing an ID first.
                val timestampSuffix = System.currentTimeMillis()
                val gpsFileName = "walk_${timestampSuffix}_gps.json"
                val inertialFileName = "walk_${timestampSuffix}_inertial.json"

                val gpsFile = File(activityDataDir, gpsFileName)
                val inertialFile = File(activityDataDir, inertialFileName)

                // Save GPS data to JSON
                val gpsJson = gson.toJson(gpsPoints)
                gpsFile.writeText(gpsJson)

                // Save inertial data to JSON
                val inertialJson = gson.toJson(inertialData)
                inertialFile.writeText(inertialJson)

                // Create the full activity record
                val newRecord = ActivityRecord(
                    name = name,
                    date = date,
                    stepCount = stepCount,
                    elapsedTimeMs = elapsedTimeMs,
                    gpsFilePath = gpsFile.absolutePath,
                    inertialFilePath = inertialFile.absolutePath,
                    distanceMeters = distanceMeters
                )

                // Insert into database and return the new ID
                activityRecordDao.insertActivityRecord(newRecord)
            } catch (e: IOException) {
                // Handle file IO exception (e.g., log error)
                e.printStackTrace()
                -1L // Indicate failure
            } catch (e: Exception) {
                // Handle other exceptions
                e.printStackTrace()
                -1L // Indicate failure
            }
        }
    }

    suspend fun updateActivityRecord(record: ActivityRecord) {
        withContext(Dispatchers.IO) {
            activityRecordDao.updateActivityRecord(record)
        }
    }

    suspend fun deleteActivityById(id: Long) {
        withContext(Dispatchers.IO) {
            // Get the record first
            val record = activityRecordDao.getActivityRecordById(id).first()
            record?.let {
                deleteActivityRecord(it)
            }
        }
    }

    suspend fun deleteActivityRecord(record: ActivityRecord) {
        withContext(Dispatchers.IO) {
            try {
                // Delete JSON files
                val gpsFile = File(record.gpsFilePath)
                if (gpsFile.exists()) {
                    gpsFile.delete()
                }

                val inertialFile = File(record.inertialFilePath)
                if (inertialFile.exists()) {
                    inertialFile.delete()
                }

                // Delete record from database
                activityRecordDao.deleteActivityRecord(record)
            } catch (e: Exception) {
                // Handle exceptions (e.g., log error)
                e.printStackTrace()
            }
        }
    }

    suspend fun loadGpsData(record: ActivityRecord): List<LocationPoint>? {
        return withContext(Dispatchers.IO) {
            try {
                val gpsFile = File(record.gpsFilePath)
                if (gpsFile.exists()) {
                    val jsonString = gpsFile.readText()
                    val typeToken = object : TypeToken<List<LocationPoint>>() {}.type
                    gson.fromJson<List<LocationPoint>>(jsonString, typeToken)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun loadInertialData(record: ActivityRecord): InertialSensorData? {
        return withContext(Dispatchers.IO) {
            try {
                val inertialFile = File(record.inertialFilePath)
                if (inertialFile.exists()) {
                    val jsonString = inertialFile.readText()
                    gson.fromJson(jsonString, InertialSensorData::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun deleteAllActivities() {
        withContext(Dispatchers.IO) {
            // First, get all records to delete their files
            val allRecords = activityRecordDao.getAllActivityRecords()

            // Safer: Iterate through files in the directory and delete them
            activityDataDir.listFiles()?.forEach { file ->
                file.delete()
            }
            // Then delete all records from the database
            activityRecordDao.deleteAllActivityRecords()
        }
    }
}
