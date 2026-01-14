package org.example.kra_rosl_9

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.kra_rosl_9.cell.impl.ConwayCell
import org.example.kra_rosl_9.cell.impl.SeedsCell
import org.example.kra_rosl_9.cell.impl.DayAndNightCell
import org.example.kra_rosl_9.logic.Engine
import org.example.kra_rosl_9.logic.Grid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@OptIn(ExperimentalCoroutinesApi::class)
class CellularAutomataTest {

    @Test
    @DisplayName("ConwayCell: Смерть от перенаселения (>3 соседей)")
    fun testConwayOverpopulation() {
        val size = 3
        val grid = Grid(size) { x, y ->
            // Центр жив и у него 4 живых соседа
            val isAlive = (x == 1 && y == 1) ||
                    (x == 0 && y == 0) || (x == 0 && y == 1) ||
                    (x == 0 && y == 2) || (x == 1 && y == 0)
            ConwayCell(isAlive)
        }
        val result = grid.getValue(1, 1).calculateNext(grid, 1, 1)
        assertFalse(result.isAlive, "Клетка должна умереть, если соседей больше 3")
    }

    @Test
    @DisplayName("Engine: Сброс аудита")
    fun testEngineReset() {
        val grid = Grid(5) { _, _ -> ConwayCell(false) }
        val engine = Engine(5, grid)
        // Проверяем, что метод существует и вызывается без ошибок
        assertDoesNotThrow { engine.resetAudit() }
    }

    @Test
    @DisplayName("Grid: Проверка корректности размера")
    fun testGridSize() {
        val size = 10
        val grid = Grid(size) { _, _ -> ConwayCell(false) }
        assertEquals(size, grid.size)

        // Проверка на отрицательный размер (должен бросить исключение)
        assertThrows(IllegalArgumentException::class.java) {
            Grid(-1) { _, _ -> ConwayCell(false) }
        }
    }
    @Test
    @DisplayName("Тест тороидальной логики (зацикливание границ)")
    fun testToroidalWrapping() {
        val size = 5
        // Указываем конкретный тип ConwayCell, чтобы не было проблем с bounds
        val grid = Grid(size) { _, _ -> ConwayCell(false) }

        grid.setValue(5, 5, ConwayCell(true))
        assertTrue(grid.getValue(0, 0).isAlive)

        grid.setValue(-1, -1, ConwayCell(true))
        assertTrue(grid.getValue(4, 4).isAlive)
    }

    @Test
    @DisplayName("ConwayCell: Выживание и смерть")
    fun testConwayLogic() {
        val size = 3
        val gridSurvival = Grid(size) { x, y ->
            val isAlive = (x == 1 && y == 1) || (x == 0 && y == 0) || (x == 0 && y == 1)
            ConwayCell(isAlive)
        }
        val currentCell = gridSurvival.getValue(1, 1)
        val nextCell = currentCell.calculateNext(gridSurvival, 1, 1)
        assertTrue(nextCell.isAlive)
    }

    @Test
    @DisplayName("SeedsCell: Рождение только при 2 соседях")
    fun testSeedsLogic() {
        val size = 3
        val grid2 = Grid(size) { x, y ->
            SeedsCell((x == 0 && y == 0) || (x == 0 && y == 1))
        }
        assertTrue(grid2.getValue(1,1).calculateNext(grid2, 1, 1).isAlive)
    }

    @Test
    @DisplayName("DayAndNightCell: Специфическое правило (8 соседей)")
    fun testDayAndNight() {
        val size = 3
        val grid8 = Grid(size) { x, y ->
            DayAndNightCell(!(x == 1 && y == 1))
        }
        val result = grid8.getValue(1, 1).calculateNext(grid8, 1, 1)
        assertTrue(result.isAlive)
    }

    @Test
    @DisplayName("Engine: Проверка смены поколений (Осциллятор 'Линия')")
    fun testEngineStep() = runTest {
        val size = 5

        // КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ:
        // Создаем Grid сразу с конкретным типом ConwayCell.
        // Engine подхватит его через дженерик автоматически.
        val grid = Grid(size) { x, y ->
            ConwayCell(x == 2 && (y == 1 || y == 2 || y == 3))
        }

        // Теперь Engine<ConwayCell> создается без ошибок типов
        val engine = Engine(size, grid)

        // Выполняем шаг
        val nextGrid = engine.step { /* игнорируем аудит */ }

        // Проверяем результат
        assertTrue(nextGrid.getValue(2, 2).isAlive, "Центр (2,2) должен выжить")
        assertTrue(nextGrid.getValue(1, 2).isAlive, "Клетка (1,2) должна ожить")
        assertTrue(nextGrid.getValue(3, 2).isAlive, "Клетка (3,2) должна ожить")

        assertFalse(nextGrid.getValue(2, 1).isAlive, "Клетка (2,1) должна умереть")
        assertFalse(nextGrid.getValue(2, 3).isAlive, "Клетка (2,3) должна умереть")
    }
}