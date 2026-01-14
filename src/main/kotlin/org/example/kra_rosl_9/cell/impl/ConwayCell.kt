package org.example.kra_rosl_9.cell.impl

import org.example.kra_rosl_9.cell.ICellLife
import org.example.kra_rosl_9.logic.ReadableGrid

class ConwayCell (override val isAlive: Boolean) : ICellLife<ConwayCell> {
    val name = "Conway"

    override fun calculateNext(grid: ReadableGrid<ConwayCell>, x: Int, y: Int): ConwayCell {
        var aliveNeighbors = 0

        // вызываем функцию, которая уже есть в интерфейсе
        forEachMooreNeighbor(grid, x, y) { neighbor ->
            if (neighbor.isAlive) aliveNeighbors++
        }

        val nextState =
            if (isAlive)
                aliveNeighbors in 2..3
            else
                aliveNeighbors == 3

        return ConwayCell(nextState)
    }
}