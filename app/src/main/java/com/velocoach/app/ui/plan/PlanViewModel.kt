package com.velocoach.app.ui.plan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.data.WorkoutWithBlocks
import com.velocoach.app.domain.Weeks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlanViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as VeloCoachApp).repository

    private val _weekStart = MutableStateFlow(Weeks.currentWeekStart())
    val weekStart: StateFlow<Long> = _weekStart.asStateFlow()

    val plan: StateFlow<Map<Long, WorkoutWithBlocks>> = _weekStart
        .flatMapLatest { start -> repo.observePlanBetween(start, start + 6) }
        .map { entries ->
            entries.mapNotNull { entry ->
                entry.workout?.let { workout -> entry.entry.epochDay to workout }
            }.toMap()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val workouts: StateFlow<List<WorkoutWithBlocks>> = repo.observeWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun previousWeek() {
        _weekStart.value -= 7
    }

    fun nextWeek() {
        _weekStart.value += 7
    }

    fun assign(epochDay: Long, workoutId: Long) {
        viewModelScope.launch { repo.assignToDay(epochDay, workoutId) }
    }

    fun remove(epochDay: Long) {
        viewModelScope.launch { repo.removeFromDay(epochDay) }
    }
}
