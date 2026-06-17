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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.domtech.medtracker.R
import com.domtech.medtracker.data.MedicationEntity
import com.domtech.medtracker.ui.util.FormatUtils
import com.domtech.medtracker.ui.viewmodel.MedListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm: MedListViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val meds = uiState.meds

    var medicationToTake by remember { mutableStateOf<MedicationEntity?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.searchQuery,
                        onQueryChange = { vm.onSearchQueryChange(it) },
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.search_medications)) },
                        leadingIcon = { 
                            IconButton(onClick = onMenuClick) {
                                Icon(androidx.compose.material.icons.Icons.Default.Menu, contentDescription = stringResource(R.string.menu)) 
                            }
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { vm.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                    )
                },
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = { }
            )
        },
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
            EmptyState(
                searchQuery = uiState.searchQuery,
                padding = padding
            )
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
private fun EmptyState(
    searchQuery: String,
    padding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (searchQuery.isEmpty()) Icons.Default.Medication else Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                text = if (searchQuery.isEmpty()) {
                    stringResource(R.string.no_medications)
                } else {
                    stringResource(R.string.no_search_results, searchQuery)
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MedicationRow(
    med: MedicationEntity,
    onClick: () -> Unit,
    onTake: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val medColor = Color(med.colorArgb)
    
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
        targetValue = if (low) MaterialTheme.colorScheme.error else medColor,
        label = "barColor",
    )

    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(medColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = medColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = FormatUtils.formatFrequency(context, med),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                
                if (low) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.low_stock),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = stringResource(R.string.current_level, FormatUtils.formatDose(med.currentLevel, med.doseUnit)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LevelBar(
                        fraction = fraction,
                        color = barColor,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = onTake,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(stringResource(R.string.take), style = MaterialTheme.typography.labelLarge)
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