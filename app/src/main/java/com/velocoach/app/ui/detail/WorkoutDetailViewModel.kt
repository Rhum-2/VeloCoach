package com.velocoach.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.data.WorkoutWithBlocks
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(
    private val app: VeloCoachApp,
    workoutId: Long
) : ViewModel() {

    val workout: StateFlow<WorkoutWithBlocks?> = app.repository.observeWorkout(workoutId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val ftp: StateFlow<Int> = app.repository.observeProfile()
        .map { it?.ftpWatts ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            val id = workout.value?.workout?.id
            if (id != null) {
                app.repository.deleteWorkout(id)
            }
            onDone()
        }
    }
}
