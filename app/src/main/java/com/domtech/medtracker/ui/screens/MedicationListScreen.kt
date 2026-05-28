package com.domtech.medtracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.domtech.medtracker.data.MedicationEntity
import com.domtech.medtracker.ui.LocalRepository
import com.domtech.medtracker.ui.util.FormatUtils
import com.domtech.medtracker.ui.viewmodel.MedListViewModel
import com.domtech.medtracker.ui.viewmodel.SimpleVmFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

import androidx.compose.ui.res.stringResource
import com.domtech.medtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = LocalRepository.current
    val vm: MedListViewModel = viewModel(factory = SimpleVmFactory { MedListViewModel(repo) })
    val meds by vm.meds.collectAsStateWithLifecycle()

    var medicationToTake by remember { mutableStateOf<MedicationEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.medications)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_medication_desc),
                )
            }
        },
    ) { padding ->
        if (meds.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.no_medications))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(items = meds, key = { it.id }) { med ->
                    MedicationRow(
                        med = med,
                        onClick = { onOpen(med.id) },
                        onTake = { medicationToTake = med },
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    medicationToTake?.let { med ->
        val warning = FormatUtils.getNextDoseWarning(context, med, System.currentTimeMillis())
        AlertDialog(
            onDismissRequest = { medicationToTake = null },
            title = { Text(if (warning != null) stringResource(R.string.early_dose_warning) else stringResource(R.string.confirm_dose)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (warning != null) {
                        Text(
                            text = warning,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(stringResource(
                        R.string.confirm_dose_prompt,
                        FormatUtils.formatDose(med.doseAmount, med.doseUnit),
                        med.name
                    ))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.takeDose(med.id, med.doseAmount)
                        medicationToTake = null
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { medicationToTake = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun MedicationRow(
    med: MedicationEntity,
    onClick: () -> Unit,
    onTake: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fractionTarget = (med.currentLevel / (med.lowLevelThreshold.coerceAtLeast(1.0) * 2.0))
        .toFloat()
        .coerceIn(0f, 1f)

    val fraction by animateFloatAsState(
        targetValue = fractionTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "levelFraction",
    )

    val low = med.currentLevel <= med.lowLevelThreshold
    val barColor by animateColorAsState(
        targetValue = if (low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        label = "barColor",
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${FormatUtils.formatFrequency(context, med)} • Level: ${FormatUtils.formatDose(med.currentLevel, med.doseUnit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (low) {
                    Text(
                        text = stringResource(R.string.low_stock),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LevelBar(
                    fraction = fraction,
                    color = barColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = onTake,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(stringResource(R.string.take), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
internal fun LevelBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(12.dp)
                .clip(shape)
                .background(color),
        )
    }
}

