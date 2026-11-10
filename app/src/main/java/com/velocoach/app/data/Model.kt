package com.velocoach.app.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class BlockType {
    WARMUP,
    STEADY,
    INTERVAL,
    RECOVERY,
    COOLDOWN
}

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class WorkoutBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val orderIndex: Int,
    val type: BlockType,
    val durationSeconds: Int,
    val powerLow: Int,
    val powerHigh: Int,
    val repetitions: Int = 1,
    val recoverySeconds: Int = 0
)

@Entity(
    tableName = "plan_entries",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["epochDay"], unique = true), Index("workoutId")]
)
data class PlanEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val workoutId: Long
)

data class WorkoutWithBlocks(
    @Embedded val workout: WorkoutEntity,
    @Relation(parentColumn = "id", entityColumn = "workoutId")
    val blocks: List<WorkoutBlockEntity>
)

data class PlanEntryWithWorkout(
    @Embedded val entry: PlanEntryEntity,
    @Relation(entity = WorkoutEntity::class, parentColumn = "workoutId", entityColumn = "id")
    val workout: WorkoutWithBlocks?
)
