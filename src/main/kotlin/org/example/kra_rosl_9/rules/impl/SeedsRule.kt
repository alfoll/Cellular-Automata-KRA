package org.example.kra_rosl_9.rules.impl

import org.example.kra_rosl_9.neighborhood.NeighborhoodContext
import org.example.kra_rosl_9.rules.AutomationRule

class SeedsRule : AutomationRule<Int> {
    override val name = "Seeds"

    override fun calculateNewState(context: NeighborhoodContext<Int>): Int {
        var aliveNeighbors = 0

        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                if (context.getNeighbor(dx, dy) == 1) aliveNeighbors++
            }
        }

        return if (context.centralState == 0 && aliveNeighbors == 2) 1 else 0
    }
}