package com.domtech.medtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.domtech.medtracker.data.Frequency
import com.domtech.medtracker.data.IntakeReminderEntity
import com.domtech.medtracker.data.MedRepository
import com.domtech.medtracker.data.MedicationEntity
import com.domtech.medtracker.reminders.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedListViewModel(
    private val repo: MedRepository,
) : ViewModel() {
    val meds: StateFlow<List<MedicationEntity>> =
        repo.observeMeds().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun takeDose(medId: Long, amount: Double) {
        viewModelScope.launch {
            repo.takeDose(medId, amount, System.currentTimeMillis())
        }
    }
}

class MedDetailsViewModel(
    private val repo: MedRepository,
    private val scheduler: ReminderScheduler,
    private val medId: Long,
) : ViewModel() {
    val med: StateFlow<MedicationEntity?> =
        repo.observeMed(medId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val reminders: StateFlow<List<IntakeReminderEntity>> =
        repo.observeRemindersForMed(medId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentIntakes =
        repo.observeRecentIntakes(medId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun takeDose(amount: Double) {
        viewModelScope.launch {
            repo.takeDose(medId, amount, System.currentTimeMillis())
        }
    }

    fun restock(amount: Double) {
        viewModelScope.launch {
            val current = med.value?.currentLevel ?: 0.0
            repo.setLevel(medId, current + amount, System.currentTimeMillis())
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            repo.deleteReminder(reminderId)
            scheduler.cancelReminder(reminderId)
        }
    }

    fun addOrUpdateReminder(
        reminderId: Long,
        minutesOfDay: Int,
        daysOfWeekMask: Int,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            val id = repo.upsertReminder(
                id = reminderId,
                medId = medId,
                minutesOfDay = minutesOfDay,
                daysOfWeekMask = daysOfWeekMask,
                enabled = enabled,
            )
            if (enabled) scheduler.scheduleReminder(id) else scheduler.cancelReminder(id)
        }
    }
}

class EditMedicationViewModel(
    private val repo: MedRepository,
    private val scheduler: ReminderScheduler,
    private val medId: Long?,
) : ViewModel() {
    val existing: StateFlow<MedicationEntity?> =
        (medId?.let { repo.observeMed(it) } ?: kotlinx.coroutines.flow.flowOf(null))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(
        name: String,
        doseAmount: Double,
        doseUnit: String,
        frequency: Frequency,
        hourlyInterval: Int,
        notes: String,
        currentLevel: Double,
        lowLevelThreshold: Double,
        onSaved: (Long) -> Unit,
    ) {
        viewModelScope.launch {
            val id = repo.saveMedication(
                id = medId ?: 0L,
                name = name,
                doseAmount = doseAmount,
                doseUnit = doseUnit,
                frequency = frequency,
                hourlyInterval = hourlyInterval,
                notes = notes,
                currentLevel = currentLevel,
                lowLevelThreshold = lowLevelThreshold,
                nowEpochMs = System.currentTimeMillis(),
            )
            // Ensure any enabled reminders remain scheduled (covers edit cases).
            scheduler.rescheduleAllEnabled()
            onSaved(id)
        }
    }
}

class SimpleVmFactory<T : ViewModel>(
    private val create: () -> T,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
}

