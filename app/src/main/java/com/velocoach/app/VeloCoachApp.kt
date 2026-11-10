package com.velocoach.app

import android.app.Application
import androidx.room.Room
import com.velocoach.app.data.AppDatabase
import com.velocoach.app.data.BlockType
import com.velocoach.app.data.WorkoutBlockEntity
import com.velocoach.app.data.WorkoutEntity
import com.velocoach.app.data.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VeloCoachApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "velocoach.db")
            .build()
    }

    val repository: WorkoutRepository by lazy {
        WorkoutRepository(database.workoutDao(), database.planDao())
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { SeedData.seedIfEmpty(repository) }
    }
}

object SeedData {

    suspend fun seedIfEmpty(repository: WorkoutRepository) {
        if (repository.getAllWorkoutsOnce().isNotEmpty()) return

        val samples = listOf(
            WorkoutEntity(
                name = "Endurance fondamentale — 1h",
                description = "Sortie facile en zone 2, respiration fluide."
            ) to listOf(
                block(0, BlockType.WARMUP, 600, 40, 60),
                block(1, BlockType.STEADY, 2700, 60, 70),
                block(2, BlockType.COOLDOWN, 300, 40, 50)
            ),
            WorkoutEntity(
                name = "Seuil — 3×8 min",
                description = "Trois blocs au seuil avec récupération courte."
            ) to listOf(
                block(0, BlockType.WARMUP, 900, 40, 65),
                block(1, BlockType.INTERVAL, 480, 95, 100, repetitions = 3, recoverySeconds = 180),
                block(2, BlockType.COOLDOWN, 600, 40, 50)
            ),
            WorkoutEntity(
                name = "Sprints courts — 10×30 s",
                description = "Sprints maximaux, récupération complète entre chaque."
            ) to listOf(
                block(0, BlockType.WARMUP, 900, 40, 65),
                block(1, BlockType.INTERVAL, 30, 150, 200, repetitions = 10, recoverySeconds = 90),
                block(2, BlockType.COOLDOWN, 600, 40, 50)
            )
        )

        samples.forEach { (workout, blocks) ->
            repository.saveWorkout(workout, blocks)
        }
    }

    private fun block(
        orderIndex: Int,
        type: BlockType,
        durationSeconds: Int,
        powerLow: Int,
        powerHigh: Int,
        repetitions: Int = 1,
        recoverySeconds: Int = 0
    ) = WorkoutBlockEntity(
        workoutId = 0,
        orderIndex = orderIndex,
        type = type,
        durationSeconds = durationSeconds,
        powerLow = powerLow,
        powerHigh = powerHigh,
        repetitions = repetitions,
        recoverySeconds = recoverySeconds
    )
}
