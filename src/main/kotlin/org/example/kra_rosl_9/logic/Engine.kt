package org.example.kra_rosl_9.logic

import kotlinx.coroutines.*
import org.example.kra_rosl_9.rules.AutomationRule
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

class Engine<T> (
    private val size: Int,
    initialGrid: Grid<T>,
) {
    init {
        require(initialGrid.size == size) { "Размер начальной сетки не совпадает с размером Engine" }
    }

    var currentGrid = initialGrid
    private var generationCounter = 0
    val nextGrid = Grid(size) { _, _ -> initialGrid.getValue(0, 0) }

    // новое состояние полч
    suspend fun step(
        rule: AutomationRule<T>,
        aliveValue: T,
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
                                val newState = rule.calculateNewState(currentGrid, x, y)
                                nextGrid.setValue(x, y, newState)

                                if (newState == aliveValue) aliveCount.incrementAndGet()
                            } catch (e: Exception) {
                                throw IllegalStateException("Ошибка при расчете правила ${rule.name} в клетке $x:$y", e)
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
                    "\nПравило [${rule.name}] выполнено за $time мс, " +
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