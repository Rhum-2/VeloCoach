package com.velocoach.app.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.data.WorkoutWithBlocks
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as VeloCoachApp).repository

    val workouts: StateFlow<List<WorkoutWithBlocks>> = repo.observeWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(workoutId: Long) {
        viewModelScope.launch { repo.deleteWorkout(workoutId) }
    }
}
