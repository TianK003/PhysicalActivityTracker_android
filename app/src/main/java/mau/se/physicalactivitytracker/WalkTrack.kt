package mau.se.physicalactivitytracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import mau.se.physicalactivitytracker.data.records.db.AppDatabase

class WalkTrack : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                // This ID must match the one used in TrackingService
                "tracking_channel",
                "Activity Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows notifications during activity tracking"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}