package org.example.kra_rosl_9.cell.impl

import org.example.kra_rosl_9.cell.ICellLife
import org.example.kra_rosl_9.logic.ReadableGrid

class DayAndNightCell(override val isAlive: Boolean) : ICellLife<DayAndNightCell> {
    val name = "DayAndNight"

    override fun calculateNext(grid: ReadableGrid<DayAndNightCell>, x: Int, y: Int): DayAndNightCell {
        var aliveNeighbors = 0

        // вызываем функцию, которая уже есть в интерфейсе
        forEachMooreNeighbor(grid, x, y) { neighbor ->
            if (neighbor.isAlive) aliveNeighbors++
        }

        val nextState =
            if (isAlive)
                aliveNeighbors in listOf(3, 4, 6, 7, 8)
            else
                aliveNeighbors in listOf(3, 6, 7, 8)

        return DayAndNightCell(nextState)
    }
}