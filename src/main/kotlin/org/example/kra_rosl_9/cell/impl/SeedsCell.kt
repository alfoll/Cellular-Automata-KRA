package org.example.kra_rosl_9.cell.impl

import org.example.kra_rosl_9.cell.ICellLife
import org.example.kra_rosl_9.logic.ReadableGrid

class SeedsCell(override val isAlive: Boolean) : ICellLife<SeedsCell> {
    val name = "Seeds"

    override fun calculateNext(grid: ReadableGrid<SeedsCell>, x: Int, y: Int): SeedsCell {
        var aliveNeighbors = 0

        // вызываем функцию, которая уже есть в интерфейсе
        forEachMooreNeighbor(grid, x, y) { neighbor ->
            if (neighbor.isAlive) aliveNeighbors++
        }

        val nextState = !isAlive && aliveNeighbors == 2

        return SeedsCell(nextState)
    }
}