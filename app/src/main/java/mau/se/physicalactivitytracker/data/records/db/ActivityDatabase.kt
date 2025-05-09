package mau.se.physicalactivitytracker.data.records.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mau.se.physicalactivitytracker.data.records.model.ActivityRecord

/**
 * The Room database for the application.
 * Contains the 'activity_records' table.
 */
@Database(entities = [ActivityRecord::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to the ActivityRecordDao.
     * @return An instance of ActivityRecordDao.
     */
    abstract fun activityRecordDao(): ActivityRecordDao

    companion object {
        // Volatile annotation ensures that writes to this field are immediately visible to other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the AppDatabase.
         * Uses a synchronized block to ensure thread safety during instance creation.
         *
         * @param context The application context.
         * @return The singleton AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            // If INSTANCE is not null, return it.
            // Otherwise, create the database in a synchronized block.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "activity_tracker_database" // Name of the database file
                )
                    // Add migrations here if you change the schema in the future
                    // .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(true) // If migrations are not set, it will recreate the database (data loss)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}