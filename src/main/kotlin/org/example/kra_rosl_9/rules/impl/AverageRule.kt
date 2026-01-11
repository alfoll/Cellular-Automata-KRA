package org.example.kra_rosl_9.rules.impl

import org.example.kra_rosl_9.logic.ReadableGrid
import org.example.kra_rosl_9.rules.AutomationRule

class AverageRule : AutomationRule<Double> {
    override val name = "Average"

    override fun calculateNewState(grid: ReadableGrid<Double>, x: Int, y: Int): Double {
        // окрестность - крест
        val up = grid.getValue(x, y + 1)
        val down = grid.getValue(x, y - 1)
        val left = grid.getValue(x - 1, y)
        val right = grid.getValue(x + 1, y)

        return (up + down + left + right) / 4.0
    }
}