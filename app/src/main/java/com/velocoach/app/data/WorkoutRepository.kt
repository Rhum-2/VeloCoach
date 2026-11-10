package com.velocoach.app.data

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val planDao: PlanDao
) {

    fun observeWorkouts() = workoutDao.observeAll()

    fun observeWorkout(id: Long) = workoutDao.observeById(id)

    suspend fun getWorkoutOnce(id: Long) = workoutDao.getById(id)

    suspend fun getAllWorkoutsOnce() = workoutDao.getAllOnce()

    fun observePlanBetween(from: Long, to: Long) = planDao.observeBetween(from, to)

    suspend fun saveWorkout(workout: WorkoutEntity, blocks: List<WorkoutBlockEntity>): Long {
        return if (workout.id == 0L) {
            val id = workoutDao.insertWorkout(workout)
            workoutDao.insertBlocks(blocks.map { it.copy(id = 0, workoutId = id) })
            id
        } else {
            workoutDao.updateWorkout(workout)
            workoutDao.deleteBlocksFor(workout.id)
            workoutDao.insertBlocks(blocks.map { it.copy(id = 0, workoutId = workout.id) })
            workout.id
        }
    }

    suspend fun deleteWorkout(id: Long) = workoutDao.deleteWorkout(id)

    suspend fun assignToDay(epochDay: Long, workoutId: Long) {
        planDao.upsert(PlanEntryEntity(epochDay = epochDay, workoutId = workoutId))
    }

    suspend fun removeFromDay(epochDay: Long) = planDao.deleteForDay(epochDay)
}
