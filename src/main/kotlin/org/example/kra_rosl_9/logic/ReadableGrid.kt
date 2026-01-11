package org.example.kra_rosl_9.logic

interface ReadableGrid<T> {
    val size : Int
    fun getValue(x: Int, y: Int): T
}