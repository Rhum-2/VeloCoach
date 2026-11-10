package com.velocoach.app.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velocoach.app.VeloCoachApp
import com.velocoach.app.data.BlockType
import com.velocoach.app.data.WorkoutBlockEntity
import com.velocoach.app.data.WorkoutEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorState(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val blocks: List<WorkoutBlockEntity> = emptyList()
)

class WorkoutEditorViewModel(
    private val app: VeloCoachApp,
    workoutId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    init {
        if (workoutId > 0) {
            viewModelScope.launch {
                app.repository.getWorkoutOnce(workoutId)?.let { existing ->
                    _state.value = EditorState(
                        id = existing.workout.id,
                        name = existing.workout.name,
                        description = existing.workout.description,
                        blocks = existing.blocks.sortedBy { it.orderIndex }
                    )
                }
            }
        }
    }

    fun updateName(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun updateDescription(value: String) {
        _state.value = _state.value.copy(description = value)
    }

    fun addBlock(type: BlockType) {
        val block = when (type) {
            BlockType.WARMUP -> simple(600, 40, 60, type)
            BlockType.STEADY -> simple(1800, 60, 70, type)
            BlockType.INTERVAL -> WorkoutBlockEntity(
                workoutId = 0,
                orderIndex = 0,
                type = BlockType.INTERVAL,
                durationSeconds = 300,
                powerLow = 95,
                powerHigh = 100,
                repetitions = 4,
                recoverySeconds = 120
            )
            BlockType.RECOVERY -> simple(180, 45, 55, type)
            BlockType.COOLDOWN -> simple(300, 40, 50, type)
        }
        _state.value = _state.value.copy(blocks = _state.value.blocks + block)
    }

    fun updateBlock(index: Int, block: WorkoutBlockEntity) {
        val blocks = _state.value.blocks.toMutableList()
        if (index in blocks.indices) {
            blocks[index] = block
            _state.value = _state.value.copy(blocks = blocks)
        }
    }

    fun removeBlock(index: Int) {
        val blocks = _state.value.blocks.toMutableList()
        if (index in blocks.indices) {
            blocks.removeAt(index)
            _state.value = _state.value.copy(blocks = blocks)
        }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val current = _state.value
            val workout = WorkoutEntity(
                id = current.id,
                name = current.name.ifBlank { "Séance sans nom" },
                description = current.description
            )
            val ordered = current.blocks.mapIndexed { index, block -> block.copy(orderIndex = index) }
            app.repository.saveWorkout(workout, ordered)
            onDone()
        }
    }

    private fun simple(
        durationSeconds: Int,
        powerLow: Int,
        powerHigh: Int,
        type: BlockType
    ) = WorkoutBlockEntity(
        workoutId = 0,
        orderIndex = 0,
        type = type,
        durationSeconds = durationSeconds,
        powerLow = powerLow,
        powerHigh = powerHigh
    )
}
