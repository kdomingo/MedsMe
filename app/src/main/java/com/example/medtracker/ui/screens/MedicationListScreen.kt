package com.example.medtracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medtracker.data.MedicationEntity
import com.example.medtracker.ui.LocalRepository
import com.example.medtracker.ui.viewmodel.MedListViewModel
import com.example.medtracker.ui.viewmodel.SimpleVmFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val repo = LocalRepository.current
    val vm: MedListViewModel = viewModel(factory = SimpleVmFactory { MedListViewModel(repo) })
    val meds by vm.meds.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Medications") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add medication",
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
                Text("No medications yet. Tap + to add one.")
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
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun MedicationRow(
    med: MedicationEntity,
    onClick: () -> Unit,
) {
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        text = "Level: ${med.currentLevel} • Low at ${med.lowLevelThreshold}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (low) {
                    Text(
                        text = "LOW",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            LevelBar(fraction = fraction, color = barColor)
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
            .height(10.dp)
            .clip(shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .padding(0.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(10.dp)
                .clip(shape),
        ) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                drawRoundRect(color = color, cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f, 999f))
            }
        }
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.06f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f, 999f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f),
            )
        }
    }
}

