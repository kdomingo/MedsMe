package com.domtech.medtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.domtech.medtracker.R
import com.domtech.medtracker.ui.util.Days
import com.domtech.medtracker.ui.util.FormatUtils
import com.domtech.medtracker.ui.util.formatMinutesOfDay
import com.domtech.medtracker.ui.viewmodel.MedDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailsScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm: MedDetailsViewModel = hiltViewModel()

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val med = uiState.med
    val reminders = uiState.reminders
    val recent = uiState.recentIntakes

    var showAddReminder by remember { mutableStateOf(false) }
    var timeText by remember { mutableStateOf("08:00") }
    var daysMask by remember { mutableStateOf(Days.weekdaysMask()) }
    var doseToConfirm by remember { mutableStateOf<Double?>(null) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var restockAmount by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(med?.name ?: stringResource(R.string.medication)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit),
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val m = med
        if (m == null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text(stringResource(R.string.not_found))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val low = m.currentLevel <= m.lowLevelThreshold
            Text(
                text = if (low) stringResource(R.string.low_stock_alert) else stringResource(R.string.in_stock),
                color = if (low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            )

            val maxForBar = (m.lowLevelThreshold.coerceAtLeast(1.0) * 2.0)
            val frac = (m.currentLevel / maxForBar).toFloat().coerceIn(0f, 1f)
            LevelBar(
                fraction = frac,
                color = if (low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )

            Text(
                text = stringResource(R.string.current_level, FormatUtils.formatDose(m.currentLevel, m.doseUnit)),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.frequency_label, FormatUtils.formatFrequency(context, m)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.restock_threshold_label, m.lowLevelThreshold.toString()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.dose_inventory), style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { doseToConfirm = m.doseAmount },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.take_dose, FormatUtils.formatDose(m.doseAmount, m.doseUnit)))
                        }
                        OutlinedButton(
                            onClick = { 
                                restockAmount = ""
                                showRestockDialog = true 
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.restock))
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reminders), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { showAddReminder = !showAddReminder }) {
                            Text(if (showAddReminder) stringResource(R.string.cancel) else stringResource(R.string.add))
                        }
                    }

                    AnimatedVisibility(
                        visible = showAddReminder,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = timeText,
                                onValueChange = { timeText = it },
                                label = { Text(stringResource(R.string.time_hh_mm)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )

                            DaysPicker(mask = daysMask, onChange = { daysMask = it })

                            Button(
                                onClick = {
                                    val minutes = parseMinutesOfDayOrNull(timeText) ?: 8 * 60
                                    vm.addOrUpdateReminder(
                                        reminderId = 0L,
                                        minutesOfDay = minutes,
                                        daysOfWeekMask = daysMask,
                                        enabled = true,
                                    )
                                    showAddReminder = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.save_reminder))
                            }
                        }
                    }

                    if (reminders.isEmpty()) {
                        Text(
                            stringResource(R.string.no_reminders),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        reminders.forEach { r ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${formatMinutesOfDay(r.minutesOfDay)} • ${Days.formatMask(context, r.daysOfWeekMask)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = if (r.enabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                OutlinedButton(onClick = { vm.deleteReminder(r.id) }) {
                                    Text(stringResource(R.string.delete))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.recent_intakes), style = MaterialTheme.typography.titleMedium)
                    if (recent.isEmpty()) {
                        Text(
                            stringResource(R.string.no_intake_history),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        recent.take(10).forEach { e ->
                            Text(
                                text = "- ${FormatUtils.formatDose(e.amount, m.doseUnit)} at ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(e.takenAtEpochMs))}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            if (m.notes.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.notes), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(m.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    doseToConfirm?.let { amount ->
        val m = med
        val warning = if (m != null) FormatUtils.getNextDoseWarning(context, m, System.currentTimeMillis()) else null
        AlertDialog(
            onDismissRequest = { doseToConfirm = null },
            title = { Text(if (warning != null) stringResource(R.string.early_dose_warning) else stringResource(R.string.confirm_dose)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (warning != null) {
                        Text(
                            text = warning,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        )
                    }
                    Text(stringResource(
                        R.string.confirm_dose_prompt,
                        FormatUtils.formatDose(amount, m?.doseUnit ?: ""),
                        m?.name ?: ""
                    ))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.takeDose(amount)
                        doseToConfirm = null
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { doseToConfirm = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showRestockDialog) {
        AlertDialog(
            onDismissRequest = { showRestockDialog = false },
            title = { Text(stringResource(R.string.restock_title, med?.name ?: "")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.restock_prompt, med?.doseUnit ?: ""))
                    OutlinedTextField(
                        value = restockAmount,
                        onValueChange = { restockAmount = it },
                        label = { Text(stringResource(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = restockAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            vm.restock(amount)
                        }
                        showRestockDialog = false
                    },
                    enabled = restockAmount.toDoubleOrNull() != null
                ) {
                    Text(stringResource(R.string.add_to_inventory))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestockDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirmation_title)) },
            text = {
                Text(stringResource(R.string.delete_confirmation_text, med?.name ?: ""))
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteMedication { onBack() }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DaysPicker(mask: Int, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.days), style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Days.all.forEach { day ->
                val selected = (mask and day.bit) != 0
                if (selected) {
                    Button(
                        onClick = { onChange(Days.toggle(mask, day.bit)) },
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Text(day.shortLabel, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onChange(Days.toggle(mask, day.bit)) },
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Text(day.shortLabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun parseMinutesOfDayOrNull(text: String): Int? {
    val parts = text.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}