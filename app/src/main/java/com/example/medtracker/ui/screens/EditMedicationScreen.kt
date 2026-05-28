package com.example.medtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medtracker.reminders.ReminderScheduler
import com.example.medtracker.ui.LocalRepository
import com.example.medtracker.ui.viewmodel.EditMedicationViewModel
import com.example.medtracker.ui.viewmodel.SimpleVmFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    medId: Long?,
    onDone: () -> Unit,
) {
    val repo = LocalRepository.current
    val scheduler = ReminderScheduler.current()
    val vm: EditMedicationViewModel =
        viewModel(factory = SimpleVmFactory { EditMedicationViewModel(repo, scheduler, medId) })

    val existing by vm.existing.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var doseAmount by remember { mutableStateOf("1") }
    var doseUnit by remember { mutableStateOf("tablet") }
    var notes by remember { mutableStateOf("") }
    var currentLevel by remember { mutableStateOf("0") }
    var lowThreshold by remember { mutableStateOf("0") }

    LaunchedEffect(existing?.id) {
        val e = existing ?: return@LaunchedEffect
        name = e.name
        doseAmount = e.doseAmount.toString()
        doseUnit = e.doseUnit
        notes = e.notes
        currentLevel = e.currentLevel.toString()
        lowThreshold = e.lowLevelThreshold.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (medId == null) "Add medication" else "Edit medication") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = doseAmount,
                    onValueChange = { doseAmount = it },
                    label = { Text("Dose amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = doseUnit,
                    onValueChange = { doseUnit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = currentLevel,
                onValueChange = { currentLevel = it },
                label = { Text("Current level (inventory)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = lowThreshold,
                onValueChange = { lowThreshold = it },
                label = { Text("Low level threshold") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    vm.save(
                        name = name,
                        doseAmount = doseAmount.toDoubleOrNull() ?: 1.0,
                        doseUnit = doseUnit,
                        notes = notes,
                        currentLevel = currentLevel.toDoubleOrNull() ?: 0.0,
                        lowLevelThreshold = lowThreshold.toDoubleOrNull() ?: 0.0,
                    ) { onDone() }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }

            Text(
                text = "Tip: inventory decreases when you tap “Take dose”.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

