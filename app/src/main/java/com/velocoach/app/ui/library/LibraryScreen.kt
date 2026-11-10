package com.velocoach.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.velocoach.app.data.WorkoutWithBlocks
import com.velocoach.app.domain.WorkoutMath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenWorkout: (Long) -> Unit,
    onCreateWorkout: () -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mes séances") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateWorkout) {
                Icon(Icons.Filled.Add, contentDescription = "Nouvelle séance")
            }
        }
    ) { padding ->
        if (workouts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aucune séance pour le moment", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Touche + pour créer ta première séance structurée.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workouts, key = { it.workout.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onClick = { onOpenWorkout(workout.workout.id) },
                        onDelete = { viewModel.delete(workout.workout.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutCard(
    workout: WorkoutWithBlocks,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = workout.workout.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${WorkoutMath.formatDuration(WorkoutMath.totalDurationSeconds(workout.blocks))} · " +
                        "${WorkoutMath.averagePowerPercent(workout.blocks)} % FTP · " +
                        "${workout.blocks.size} blocs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
            }
        }
    }
}
