package com.velocoach.app.gps

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.data.RideEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationManager: LocationManager? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location) {
            RideSession.onLocation(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTracking()
        } else {
            startTracking()
        }
        return START_NOT_STICKY
    }

    private fun startTracking() {
        if (RideSession.state.value.isRecording) return

        createChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Enregistrement en cours")
            .setContentText("Sortie vélo en cours d'enregistrement")
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)

        RideSession.start()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                locationListener
            )
        } catch (e: SecurityException) {
            stopTracking()
        }
    }

    private fun stopTracking() {
        locationManager?.removeUpdates(locationListener)
        locationManager = null

        val stats = RideSession.finish()
        if (stats.points.size >= 2) {
            serviceScope.launch {
                val pointsJson = JSONArray().apply {
                    stats.points.forEach { point ->
                        put(
                            JSONObject()
                                .put("lat", point.lat)
                                .put("lon", point.lon)
                                .put("ele", point.elevation)
                                .put("t", point.timeMillis)
                        )
                    }
                }.toString()
                val duration = maxOf(stats.elapsedSeconds, 1L)
                val avgSpeed = stats.distanceMeters / 1000.0 / (duration / 3600.0)
                (application as VeloCoachApp).repository.saveRide(
                    RideEntity(
                        startedAt = stats.startedAt,
                        durationSeconds = duration,
                        distanceMeters = stats.distanceMeters,
                        avgSpeedKmh = avgSpeed,
                        maxSpeedKmh = stats.maxSpeedKmh,
                        pointsJson = pointsJson
                    )
                )
            }
        }

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Enregistrement de sortie",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_STOP = "com.velocoach.app.STOP_TRACKING"
        private const val CHANNEL_ID = "ride_tracking"
        private const val NOTIFICATION_ID = 1
    }
}
