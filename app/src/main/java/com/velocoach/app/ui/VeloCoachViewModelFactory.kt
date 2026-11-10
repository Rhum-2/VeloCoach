package com.velocoach.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.ui.detail.WorkoutDetailViewModel
import com.velocoach.app.ui.editor.WorkoutEditorViewModel
import com.velocoach.app.ui.library.LibraryViewModel
import com.velocoach.app.ui.plan.PlanViewModel

class VeloCoachViewModelFactory(
    private val app: VeloCoachApp,
    private val workoutId: Long = -1L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        PlanViewModel::class.java -> PlanViewModel(app) as T
        LibraryViewModel::class.java -> LibraryViewModel(app) as T
        WorkoutDetailViewModel::class.java -> WorkoutDetailViewModel(app, workoutId) as T
        WorkoutEditorViewModel::class.java -> WorkoutEditorViewModel(app, workoutId) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
    }
}
