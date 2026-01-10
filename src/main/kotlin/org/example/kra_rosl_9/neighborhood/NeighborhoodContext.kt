package org.example.kra_rosl_9.neighborhood

interface NeighborhoodContext<T> {
    val centralState: T
    // Позволяет получить соседа по относительным координатам
    fun getNeighbor(dx: Int, dy: Int): T
}