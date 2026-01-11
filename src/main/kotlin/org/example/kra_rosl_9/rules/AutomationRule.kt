package org.example.kra_rosl_9.rules

import org.example.kra_rosl_9.logic.ReadableGrid

interface AutomationRule<T> {

    val name: String
//    fun calculateNewState(currentState: T, neighbors: List<T>): // T - для реализации стратегии
//    fun calculateNewState(context: NeighborhoodContext<T>): T // для реализации контекста
    fun calculateNewState(grid: ReadableGrid<T>, x: Int, y: Int): T
}