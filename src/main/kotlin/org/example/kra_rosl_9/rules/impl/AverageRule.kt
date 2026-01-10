package org.example.kra_rosl_9.rules.impl

import org.example.kra_rosl_9.neighborhood.NeighborhoodContext
import org.example.kra_rosl_9.rules.AutomationRule

class AverageRule : AutomationRule<Double> {
    override val name = "Average"

    override fun calculateNewState(context: NeighborhoodContext<Double>): Double {
        // окрестность - крест
        val up = context.getNeighbor(0, 1)
        val down = context.getNeighbor(0, -1)
        val left = context.getNeighbor(-1, 0)
        val right = context.getNeighbor(1, 0)

        return (up + down + left + right) / 4.0
    }
}