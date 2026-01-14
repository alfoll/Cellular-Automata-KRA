package org.example.kra_rosl_9.cell

import org.example.kra_rosl_9.logic.ReadableGrid

interface ICellLife <T : ICellLife<T>> {

    val isAlive: Boolean
    fun calculateNext(grid: ReadableGrid<T>, x: Int, y: Int): T

    fun forEachMooreNeighbor(
        grid: ReadableGrid<T>,
        centerX: Int,
        centerY: Int,
        radius: Int = 1, // По умолчанию радиус 1 (окрестность 3х3)
        action: (T) -> Unit
    ) {
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                if (dx == 0 && dy == 0) continue // Пропускаем саму клетку

                // Берем соседа. ReadableGrid сам обработает границы (тор)
                val neighbor = grid.getValue(centerX + dx, centerY + dy)
                action(neighbor)
            }
        }
    }

    fun forEachVonNeumannNeighbor(
        grid: ReadableGrid<T>,
        centerX: Int, centerY: Int,
        radius: Int = 1,
        action: (T) -> Unit
    ) {
        for (i in 1..radius) {
            // Список относительных координат для "креста"
            val neighbors = listOf(
                centerX + i to centerY,     // Справа
                centerX - i to centerY,     // Слева
                centerX to centerY + i,     // Снизу
                centerX to centerY - i      // Сверху
            )
            neighbors.forEach { (nx, ny) ->
                action(grid.getValue(nx, ny))
            }
        }
    }
}