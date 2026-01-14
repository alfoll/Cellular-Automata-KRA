package org.example.kra_rosl_9.cell.impl

import org.example.kra_rosl_9.cell.ICellLife
import org.example.kra_rosl_9.logic.ReadableGrid

class AverageCell(val value: Double) : ICellLife<AverageCell> {
    override val isAlive: Boolean = value > 0.2

    val name = "Average"

    override fun calculateNext(grid: ReadableGrid<AverageCell>, x: Int, y: Int): AverageCell {
        var sum = 0.0

        forEachVonNeumannNeighbor(grid, x, y, radius = 1) { neighbor ->
            sum += neighbor.value
        }

        // Возвращаем новую клетку со средним значением
        return AverageCell(sum / 4.0)
    }
}