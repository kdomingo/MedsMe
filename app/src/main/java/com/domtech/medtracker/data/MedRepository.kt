package com.domtech.medtracker.data

import kotlinx.coroutines.flow.Flow

class MedRepository(
    private val db: AppDatabase,
) {
    private val meds = db.medicationDao()
    private val reminders = db.reminderDao()

    fun observeMeds(): Flow<List<MedicationEntity>> = meds.observeAll()
    fun observeMed(id: Long): Flow<MedicationEntity?> = meds.observeById(id)
    fun observeRemindersForMed(medId: Long): Flow<List<IntakeReminderEntity>> =
        reminders.observeForMedication(medId)

    fun observeRecentIntakes(medId: Long, limit: Int = 20): Flow<List<IntakeEventEntity>> =
        reminders.observeRecentIntakes(medId, limit)

    suspend fun saveMedication(
        id: Long,
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
        nowEpochMs: Long,
    ): Long {
        val existing = if (id != 0L) meds.getById(id) else null
        val entity = MedicationEntity(
            id = id,
            name = name.trim(),
            doseAmount = doseAmount,
            doseUnit = doseUnit.trim(),
            frequency = frequency,
            hourlyInterval = hourlyInterval,
            dailyInterval = dailyInterval,
            notes = notes.trim(),
            currentLevel = currentLevel,
            lowLevelThreshold = lowLevelThreshold,
            lastTakenEpochMs = existing?.lastTakenEpochMs ?: 0L,
            colorArgb = colorArgb,
            updatedAtEpochMs = nowEpochMs,
        )
        return meds.upsert(entity)
    }

    suspend fun deleteMedication(id: Long) = meds.delete(id)

    suspend fun setLevel(medId: Long, level: Double, nowEpochMs: Long) =
        meds.updateLevel(medId, level, nowEpochMs)

    suspend fun takeDose(medId: Long, amount: Double, nowEpochMs: Long) {
        reminders.recordIntakeAndDecrement(
            medicationDao = meds,
            medId = medId,
            amount = amount,
            nowEpochMs = nowEpochMs,
        )
    }

    suspend fun upsertReminder(
        id: Long,
        medId: Long,
        minutesOfDay: Int,
        daysOfWeekMask: Int,
        enabled: Boolean,
    ): Long {
        val entity = IntakeReminderEntity(
            id = id,
            medId = medId,
            minutesOfDay = minutesOfDay.coerceIn(0, 1439),
            daysOfWeekMask = daysOfWeekMask,
            enabled = enabled,
        )
        return reminders.upsertReminder(entity)
    }

    suspend fun deleteReminder(id: Long) = reminders.deleteReminder(id)

    suspend fun listEnabledReminders(): List<IntakeReminderEntity> = reminders.listEnabled()
    suspend fun listLowStock(): List<MedicationEntity> = meds.listLowStock()
}

