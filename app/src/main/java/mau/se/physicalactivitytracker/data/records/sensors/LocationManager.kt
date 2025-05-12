package mau.se.physicalactivitytracker.data.records.sensors

import android.content.Context
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.channels.Channel
import mau.se.physicalactivitytracker.data.records.model.LocationPoint

class LocationManager(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationChannel = Channel<LocationPoint>(Channel.UNLIMITED)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.lastOrNull()?.let { location ->
                locationChannel.trySend(
                    LocationPoint(
                        timestamp = System.currentTimeMillis(),
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        accuracy = location.accuracy
                    )
                )
            }
        }
    }

    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}