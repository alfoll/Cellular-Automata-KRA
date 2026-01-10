package org.example.kra_rosl_9.rules.impl

import org.example.kra_rosl_9.neighborhood.NeighborhoodContext
import org.example.kra_rosl_9.rules.AutomationRule

class DayAndNightRule : AutomationRule<Int> {
    override val name = "DayAndNight"

    override fun calculateNewState(context: NeighborhoodContext<Int>): Int {
        var aliveNeighbors = 0

        // окрестность - квадрат
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                if (context.getNeighbor(dx, dy) == 1) aliveNeighbors++
            }
        }

        return if (context.centralState == 1) {
            // выживание: 3, 4, 6, 7, 8
            if (aliveNeighbors == 3 || aliveNeighbors == 4 || aliveNeighbors >= 6) 1 else 0
        } else {
            // рождение: 3, 6, 7, 8
            if (aliveNeighbors == 3 || aliveNeighbors >= 6) 1 else 0
        }
    }
}