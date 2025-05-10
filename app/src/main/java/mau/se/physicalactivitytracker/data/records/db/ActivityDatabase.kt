package mau.se.physicalactivitytracker.data.records.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mau.se.physicalactivitytracker.data.records.model.ActivityRecord

@Database(entities = [ActivityRecord::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun activityRecordDao(): ActivityRecordDao

    companion object {
        // Volatile annotation ensures that writes to this field are immediately visible to other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If INSTANCE is not null, return it.
            // Otherwise, create the database in a synchronized block.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "activity_tracker_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}