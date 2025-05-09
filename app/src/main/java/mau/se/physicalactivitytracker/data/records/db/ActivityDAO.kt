package mau.se.physicalactivitytracker.data.record.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mau.se.physicalactivitytracker.data.record.model.ActivityRecord

/**
 * Data Access Object (DAO) for the ActivityRecord entity.
 * Defines database operations for activity records.
 */
@Dao
interface ActivityRecordDao {

    /**
     * Inserts a new activity record into the database.
     * If a record with the same ID already exists, it will be replaced.
     * @param record The ActivityRecord to insert.
     * @return The row ID of the newly inserted record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityRecord(record: ActivityRecord): Long

    /**
     * Updates an existing activity record in the database.
     * @param record The ActivityRecord to update.
     */
    @Update
    suspend fun updateActivityRecord(record: ActivityRecord)

    /**
     * Deletes an activity record from the database.
     * @param record The ActivityRecord to delete.
     */
    @Delete
    suspend fun deleteActivityRecord(record: ActivityRecord)

    /**
     * Retrieves an activity record by its ID.
     * @param id The ID of the record to retrieve.
     * @return A Flow emitting the ActivityRecord if found, or null otherwise.
     */
    @Query("SELECT * FROM activity_records WHERE id = :id")
    fun getActivityRecordById(id: Long): Flow<ActivityRecord?>

    /**
     * Retrieves all activity records from the database, ordered by date in descending order.
     * @return A Flow emitting a list of all ActivityRecords.
     */
    @Query("SELECT * FROM activity_records ORDER BY date DESC")
    fun getAllActivityRecords(): Flow<List<ActivityRecord>>

    /**
     * Retrieves activity records within a specific date range.
     * @param startDate The start date of the range (inclusive).
     * @param endDate The end date of the range (inclusive).
     * @return A Flow emitting a list of ActivityRecords within the specified date range.
     */
    @Query("SELECT * FROM activity_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getActivityRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<ActivityRecord>>

    /**
     * Deletes all activity records from the database.
     * Use with caution.
     */
    @Query("DELETE FROM activity_records")
    suspend fun deleteAllActivityRecords()
}