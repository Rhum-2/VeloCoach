package com.velocoach.app.gps

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

object RideSession {

    data class TrackPoint(
        val lat: Double,
        val lon: Double,
        val elevation: Double,
        val timeMillis: Long
    )

    data class RideStats(
        val isRecording: Boolean = false,
        val startedAt: Long = 0L,
        val elapsedSeconds: Long = 0L,
        val distanceMeters: Double = 0.0,
        val currentSpeedKmh: Double = 0.0,
        val maxSpeedKmh: Double = 0.0,
        val points: List<TrackPoint> = emptyList()
    )

    private val _state = MutableStateFlow(RideStats())
    val state: StateFlow<RideStats> = _state.asStateFlow()

    fun start() {
        _state.value = RideStats(isRecording = true, startedAt = System.currentTimeMillis())
    }

    fun onLocation(location: Location) {
        val current = _state.value
        if (!current.isRecording) return

        val point = TrackPoint(
            lat = location.latitude,
            lon = location.longitude,
            elevation = location.altitude,
            timeMillis = location.time
        )
        val last = current.points.lastOrNull()
        var distance = current.distanceMeters
        if (last != null) {
            val results = FloatArray(1)
            Location.distanceBetween(last.lat, last.lon, point.lat, point.lon, results)
            distance += results[0].toDouble()
        }
        val speedKmh = location.speed * 3.6

        _state.value = current.copy(
            elapsedSeconds = (System.currentTimeMillis() - current.startedAt) / 1000,
            distanceMeters = distance,
            currentSpeedKmh = speedKmh,
            maxSpeedKmh = max(current.maxSpeedKmh, speedKmh),
            points = current.points + point
        )
    }

    fun finish(): RideStats {
        val stats = _state.value
        _state.value = RideStats()
        return stats
    }
}
