package org.example.kra_rosl_9.logic

import kotlinx.coroutines.*
import org.example.kra_rosl_9.cell.ICellLife
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

class Engine<T : ICellLife<T>> (
    private val size: Int,
    initialGrid: Grid<T>,
) {
    init {
        require(initialGrid.size == size) { "Размер начальной сетки не совпадает с размером Engine" }
    }

    var currentGrid = initialGrid
    private var generationCounter = 0
    val nextGrid = Grid(size) { x, y -> initialGrid.getValue(x, y) }

    // новое состояние полч
    suspend fun step(
        onAudit: (String) -> Unit,
        ): Grid<T> = coroutineScope {

        val aliveCount = AtomicInteger(0)

        val numCores = Runtime.getRuntime().availableProcessors()
        val chunkSize = (size / numCores).coerceAtLeast(1)

        val time = measureTimeMillis {

            val tasks = (0 until size step chunkSize).map {
                startX ->
                async(Dispatchers.Default) {
                    val endX = (startX + chunkSize).coerceAtMost(size)

                    for (x in startX until endX) {
                        for (y in 0 until size) {

//                      осталось от реализации с контекстом
//                            val context = object : NeighborhoodContext<T> {
//                                override val centralState: T = currentGrid.getValue(x, y)
//                                override fun getNeighbor(dx: Int, dy: Int): T =
//                                    currentGrid.getValue(x + dx, y + dy)
//                            }

                            try {
                                val currentCell = currentGrid.getValue(x, y)
                                val newState = currentCell.calculateNext(currentGrid, x, y)
                                nextGrid.setValue(x, y, newState)

                                if (newState.isAlive)
                                    aliveCount.incrementAndGet()
                            } catch (e: Exception) {
                                throw IllegalStateException("Ошибка при расчете нового состояния в клетке $x:$y", e)
                            }
                        }
                    }
                }
            }

            tasks.awaitAll()
        }
        currentGrid.copyFrom(nextGrid)
        generationCounter++

        if (generationCounter % 10 == 0 || generationCounter == 1) {
            val report = "\nАУДИТ ПОКОЛЕНИЯ $generationCounter: " +
                    "\nВыполнено за $time мс, " +
                    "\nСтатистика: живых клеток = ${aliveCount.get()}, мертвых = ${size * size - aliveCount.get()}" +
                    "\nИспользовано $numCores ядер, " +
                    "\nРазмер области: ${size}x${size}\n"

            onAudit(report)
        }

        return@coroutineScope nextGrid
    }

    fun resetAudit() {
        generationCounter = 0
    }
}