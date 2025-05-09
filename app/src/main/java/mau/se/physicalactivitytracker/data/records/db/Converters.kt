package mau.se.physicalactivitytracker.data.records.db

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room database.
 * Allows Room to store complex types that it doesn't support natively.
 */
class Converters {
    /**
     * Converts a Long timestamp to a Date object.
     * @param value The Long timestamp.
     * @return The corresponding Date object, or null if the input was null.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts a Date object to a Long timestamp.
     * @param date The Date object.
     * @return The corresponding Long timestamp, or null if the input was null.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}