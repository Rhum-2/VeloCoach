package com.velocoach.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.velocoach.app.data.BlockType
import com.velocoach.app.data.WorkoutBlockEntity

private data class Segment(
    val duration: Int,
    val low: Int,
    val high: Int,
    val color: Color
)

fun colorFor(type: BlockType): Color = when (type) {
    BlockType.WARMUP -> Color(0xFF42A5F5)
    BlockType.STEADY -> Color(0xFF66BB6A)
    BlockType.INTERVAL -> Color(0xFFFF7043)
    BlockType.RECOVERY -> Color(0xFF9E9E9E)
    BlockType.COOLDOWN -> Color(0xFF26A69A)
}

private fun buildSegments(blocks: List<WorkoutBlockEntity>): List<Segment> = buildList {
    blocks.sortedBy { it.orderIndex }.forEach { block ->
        val reps = if (block.type == BlockType.INTERVAL) block.repetitions.coerceAtLeast(1) else 1
        repeat(reps) { index ->
            add(Segment(block.durationSeconds, block.powerLow, block.powerHigh, colorFor(block.type)))
            if (block.type == BlockType.INTERVAL && index < reps - 1 && block.recoverySeconds > 0) {
                add(Segment(block.recoverySeconds, 45, 55, colorFor(BlockType.RECOVERY)))
            }
        }
    }
}.filter { it.duration > 0 }

@Composable
fun PowerProfileChart(
    blocks: List<WorkoutBlockEntity>,
    modifier: Modifier = Modifier
) {
    val segments = remember(blocks) { buildSegments(blocks) }
    val totalDuration = segments.sumOf { it.duration }.coerceAtLeast(1)
    val maxPower = (segments.maxOfOrNull { it.high } ?: 100).coerceAtLeast(100)

    Canvas(modifier = modifier) {
        val gap = 2f
        val usableWidth = size.width - gap * (segments.size - 1).coerceAtLeast(0)
        var x = 0f
        val chartHeight = size.height

        drawLine(
            color = Color(0x55888888),
            start = Offset(0f, chartHeight * (1f - 100f / maxPower)),
            end = Offset(size.width, chartHeight * (1f - 100f / maxPower)),
            strokeWidth = 2f
        )

        segments.forEach { segment ->
            val width = usableWidth * segment.duration / totalDuration
            val top = chartHeight * (1f - segment.high / maxPower.toFloat())
            val bottom = chartHeight * (1f - segment.low / maxPower.toFloat())
            drawRect(
                color = segment.color,
                topLeft = Offset(x, top),
                size = Size(width, (bottom - top).coerceAtLeast(3f))
            )
            x += width + gap
        }
    }
}
