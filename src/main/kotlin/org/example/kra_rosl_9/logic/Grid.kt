package org.example.kra_rosl_9.logic

import org.example.kra_rosl_9.cell.ICellLife

class Grid<T : ICellLife<T>> (
    override val size: Int,
    private val initialValues: (Int, Int) -> T,
) : ReadableGrid<T> {
    init {
        require(size > 0) { "Размер сетки должен быть положительным (передано: $size)" }
    }

    // один массив для матрицы
    private val cells: MutableList<T> = MutableList(size * size) { index ->
        initialValues(index / size, index % size)
    }

// arrayList можно ли сразу инициализировать как T
//    посмотреть можно ли сделать T (мб наследник от Ilife какого то - вычисл след сост + жив не жив)
//    ячейка знает о себе все сама передавать через контекст окрестность, высчитываем состоения на след итерации

    private fun getIndex(x: Int, y: Int): Int {
        // % size чтобы быть внутр массива
        val safeX = ((x % size) + size) % size
        val safeY = ((y % size) + size) % size
        return safeX * size + safeY
    }

    override fun getValue(x: Int, y: Int): T =
        cells[getIndex(x, y)]

    fun setValue(x: Int, y: Int, value: T) {
        cells[getIndex(x, y)] = value
    }

    fun copyFrom(other: Grid<T>) {
        require(other.size == this.size) { "Размеры сеток не совпадают для копирования" }
//        System.arraycopy(other.cells, 0, this.cells, 0, cells.size)
        for (i in 0 until cells.size) {
            this.cells[i] = other.cells[i]
        }
    }
}