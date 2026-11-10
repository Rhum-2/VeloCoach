package com.velocoach.app.ui.navigation

object Routes {
    const val PLAN = "plan"
    const val LIBRARY = "library"
    const val WORKOUT_DETAIL = "workout/{workoutId}"
    const val WORKOUT_EDITOR = "workout/edit?workoutId={workoutId}"

    fun detail(workoutId: Long) = "workout/$workoutId"
    fun editor(workoutId: Long) = "workout/edit?workoutId=$workoutId"
}
