package com.velocoach.app.ui.editor

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun WorkoutEditorScreen(
    onDone: () -> Unit,
    workoutId: Long = -1L,
    viewModel: WorkoutEditorViewModel = run {
        val app = LocalContext.current.applicationContext as VeloCoachApp
        viewModel(
            key = "editor-$workoutId",
            factory = VeloCoachViewModelFactory(app, workoutId)
        )
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == 0L) "Nouvelle séance" else "Modifier la séance") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save(onDone) }) {
                        Icon(Icons.Filled.Check, contentDescription = "Enregistrer")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Nom de la séance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Column {
                    Text("Profil de puissance", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    PowerProfileChart(
                        blocks = state.blocks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Durée : ${WorkoutMath.formatDuration(WorkoutMath.totalDurationSeconds(state.blocks))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Intensité : ${WorkoutMath.averagePowerPercent(state.blocks)} % FTP",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Column {
                    Text("Ajouter un bloc", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.addBlock(BlockType.WARMUP) },
                            label = { Text("Échauff.") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.addBlock(BlockType.STEADY) },
                            label = { Text("Continu") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.addBlock(BlockType.INTERVAL) },
                            label = { Text("Intervalles") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.addBlock(BlockType.COOLDOWN) },
                            label = { Text("Retour calme") }
                        )
                    }
                }
            }
            item {
                Text("Structure", style = MaterialTheme.typography.titleSmall)
            }
            itemsIndexed(state.blocks) { index, block ->
                BlockCard(
                    block = block,
                    onEdit = { editingIndex = index },
                    onRemove = { viewModel.removeBlock(index) }
                )
            }
        }
    }

    editingIndex?.let { index ->
        val block = state.blocks.getOrNull(index)
        if (block == null) {
            editingIndex = null
        } else {
            BlockEditDialog(
                block = block,
                onDismiss = { editingIndex = null },
                onConfirm = { updated ->
                    viewModel.updateBlock(index, updated)
                    editingIndex = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockCard(
    block: WorkoutBlockEntity,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(labelFor(block.type), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = if (block.type == BlockType.INTERVAL) {
                        "${block.repetitions} × ${block.durationSeconds} s @ " +
                            "${block.powerLow}–${block.powerHigh} % · récup ${block.recoverySeconds} s"
                    } else {
                        "${WorkoutMath.formatDuration(block.durationSeconds)} @ " +
                            "${block.powerLow}–${block.powerHigh} % FTP"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Modifier le bloc")
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Supprimer le bloc")
            }
        }
    }
}

@Composable
private fun BlockEditDialog(
    block: WorkoutBlockEntity,
    onDismiss: () -> Unit,
    onConfirm: (WorkoutBlockEntity) -> Unit
) {
    val isInterval = block.type == BlockType.INTERVAL
    var durationMinutes by remember { mutableStateOf((block.durationSeconds / 60).toString()) }
    var durationSeconds by remember { mutableStateOf(block.durationSeconds.toString()) }
    var powerLow by remember { mutableStateOf(block.powerLow.toString()) }
    var powerHigh by remember { mutableStateOf(block.powerHigh.toString()) }
    var repetitions by remember { mutableStateOf(block.repetitions.toString()) }
    var recovery by remember { mutableStateOf(block.recoverySeconds.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(labelFor(block.type)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isInterval) {
                    NumberField("Durée d'un effort (s)", durationSeconds) { durationSeconds = it }
                    NumberField("Répétitions", repetitions) { repetitions = it }
                    NumberField("Récupération entre efforts (s)", recovery) { recovery = it }
                } else {
                    NumberField("Durée (min)", durationMinutes) { durationMinutes = it }
                }
                NumberField("Puissance min (% FTP)", powerLow) { powerLow = it }
                NumberField("Puissance max (% FTP)", powerHigh) { powerHigh = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newBlock = if (isInterval) {
                    block.copy(
                        durationSeconds = (durationSeconds.toIntOrNull() ?: block.durationSeconds)
                            .coerceAtLeast(1),
                        repetitions = (repetitions.toIntOrNull() ?: block.repetitions).coerceAtLeast(1),
                        recoverySeconds = (recovery.toIntOrNull() ?: block.recoverySeconds).coerceAtLeast(0),
                        powerLow = (powerLow.toIntOrNull() ?: block.powerLow).coerceIn(0, 400),
                        powerHigh = (powerHigh.toIntOrNull() ?: block.powerHigh).coerceIn(0, 400)
                    )
                } else {
                    block.copy(
                        durationSeconds = ((durationMinutes.toIntOrNull() ?: (block.durationSeconds / 60)) * 60)
                            .coerceAtLeast(60),
                        powerLow = (powerLow.toIntOrNull() ?: block.powerLow).coerceIn(0, 400),
                        powerHigh = (powerHigh.toIntOrNull() ?: block.powerHigh).coerceIn(0, 400)
                    )
                }
                onConfirm(newBlock)
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }.take(5)) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
