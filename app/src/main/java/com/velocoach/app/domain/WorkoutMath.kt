package com.velocoach.app.domain

import com.velocoach.app.data.BlockType
import com.velocoach.app.data.WorkoutBlockEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

object WorkoutMath {

    fun blockDurationSeconds(block: WorkoutBlockEntity): Int {
        if (block.type != BlockType.INTERVAL) return block.durationSeconds
        val reps = block.repetitions.coerceAtLeast(1)
        return block.durationSeconds * reps + block.recoverySeconds * (reps - 1)
    }

    fun totalDurationSeconds(blocks: List<WorkoutBlockEntity>): Int =
        blocks.sumOf { blockDurationSeconds(it) }

    fun averagePowerPercent(blocks: List<WorkoutBlockEntity>): Int {
        val total = totalDurationSeconds(blocks)
        if (total == 0) return 0
        val weighted = blocks.sumOf { block ->
            val mid = (block.powerLow + block.powerHigh) / 2.0
            blockDurationSeconds(block) * mid
        }
        return (weighted / total).roundToInt()
    }

    fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return if (h > 0) "${h}h${m.toString().padStart(2, '0')}" else "$m min"
    }

    fun formatPowerRange(low: Int, high: Int): String =
        if (low == high) "$low % FTP" else "$low–$high % FTP"
}

object Weeks {

    fun currentWeekStart(): Long = LocalDate.now().with(DayOfWeek.MONDAY).toEpochDay()

    fun dayLabel(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
            .replaceFirstChar { it.uppercase() }

    fun dayNumber(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).dayOfMonth.toString()

    fun weekTitle(weekStartEpochDay: Long): String {
        val start = LocalDate.ofEpochDay(weekStartEpochDay)
        val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH)
        return "Semaine du ${start.format(formatter)}"
    }
}
