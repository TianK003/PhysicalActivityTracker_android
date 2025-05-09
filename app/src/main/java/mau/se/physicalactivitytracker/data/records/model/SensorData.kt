package mau.se.physicalactivitytracker.data.records.model

// Represents a single GPS data point for JSON serialization
data class LocationPoint(
    val timestamp: Long,    // Milliseconds since epoch
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,  // In meters
    val accuracy: Float?    // In meters
)

// Represents a single accelerometer data point for JSON serialization
data class AccelerometerData(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)

// Represents a single gyroscope data point for JSON serialization
data class GyroscopeData(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)

// Represents a single step detector event for JSON serialization
data class StepDetectorEvent(
    val timestamp: Long
    // You could add more info here if the sensor provides it, e.g., step confidence
)

// Container for all inertial data for a walk, for JSON serialization
data class InertialSensorData(
    val accelerometerReadings: List<AccelerometerData> = emptyList(),
    val gyroscopeReadings: List<GyroscopeData> = emptyList(),
    val stepDetectorEvents: List<StepDetectorEvent> = emptyList()
    // Add other sensor data lists here as needed (e.g., magnetometer)
)
