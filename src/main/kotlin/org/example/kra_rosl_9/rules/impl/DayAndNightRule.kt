package org.example.kra_rosl_9.rules.impl

import org.example.kra_rosl_9.logic.ReadableGrid
import org.example.kra_rosl_9.rules.AutomationRule

class DayAndNightRule : AutomationRule<Int> {
    override val name = "DayAndNight"

    override fun calculateNewState(grid: ReadableGrid<Int>, x: Int, y: Int): Int {
        var aliveNeighbors = 0

        // окрестность - квадрат
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                if (grid.getValue(x + dx, y + dy) == 1) aliveNeighbors++
            }
        }

        val currentState = grid.getValue(x, y)
        return if (currentState == 1) {
            // выживание: 3, 4, 6, 7, 8
            if (aliveNeighbors == 3 || aliveNeighbors == 4 || aliveNeighbors >= 6) 1 else 0
        } else {
            // рождение: 3, 6, 7, 8
            if (aliveNeighbors == 3 || aliveNeighbors >= 6) 1 else 0
        }
    }
}