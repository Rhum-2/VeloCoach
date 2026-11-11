package com.velocoach.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.data.BlockType
import com.velocoach.app.data.WorkoutBlockEntity
import com.velocoach.app.domain.WorkoutMath
import com.velocoach.app.ui.VeloCoachViewModelFactory
import com.velocoach.app.ui.components.PowerProfileChart
import com.velocoach.app.ui.components.colorFor
import com.velocoach.app.ui.components.labelFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: WorkoutDetailViewModel = run {
        val app = LocalContext.current.applicationContext as VeloCoachApp
        viewModel(key = "detail-$workoutId", factory = VeloCoachViewModelFactory(app, workoutId))
    }
) {
    val workout by viewModel.workout.collectAsStateWithLifecycle()
    val ftp by viewModel.ftp.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout?.workout?.name ?: "Séance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                    }
                    IconButton(onClick = { workout?.let { onEdit(it.workout.id) } }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Modifier")
                    }
                }
            )
        }
    ) { padding ->
        val current = workout
        if (current == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (current.workout.description.isNotBlank()) {
                    item {
                        Text(
                            text = current.workout.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Profil de puissance", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            PowerProfileChart(
                                blocks = current.blocks,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Durée",
                            value = WorkoutMath.formatDuration(WorkoutMath.totalDurationSeconds(current.blocks)),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Intensité",
                            value = "${WorkoutMath.averagePowerPercent(current.blocks)} % FTP",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Blocs",
                            value = current.blocks.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text("Structure", style = MaterialTheme.typography.titleMedium)
                }
                itemsIndexed(current.blocks.sortedBy { it.orderIndex }) { _, block ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colorFor(block.type), CircleShape)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column {
                                Text(labelFor(block.type), style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = blockDescription(block, ftp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Supprimer cette séance ?") },
            text = { Text("Elle sera aussi retirée de ton planning.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(onBack)
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun blockDescription(block: WorkoutBlockEntity, ftp: Int): String {
    val power = WorkoutMath.formatPowerRange(block.powerLow, block.powerHigh)
    val watts = if (ftp > 0) " · ${block.powerLow * ftp / 100}–${block.powerHigh * ftp / 100} W" else ""
    return if (block.type == BlockType.INTERVAL) {
        val recovery = if (block.recoverySeconds > 0) " · récup ${block.recoverySeconds} s" else ""
        "${block.repetitions} × ${block.durationSeconds} s @ $power$watts$recovery"
    } else {
        "${WorkoutMath.formatDuration(block.durationSeconds)} @ $power$watts"
    }
}
