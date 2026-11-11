package com.velocoach.app.ui.ride

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.velocoach.app.data.RideEntity
import com.velocoach.app.domain.WorkoutMath
import com.velocoach.app.gps.LocationTrackingService
import com.velocoach.app.gps.RideSession
import com.velocoach.app.util.GpxExporter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideScreen(
    viewModel: RideViewModel = viewModel()
) {
    val context = LocalContext.current
    val rides by viewModel.rides.collectAsStateWithLifecycle()
    val session by RideSession.state.collectAsStateWithLifecycle()

    val now by produceState(System.currentTimeMillis(), session.isRecording) {
        if (session.isRecording) {
            while (true) {
                value = System.currentTimeMillis()
                delay(500)
            }
        }
    }
    val elapsedSeconds = if (session.isRecording) (now - session.startedAt) / 1000 else 0L

    fun startRide() {
        ContextCompat.startForegroundService(
            context,
            Intent(context, LocationTrackingService::class.java)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) startRide()
    }

    fun requestStart() {
        val needed = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }
        val allGranted = needed.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) startRide() else permissionLauncher.launch(needed.toTypedArray())
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sortie") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (session.isRecording) {
                            Text(
                                "● ENREGISTREMENT",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                formatStopwatch(elapsedSeconds),
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                LiveStat("%.2f km".format(session.distanceMeters / 1000), "Distance")
                                LiveStat("%.1f km/h".format(session.currentSpeedKmh), "Vitesse")
                                LiveStat("%.1f km/h".format(session.maxSpeedKmh), "Max")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    context.startService(
                                        Intent(context, LocationTrackingService::class.java)
                                            .setAction(LocationTrackingService.ACTION_STOP)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.padding(start = 4.dp))
                                Text("Terminer la sortie")
                            }
                        } else {
                            Text("Prêt à rouler ?", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { requestStart() }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.padding(start = 4.dp))
                                Text("Démarrer la sortie")
                            }
                        }
                    }
                }
            }

            item {
                Text("Historique", style = MaterialTheme.typography.titleMedium)
            }

            if (rides.isEmpty()) {
                item {
                    Text(
                        "Aucune sortie enregistrée.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(rides, key = { it.id }) { ride ->
                    RideCard(ride = ride, onShare = { GpxExporter.share(context, ride) })
                }
            }
        }
    }
}

@Composable
private fun LiveStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RideCard(ride: RideEntity, onShare: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SimpleDateFormat("EEE d MMM · HH:mm", Locale.FRENCH)
                        .format(Date(ride.startedAt)),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "%.2f km · %s · %.1f km/h moy".format(
                        ride.distanceMeters / 1000,
                        WorkoutMath.formatDuration(ride.durationSeconds.toInt()),
                        ride.avgSpeedKmh
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "Exporter en GPX")
            }
        }
    }
}

private fun formatStopwatch(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
