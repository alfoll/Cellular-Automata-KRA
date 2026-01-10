package org.example.kra_rosl_9.rules

import org.example.kra_rosl_9.neighborhood.NeighborhoodContext

interface AutomationRule<T> {

    val name: String
//    fun calculateNewState(currentState: T, neighbors: List<T>): T
    fun calculateNewState(context: NeighborhoodContext<T>): T
}