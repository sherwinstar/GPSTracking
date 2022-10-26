package com.rollingbeast

enum class TrackingDataSource { GPS, Altimeter }

data class TrackingData(
    val locationData: LocationData,
    val relativeAltitudeData: RelativeAltitudeData,
    val timestamp: Long = System.currentTimeMillis(),
    val source: TrackingDataSource,
)

fun TrackingData.toMap(): Map<String, Any> {
    return mapOf(
        "latitude" to locationData.latitude,
        "longitude" to locationData.longitude,
        "speed" to locationData.speed,
        "altitude" to locationData.altitude,
        "horizontalAccuracy" to locationData.horizontalAccuracy,
        "verticalAccuracy" to locationData.verticalAccuracy,
        "course" to locationData.course,
        "gpsTimestamp" to locationData.timestamp,
        "relativeAltitude" to relativeAltitudeData.relativeAltitude,
        "relativeAltitudeTimestamp" to relativeAltitudeData.timestamp,
        "timestamp" to timestamp,
        "source" to if (source == TrackingDataSource.GPS) "gps" else "altimeter",
    )
}
