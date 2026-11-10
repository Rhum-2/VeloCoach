package com.velocoach.app.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.velocoach.app.data.WorkoutWithBlocks
import com.velocoach.app.domain.WorkoutMath
import com.velocoach.app.domain.Weeks

@Composable
fun PlanScreen(
    onOpenWorkout: (Long) -> Unit,
    onCreateWorkout: () -> Unit,
    viewModel: PlanViewModel = viewModel()
) {
    val weekStart by viewModel.weekStart.collectAsStateWithLifecycle()
    val plan by viewModel.plan.collectAsStateWithLifecycle()
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    var assignDay by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = viewModel::previousWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Semaine précédente")
            }
            Text(
                text = Weeks.weekTitle(weekStart),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = viewModel::nextWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Semaine suivante")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed((0..6).toList()) { _, index ->
                val epochDay = weekStart + index
                val workout = plan[epochDay]
                DayCard(
                    epochDay = epochDay,
                    workout = workout,
                    onClick = {
                        if (workout != null) onOpenWorkout(workout.workout.id) else assignDay = epochDay
                    },
                    onRemove = { viewModel.remove(epochDay) }
                )
            }
        }
    }

    assignDay?.let { day ->
        AssignWorkoutDialog(
            workouts = workouts,
            onDismiss = { assignDay = null },
            onAssign = { workoutId ->
                viewModel.assign(day, workoutId)
                assignDay = null
            },
            onCreateWorkout = {
                assignDay = null
                onCreateWorkout()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayCard(
    epochDay: Long,
    workout: WorkoutWithBlocks?,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
                Text(
                    text = Weeks.dayLabel(epochDay),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Weeks.dayNumber(epochDay),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (workout != null) {
                    Text(
                        text = workout.workout.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${WorkoutMath.formatDuration(WorkoutMath.totalDurationSeconds(workout.blocks))} · " +
                            "${WorkoutMath.averagePowerPercent(workout.blocks)} % FTP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Repos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Toucher pour planifier",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (workout != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Retirer la séance")
                }
            }
        }
    }
}

@Composable
private fun AssignWorkoutDialog(
    workouts: List<WorkoutWithBlocks>,
    onDismiss: () -> Unit,
    onAssign: (Long) -> Unit,
    onCreateWorkout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assigner une séance") },
        text = {
            if (workouts.isEmpty()) {
                Column {
                    Text("Aucune séance dans ta bibliothèque.")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onCreateWorkout) { Text("Créer une séance") }
                }
            } else {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(workouts, key = { it.workout.id }) { workout ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(workout.workout.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = WorkoutMath.formatDuration(
                                        WorkoutMath.totalDurationSeconds(workout.blocks)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { onAssign(workout.workout.id) }) { Text("Choisir") }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
