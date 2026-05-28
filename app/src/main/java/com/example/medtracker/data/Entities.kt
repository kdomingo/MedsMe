package com.example.medtracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    indices = [
        Index("name"),
    ],
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val doseAmount: Double,
    val doseUnit: String,
    val notes: String,
    val currentLevel: Double,
    val lowLevelThreshold: Double,
    val updatedAtEpochMs: Long,
)

@Entity(
    tableName = "intake_events",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("medId"),
        Index("takenAtEpochMs"),
    ],
)
data class IntakeEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medId: Long,
    val amount: Double,
    val takenAtEpochMs: Long,
)

/**
 * Reminder is stored as minutes since midnight (0..1439) with a days-of-week bitmask.
 * Bit positions: 0=Mon ... 6=Sun.
 */
@Entity(
    tableName = "intake_reminders",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("medId"),
        Index("enabled"),
    ],
)
data class IntakeReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medId: Long,
    val minutesOfDay: Int,
    val daysOfWeekMask: Int,
    val enabled: Boolean,
)

