package org.example.kra_rosl_9.logic

import kotlinx.coroutines.*
import org.example.kra_rosl_9.neighborhood.NeighborhoodContext
import org.example.kra_rosl_9.rules.AutomationRule
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

class Engine<T> (
    val size: Int,
    initialGrid: Grid<T>,
) {
    private var currentGrid = initialGrid
    private var generationCounter = 0
    val nextGrid = Grid(size) { _, _ -> initialGrid.getValue(0, 0) }

    // новое состояние полч
    suspend fun step(
        rule: AutomationRule<T>,
        aliveValue: T,
        ): Grid<T> = coroutineScope {
            // Временная инициализация

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

                            val context = object : NeighborhoodContext<T> {
                                override val centralState: T = currentGrid.getValue(x, y)
                                override fun getNeighbor(dx: Int, dy: Int): T =
                                    currentGrid.getValue(x + dx, y + dy)
                            }

                            val newState = rule.calculateNewState(context)

                            nextGrid.setValue(x, y, newState)
                            if (newState == aliveValue) aliveCount.incrementAndGet()
                        }
                    }
                }
            }

            tasks.awaitAll()
        }
        currentGrid.copyFrom(nextGrid)
        generationCounter++

        println("\nАУДИТ ПОКОЛЕНИЯ $generationCounter: " +
                "\nПравило [${rule.name}] выполнено за $time мс, " +
                "\nСтатистика: живых клеток = ${aliveCount.get()}, мертвых = ${size * size - aliveCount.get()}" +
                "\nИспользовано $numCores ядер, " +
                "\nРазмер области: ${size}x${size}\n")

        return@coroutineScope nextGrid
    }

    fun resetAudit() {
        generationCounter = 0
    }
}