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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.domtech.medtracker.ui.components.MedsMeScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var isPm by remember { mutableStateOf(false) }
    var daysMask by remember { mutableStateOf(Days.weekdaysMask()) }
    var doseToConfirm by remember { mutableStateOf<Double?>(null) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var restockAmount by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showManualLogDialog by remember { mutableStateOf(false) }
    var manualLogTime by remember { mutableStateOf("") }
    var isManualPm by remember { mutableStateOf(false) }
    var manualLogAmount by remember { mutableStateOf("") }
    var showAllIntakes by remember { mutableStateOf(false) }

    val dismissNotifications = {
        val nm = androidx.core.app.NotificationManagerCompat.from(context)
        reminders.forEach { nm.cancel(it.id.toInt()) }
    }

    val m = med
    val medColor = if (m != null) Color(m.colorArgb) else MaterialTheme.colorScheme.primary

    MedsMeScaffold(
        title = m?.name ?: stringResource(R.string.medication),
        onBack = onBack,
        actions = {
            IconButton(onClick = { 
                val now = java.time.LocalTime.now()
                val h24 = now.hour
                isManualPm = h24 >= 12
                val h12 = when {
                    h24 == 0 -> 12
                    h24 > 12 -> h24 - 12
                    else -> h24
                }
                manualLogTime = "%d:%02d".format(java.util.Locale.US, h12, now.minute)
                manualLogAmount = med?.doseAmount?.toString() ?: ""
                showManualLogDialog = true 
            }) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = stringResource(R.string.record_past_intake),
                )
            }
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
        modifier = modifier,
    ) { padding ->
        val m = med
        if (m == null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text(stringResource(R.string.not_found))
            }
            return@MedsMeScaffold
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
                color = if (low) MaterialTheme.colorScheme.error else medColor,
            )

            Text(
                text = stringResource(R.string.current_level, FormatUtils.formatDose(m.currentLevel, m.doseUnit)),
                style = MaterialTheme.typography.titleLarge.copy(color = medColor),
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

            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.dose_inventory), style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { 
                                doseToConfirm = m.doseAmount
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(stringResource(R.string.take_dose, FormatUtils.formatDose(m.doseAmount, m.doseUnit)))
                        }
                        OutlinedButton(
                            onClick = { 
                                restockAmount = ""
                                showRestockDialog = true 
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text(stringResource(R.string.restock))
                        }
                    }
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reminders), style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), modifier = Modifier.weight(1f))
                        IconButton(onClick = { showAddReminder = !showAddReminder }) {
                            Icon(
                                imageVector = if (showAddReminder) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = stringResource(if (showAddReminder) R.string.cancel else R.string.add),
                                tint = if (showAddReminder) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showAddReminder,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = timeText,
                                    onValueChange = { timeText = it },
                                    label = { Text(stringResource(R.string.time_hh_mm)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                
                                SingleChoiceSegmentedButtonRow {
                                    SegmentedButton(
                                        selected = !isPm,
                                        onClick = { isPm = false },
                                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                    ) {
                                        Text("AM")
                                    }
                                    SegmentedButton(
                                        selected = isPm,
                                        onClick = { isPm = true },
                                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                    ) {
                                        Text("PM")
                                    }
                                }
                            }

                            DaysPicker(mask = daysMask, onChange = { daysMask = it })

                            Button(
                                onClick = {
                                    val minutes = parse12HourMinutes(timeText, isPm) ?: 8 * 60
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
                                        text = "${FormatUtils.formatTime12h(r.minutesOfDay)} • ${Days.formatMask(context, r.daysOfWeekMask)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = if (r.enabled) stringResource(R.string.enabled) else stringResource(R.string.disabled),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { vm.deleteReminder(r.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.recent_intakes),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        if (recent.size > 3) {
                            TextButton(onClick = { showAllIntakes = !showAllIntakes }) {
                                Text(if (showAllIntakes) stringResource(R.string.show_less) else stringResource(R.string.show_more))
                            }
                        }
                    }
                    
                    if (recent.isEmpty()) {
                        Text(
                            stringResource(R.string.no_intake_history),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        val displayCount = if (showAllIntakes) recent.size else 3
                        recent.take(displayCount).forEach { e ->
                            Text(
                                text = "- ${FormatUtils.formatDose(e.amount, m.doseUnit)} at ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(e.takenAtEpochMs))}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            if (m.notes.isNotBlank()) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.notes), style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
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
                            dismissNotifications()
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

    if (showManualLogDialog) {
        AlertDialog(
            onDismissRequest = { showManualLogDialog = false },
            title = { Text(stringResource(R.string.record_intake)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = manualLogAmount,
                        onValueChange = { manualLogAmount = it },
                        label = { Text(stringResource(R.string.dose_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = manualLogTime,
                            onValueChange = { manualLogTime = it },
                            label = { Text(stringResource(R.string.intake_time)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = !isManualPm,
                                onClick = { isManualPm = false },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("AM")
                            }
                            SegmentedButton(
                                selected = isManualPm,
                                onClick = { isManualPm = true },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("PM")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = manualLogAmount.toDoubleOrNull() ?: 0.0
                        val minutes = parse12HourMinutes(manualLogTime, isManualPm) ?: 0
                        
                        val timestamp = java.time.LocalDate.now()
                            .atTime(java.time.LocalTime.of(minutes / 60, minutes % 60))
                            .atZone(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()

                        vm.takeDose(amount, timestamp)
                        dismissNotifications()
                        manualLogAmount = ""
                        manualLogTime = ""
                        showManualLogDialog = false
                    },
                    enabled = manualLogAmount.toDoubleOrNull() != null && parse12HourMinutes(manualLogTime, isManualPm) != null
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualLogDialog = false }) {
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

private fun parse12HourMinutes(text: String, isPm: Boolean): Int? {
    val parts = text.trim().split(":")
    if (parts.isEmpty()) return null
    
    val h = parts[0].toIntOrNull() ?: return null
    val m = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0
    
    if (h !in 1..12 || m !in 0..59) return null
    
    var hour24 = h
    if (isPm) {
        if (h < 12) hour24 += 12
    } else {
        if (h == 12) hour24 = 0
    }
    
    return hour24 * 60 + m
    }