package mau.se.physicalactivitytracker.data.records.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mau.se.physicalactivitytracker.data.records.model.ActivityRecord

@Dao
interface ActivityRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityRecord(record: ActivityRecord): Long

    @Update
    suspend fun updateActivityRecord(record: ActivityRecord)

    @Delete
    suspend fun deleteActivityRecord(record: ActivityRecord)

    @Query("SELECT * FROM activity_records WHERE id = :id")
    fun getActivityRecordById(id: Long): Flow<ActivityRecord?>

    @Query("SELECT * FROM activity_records ORDER BY date DESC")
    fun getAllActivityRecords(): Flow<List<ActivityRecord>>

    @Query("SELECT * FROM activity_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getActivityRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<ActivityRecord>>

    @Query("DELETE FROM activity_records")
    suspend fun deleteAllActivityRecords()
}