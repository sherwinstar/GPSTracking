package com.rollingbeast
import com.huawei.hms.location.*

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val altitude: Double,
    val horizontalAccuracy: Float,
    val verticalAccuracy: Double,
    val course: Float,
    val timestamp: Long,
) {
    companion object {
        fun fromLocation(data: LocationResult): LocationData {
            // huawei
           return LocationData(
             data.lastHWLocation.latitude,
             data.lastHWLocation.longitude,
             if (data.lastHWLocation.speed <= 0) 0.0 else data.lastHWLocation.speed * 3.6,
             data.lastHWLocation.altitude,
             data.lastHWLocation.accuracy,
             data.lastHWLocation.verticalAccuracyMeters.toDouble(),
             data.lastHWLocation.bearing,
             System.currentTimeMillis(),
           )
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "speed" to speed,
            "altitude" to altitude,
            "horizontalAccuracy" to horizontalAccuracy,
            "verticalAccuracy" to verticalAccuracy,
            "course" to course,
            "timestamp" to timestamp,
        )
    }
}
