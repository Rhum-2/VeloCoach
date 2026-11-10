package com.velocoach.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WorkoutWithBlocks>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeById(id: Long): Flow<WorkoutWithBlocks?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): WorkoutWithBlocks?

    @Transaction
    @Query("SELECT * FROM workouts")
    suspend fun getAllOnce(): List<WorkoutWithBlocks>

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert
    suspend fun insertBlocks(blocks: List<WorkoutBlockEntity>)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM blocks WHERE workoutId = :workoutId")
    suspend fun deleteBlocksFor(workoutId: Long)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun count(): Int
}

@Dao
interface PlanDao {

    @Transaction
    @Query("SELECT * FROM plan_entries WHERE epochDay BETWEEN :from AND :to")
    fun observeBetween(from: Long, to: Long): Flow<List<PlanEntryWithWorkout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: PlanEntryEntity)

    @Query("DELETE FROM plan_entries WHERE epochDay = :epochDay")
    suspend fun deleteForDay(epochDay: Long)
}

@Database(
    entities = [WorkoutEntity::class, WorkoutBlockEntity::class, PlanEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun planDao(): PlanDao
}
