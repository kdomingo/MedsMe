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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.domtech.medtracker.reminders.ReminderScheduler
import com.domtech.medtracker.ui.LocalRepository
import com.domtech.medtracker.ui.util.Days
import com.domtech.medtracker.ui.util.FormatUtils
import com.domtech.medtracker.ui.util.formatMinutesOfDay
import com.domtech.medtracker.ui.viewmodel.MedDetailsViewModel
import com.domtech.medtracker.ui.viewmodel.SimpleVmFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailsScreen(
    medId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val repo = LocalRepository.current
    val scheduler = ReminderScheduler.current()
    val vm: MedDetailsViewModel =
        viewModel(factory = SimpleVmFactory { MedDetailsViewModel(repo, scheduler, medId) })

    val med by vm.med.collectAsStateWithLifecycle()
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val recent by vm.recentIntakes.collectAsStateWithLifecycle()

    var showAddReminder by remember { mutableStateOf(false) }
    var timeText by remember { mutableStateOf("08:00") }
    var daysMask by remember { mutableStateOf(Days.weekdaysMask()) }
    var doseToConfirm by remember { mutableStateOf<Double?>(null) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var restockAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(med?.name ?: "Medication") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                        )
                    }
                },
            )
        },
    ) { padding ->
        val m = med
        if (m == null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text("Not found.")
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
                text = if (low) "LOW STOCK ALERT" else "IN STOCK",
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
                text = "Current Level: ${FormatUtils.formatDose(m.currentLevel, m.doseUnit)}",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Frequency: ${FormatUtils.formatFrequency(m)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Restock threshold: ${m.lowLevelThreshold}",
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
                    Text("Dose & Inventory", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { doseToConfirm = m.doseAmount },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Take ${FormatUtils.formatDose(m.doseAmount, m.doseUnit)}")
                        }
                        OutlinedButton(
                            onClick = { 
                                restockAmount = ""
                                showRestockDialog = true 
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restock")
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Reminders", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { showAddReminder = !showAddReminder }) {
                            Text(if (showAddReminder) "Cancel" else "Add")
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
                                label = { Text("Time (HH:MM)") },
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
                                Text("Save reminder")
                            }
                        }
                    }

                    if (reminders.isEmpty()) {
                        Text(
                            "No reminders yet.",
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
                                        text = "${formatMinutesOfDay(r.minutesOfDay)} • ${Days.formatMask(r.daysOfWeekMask)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = if (r.enabled) "Enabled" else "Disabled",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                OutlinedButton(onClick = { vm.deleteReminder(r.id) }) {
                                    Text("Delete")
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Recent intakes", style = MaterialTheme.typography.titleMedium)
                    if (recent.isEmpty()) {
                        Text(
                            "No intake history yet.",
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
                        Text("Notes", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(m.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    doseToConfirm?.let { amount ->
        val m = med
        val warning = if (m != null) FormatUtils.getNextDoseWarning(m, System.currentTimeMillis()) else null
        AlertDialog(
            onDismissRequest = { doseToConfirm = null },
            title = { Text(if (warning != null) "Early Dose Warning" else "Confirm Dose") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (warning != null) {
                        Text(
                            text = warning,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        )
                    }
                    Text("Are you taking ${FormatUtils.formatDose(amount, m?.doseUnit ?: "")} of ${m?.name}?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.takeDose(amount)
                        doseToConfirm = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { doseToConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRestockDialog) {
        AlertDialog(
            onDismissRequest = { showRestockDialog = false },
            title = { Text("Restock ${med?.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How many ${med?.doseUnit}s are you adding?")
                    OutlinedTextField(
                        value = restockAmount,
                        onValueChange = { restockAmount = it },
                        label = { Text("Amount") },
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
                    Text("Add to Inventory")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DaysPicker(mask: Int, onChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Days", style = MaterialTheme.typography.labelLarge)
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

