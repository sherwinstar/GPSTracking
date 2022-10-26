package com.rollingbeast

data class RelativeAltitudeData(val relativeAltitude: Double, val timestamp: Long = System.currentTimeMillis()) {
    companion object {
        fun fromRelativeAltitude(relativeAltitude: Double): RelativeAltitudeData {
            return RelativeAltitudeData(relativeAltitude = relativeAltitude)
        }
    }
}
