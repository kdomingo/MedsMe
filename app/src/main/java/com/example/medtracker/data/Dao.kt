package com.example.medtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun observeById(id: Long): Flow<MedicationEntity?>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MedicationEntity): Long

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE medications SET currentLevel = :level, updatedAtEpochMs = :now WHERE id = :id")
    suspend fun updateLevel(id: Long, level: Double, now: Long)

    @Query(
        """
        UPDATE medications
        SET currentLevel = MAX(0, currentLevel - :delta),
            updatedAtEpochMs = :now
        WHERE id = :id
        """,
    )
    suspend fun decrementLevel(id: Long, delta: Double, now: Long)

    @Query("SELECT * FROM medications WHERE currentLevel <= lowLevelThreshold")
    suspend fun listLowStock(): List<MedicationEntity>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM intake_reminders WHERE medId = :medId")
    fun observeForMedication(medId: Long): Flow<List<IntakeReminderEntity>>

    @Query("SELECT * FROM intake_reminders WHERE enabled = 1")
    suspend fun listEnabled(): List<IntakeReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminder(entity: IntakeReminderEntity): Long

    @Query("DELETE FROM intake_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntakeEvent(entity: IntakeEventEntity): Long

    @Query("SELECT * FROM intake_events WHERE medId = :medId ORDER BY takenAtEpochMs DESC LIMIT :limit")
    fun observeRecentIntakes(medId: Long, limit: Int = 20): Flow<List<IntakeEventEntity>>

    @Transaction
    suspend fun recordIntakeAndDecrement(
        medicationDao: MedicationDao,
        medId: Long,
        amount: Double,
        nowEpochMs: Long,
    ) {
        insertIntakeEvent(
            IntakeEventEntity(
                medId = medId,
                amount = amount,
                takenAtEpochMs = nowEpochMs,
            ),
        )
        medicationDao.decrementLevel(medId, amount, nowEpochMs)
    }
}

