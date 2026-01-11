package org.example.kra_rosl_9.rules.impl

import org.example.kra_rosl_9.logic.ReadableGrid
import org.example.kra_rosl_9.rules.AutomationRule

class SeedsRule : AutomationRule<Int> {
    override val name = "Seeds"

    override fun calculateNewState(grid: ReadableGrid<Int>, x: Int, y: Int): Int {
        var aliveNeighbors = 0

        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                if (grid.getValue(x + dx, y + dy) == 1) aliveNeighbors++
            }
        }
        val currentState = grid.getValue(x, y)
        return if (currentState == 0 && aliveNeighbors == 2) 1 else 0
    }
}