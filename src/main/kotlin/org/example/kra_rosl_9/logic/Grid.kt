package org.example.kra_rosl_9.logic

class Grid<T> (
    override val size: Int,
    private val initialValues: (Int, Int) -> T,
) : ReadableGrid<T> {
    init {
        require(size > 0) { "Размер сетки должен быть положительным (передано: $size)" }
    }

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
    override fun getValue(x: Int, y: Int): T =
        cells[getIndex(x, y)] as T

    fun setValue(x: Int, y: Int, value: T) {
        cells[getIndex(x, y)] = value
    }

    fun copyFrom(other: Grid<T>) {
        require(other.size == this.size) { "Размеры сеток не совпадают для копирования" }
        System.arraycopy(other.cells, 0, this.cells, 0, cells.size)
    }
}