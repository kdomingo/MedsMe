package com.domtech.medtracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.domtech.medtracker.data.Frequency
import com.domtech.medtracker.data.IntakeEventEntity
import com.domtech.medtracker.data.IntakeReminderEntity
import com.domtech.medtracker.data.MedRepository
import com.domtech.medtracker.data.MedicationEntity
import com.domtech.medtracker.domain.models.MedicationSuggestion
import com.domtech.medtracker.domain.repository.SuggestionRepository
import com.domtech.medtracker.reminders.ReminderScheduler
import com.domtech.medtracker.ui.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedListUiState(
    val meds: List<MedicationEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class MedListViewModel @Inject constructor(
    private val repo: MedRepository,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    
    val uiState: StateFlow<MedListUiState> = combine(
        repo.observeMeds(),
        _searchQuery
    ) { meds, query ->
        val filtered = if (query.isBlank()) meds else {
            meds.filter { it.name.contains(query, ignoreCase = true) }
        }
        MedListUiState(meds = filtered, searchQuery = query)
    }.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5_000), 
        MedListUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun takeDose(medId: Long, amount: Double, timestampMs: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repo.takeDose(medId, amount, timestampMs)
        }
    }
}

data class MedDetailsUiState(
    val med: MedicationEntity? = null,
    val reminders: List<IntakeReminderEntity> = emptyList(),
    val recentIntakes: List<IntakeEventEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MedDetailsViewModel @Inject constructor(
    private val repo: MedRepository,
    private val scheduler: ReminderScheduler,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route: Route.Details = savedStateHandle.toRoute()
    private val medId = route.id

    val uiState: StateFlow<MedDetailsUiState> = combine(
        repo.observeMed(medId),
        repo.observeRemindersForMed(medId),
        repo.observeRecentIntakes(medId)
    ) { med, reminders, recent ->
        MedDetailsUiState(med, reminders, recent)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedDetailsUiState(isLoading = true))

    fun takeDose(amount: Double, timestampMs: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repo.takeDose(medId, amount, timestampMs)
        }
    }

    fun restock(amount: Double) {
        viewModelScope.launch {
            val current = uiState.value.med?.currentLevel ?: 0.0
            repo.setLevel(medId, current + amount, System.currentTimeMillis())
        }
    }

    fun deleteMedication(onDeleted: () -> Unit) {
        viewModelScope.launch {
            // First cancel all reminders
            uiState.value.reminders.forEach { 
                scheduler.cancelReminder(it.id)
            }
            repo.deleteMedication(medId)
            onDeleted()
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

data class EditMedicationUiState(
    val existing: MedicationEntity? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class EditMedicationViewModel @Inject constructor(
    private val repo: MedRepository,
    private val suggestionRepo: SuggestionRepository,
    private val scheduler: ReminderScheduler,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // Both Add and Edit routes use this ViewModel.
    private val medId: Long? = runCatching { savedStateHandle.toRoute<Route.Edit>().id }.getOrNull()

    val uiState: StateFlow<EditMedicationUiState> = (medId?.let { repo.observeMed(it) } ?: flowOf(null))
        .map { EditMedicationUiState(existing = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EditMedicationUiState(isLoading = medId != null))

    private val _nameInput = MutableStateFlow("")
    val nameInput: StateFlow<String> = _nameInput

    @OptIn(ExperimentalCoroutinesApi::class)
    val suggestions: StateFlow<List<MedicationSuggestion>> = _nameInput
        .flatMapLatest { query ->
            if (query.length < 2) flowOf(emptyList())
            else flowOf(suggestionRepo.getSuggestions(query))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onNameChange(newName: String) {
        _nameInput.value = newName
    }

    fun save(
        name: String,
        doseAmount: Double,
        doseUnit: String,
        frequency: Frequency,
        hourlyInterval: Int,
        dailyInterval: Int,
        notes: String,
        currentLevel: Double,
        lowLevelThreshold: Double,
        colorArgb: Int,
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
                dailyInterval = dailyInterval,
                notes = notes,
                currentLevel = currentLevel,
                lowLevelThreshold = lowLevelThreshold,
                colorArgb = colorArgb,
                nowEpochMs = System.currentTimeMillis(),
            )
            // Ensure any enabled reminders remain scheduled (covers edit cases).
            scheduler.rescheduleAllEnabled()
            onSaved(id)
        }
    }
}