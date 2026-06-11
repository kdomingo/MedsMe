package com.domtech.medtracker.ui.screens

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
import androidx.compose.material3.FilterChip
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
import com.domtech.medtracker.data.Frequency
import com.domtech.medtracker.ui.viewmodel.EditMedicationViewModel
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.domtech.medtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: EditMedicationViewModel = hiltViewModel()

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val existing = uiState.existing

    var name by remember { mutableStateOf("") }
    var doseAmount by remember { mutableStateOf("1") }
    var doseUnit by remember { mutableStateOf("tablet") }
    var frequency by remember { mutableStateOf(Frequency.DAILY) }
    var hourlyInterval by remember { mutableStateOf("4") }
    var dailyInterval by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var currentLevel by remember { mutableStateOf("0") }
    var lowThreshold by remember { mutableStateOf("0") }

    LaunchedEffect(existing?.id) {
        val e = existing ?: return@LaunchedEffect
        name = e.name
        doseAmount = e.doseAmount.toString()
        doseUnit = e.doseUnit
        frequency = e.frequency
        hourlyInterval = e.hourlyInterval.toString()
        dailyInterval = e.dailyInterval.toString()
        notes = e.notes
        currentLevel = e.currentLevel.toString()
        lowThreshold = e.lowLevelThreshold.toString()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) stringResource(R.string.add_medication) else stringResource(R.string.edit_medication)) },
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
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = doseAmount,
                    onValueChange = { doseAmount = it },
                    label = { Text(stringResource(R.string.dose_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = doseUnit,
                    onValueChange = { doseUnit = it },
                    label = { Text(stringResource(R.string.unit_hint)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            Text(stringResource(R.string.frequency), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Frequency.entries.forEach { f ->
                    FilterChip(
                        selected = frequency == f,
                        onClick = { frequency = f },
                        label = { 
                            val label = when(f) {
                                Frequency.DAILY -> stringResource(R.string.freq_daily)
                                Frequency.HOURLY -> stringResource(R.string.freq_hourly)
                            }
                            Text(label) 
                        }
                    )
                }
            }

            if (frequency == Frequency.HOURLY) {
                OutlinedTextField(
                    value = hourlyInterval,
                    onValueChange = { hourlyInterval = it },
                    label = { Text(stringResource(R.string.every_x_hours)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            } else {
                OutlinedTextField(
                    value = dailyInterval,
                    onValueChange = { dailyInterval = it },
                    label = { Text(stringResource(R.string.every_x_days)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = currentLevel,
                onValueChange = { currentLevel = it },
                label = { Text(stringResource(R.string.current_level_inventory)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = lowThreshold,
                onValueChange = { lowThreshold = it },
                label = { Text(stringResource(R.string.low_level_threshold)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
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
                        frequency = frequency,
                        hourlyInterval = hourlyInterval.toIntOrNull() ?: 0,
                        dailyInterval = dailyInterval.toIntOrNull() ?: 1,
                        notes = notes,
                        currentLevel = currentLevel.toDoubleOrNull() ?: 0.0,
                        lowLevelThreshold = lowThreshold.toDoubleOrNull() ?: 0.0,
                    ) { onDone() }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }

            Text(
                text = stringResource(R.string.inventory_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

