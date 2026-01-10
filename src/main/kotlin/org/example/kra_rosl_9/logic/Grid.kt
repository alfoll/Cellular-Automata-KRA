package org.example.kra_rosl_9.logic

class Grid<T> (
    val size: Int,
    private val initialValues: (Int, Int) -> T,
) {
    // один массив для матрицы
    private val cells: Array<Any?> = Array(size * size) { i ->
        initialValues(i / size, i % size)
    }

    private fun getIndex(x: Int, y: Int): Int {
        // % size чтобы быть внутр массива
        val safeX = ((x % size) + size) % size
        val safeY = ((y % size) + size) % size
        return safeX * size + safeY
    }

    @Suppress("UNCHECKED_CAST")
    fun getValue(x: Int, y: Int): T =
        cells[getIndex(x, y)] as T

    fun setValue(x: Int, y: Int, value: T) {
        cells[getIndex(x, y)] = value
    }

    fun copyFrom(other: Grid<T>) {
        System.arraycopy(other.cells, 0, this.cells, 0, cells.size)
    }
}