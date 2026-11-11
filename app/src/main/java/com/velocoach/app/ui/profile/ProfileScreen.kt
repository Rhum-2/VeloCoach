package com.velocoach.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var ftpText by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(profile?.ftpWatts) {
        profile?.let { ftpText = if (it.ftpWatts > 0) it.ftpWatts.toString() else "" }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("FTP — Functional Threshold Power", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "La puissance moyenne maximale que tu peux tenir pendant une heure. " +
                            "Elle sert à calibrer l'intensité de tes séances (% FTP) et à afficher des watts cibles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = ftpText,
                        onValueChange = {
                            ftpText = it.filter { char -> char.isDigit() }.take(3)
                            saved = false
                        },
                        label = { Text("FTP (watts)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            ftpText.toIntOrNull()?.let { watts ->
                                viewModel.setFtp(watts)
                                saved = true
                            }
                        },
                        enabled = ftpText.toIntOrNull() != null
                    ) {
                        Text("Enregistrer")
                    }
                    if (saved) {
                        Text(
                            "FTP enregistrée ✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            val currentFtp = profile?.ftpWatts ?: 0
            if (currentFtp > 0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Zones d'entraînement", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ZoneRow("Récupération", "40–55 %", currentFtp * 40 / 100, currentFtp * 55 / 100)
                        ZoneRow("Endurance", "56–75 %", currentFtp * 56 / 100, currentFtp * 75 / 100)
                        ZoneRow("Tempo", "76–90 %", currentFtp * 76 / 100, currentFtp * 90 / 100)
                        ZoneRow("Seuil", "91–105 %", currentFtp * 91 / 100, currentFtp * 105 / 100)
                        ZoneRow("VO2max", "106–120 %", currentFtp * 106 / 100, currentFtp * 120 / 100)
                        ZoneRow("Sprint / PMA", "> 120 %", currentFtp * 121 / 100, currentFtp * 2)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneRow(name: String, percent: String, lowWatts: Int, highWatts: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        Text(
            "$percent · $lowWatts–$highWatts W",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
