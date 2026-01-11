package org.example.kra_rosl_9

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.example.kra_rosl_9.logic.Engine
import org.example.kra_rosl_9.logic.Grid
import org.example.kra_rosl_9.rules.impl.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@OptIn(ExperimentalCoroutinesApi::class)
class CellularAutomataTest {

    // --- ТЕСТЫ СЕТКИ (Grid) ---

    @Test
    @DisplayName("Тест тороидальной логики (зацикливание границ)")
    fun testToroidalWrapping() {
        val size = 5
        val grid = Grid(size) { _, _ -> 0 }

        // Ставим значение в "виртуальные" координаты
        // (5, 5) при размере 5 должно попасть в (0, 0)
        grid.setValue(5, 5, 1)
        assertEquals(1, grid.getValue(0, 0), "Координата (5,5) должна превратиться в (0,0)")

        // (-1, -1) должно превратиться в (4, 4)
        grid.setValue(-1, -1, 1)
        assertEquals(1, grid.getValue(4, 4), "Координата (-1,-1) должна превратиться в (4,4)")
    }

    @Test
    @DisplayName("Тест инициализации сетки")
    fun testGridInitialization() {
        val grid = Grid(10) { x, y -> x + y }
        assertEquals(0, grid.getValue(0, 0))
        assertEquals(2, grid.getValue(1, 1))
        assertEquals(18, grid.getValue(9, 9))
    }

    // --- ТЕСТЫ ПРАВИЛ (AutomationRules) ---

    @Test
    @DisplayName("ConwayLife: Выживание клетки (2-3 соседа)")
    fun testConwaySurvival() {
        val rule = ConwayLifeRule()
        val grid = Grid(3) { _, _ -> 0 }

        // Сценарий: центральная живая, 2 соседа живых
        grid.setValue(1, 1, 1) // Центр
        grid.setValue(0, 0, 1) // Сосед 1
        grid.setValue(0, 1, 1) // Сосед 2

        val nextState = rule.calculateNewState(grid, 1, 1)
        assertEquals(1, nextState, "Живая клетка с 2 соседями должна выжить")
    }

    @Test
    @DisplayName("ConwayLife: Рождение клетки (ровно 3 соседа)")
    fun testConwayBirth() {
        val rule = ConwayLifeRule()
        val grid = Grid(3) { _, _ -> 0 }

        // Сценарий: центр мертв, 3 соседа живых
        grid.setValue(0, 0, 1)
        grid.setValue(0, 1, 1)
        grid.setValue(0, 2, 1)

        val nextState = rule.calculateNewState(grid, 1, 1)
        assertEquals(1, nextState, "Мертвая клетка с 3 соседями должна ожить")
    }

    @Test
    @DisplayName("Seeds: Рождение только при 2 соседях")
    fun testSeedsRule() {
        val rule = SeedsRule()
        val grid = Grid(3) { _, _ -> 0 }

        grid.setValue(0, 0, 1)
        grid.setValue(0, 1, 1)

        val resultWithTwo = rule.calculateNewState(grid, 1, 1)
        assertEquals(1, resultWithTwo, "В Seeds рождение происходит при 2 соседях")

        grid.setValue(0, 2, 1) // Добавили третьего
        val resultWithThree = rule.calculateNewState(grid, 1, 1)
        assertEquals(0, resultWithThree, "В Seeds клетка не рождается при 3 соседях")
    }

    @Test
    @DisplayName("Average: Математическое усреднение по кресту")
    fun testAverageRule() {
        val rule = AverageRule()
        val grid = Grid(3) { _, _ -> 0.0 }

        // Соседи по кресту (вверх, вниз, влево, вправо)
        grid.setValue(1, 0, 1.0)
        grid.setValue(1, 2, 1.0)
        grid.setValue(0, 1, 1.0)
        grid.setValue(2, 1, 1.0)

        val result = rule.calculateNewState(grid, 1, 1)
        assertEquals(1.0, result, 0.001, "Среднее (1+1+1+1)/4 должно быть 1.0")
    }

    // --- ТЕСТЫ ДВИЖКА (Engine) ---

    @Test
    @DisplayName("Engine: Проверка смены поколений (параллельный расчет)")
    fun testEngineStep() = runTest {
        // Увеличиваем размер до 5x5, чтобы края не мешали логике фигуры
        val size = 5
        // Создаем вертикальную линию в центре: (2, 1), (2, 2), (2, 3)
        val initialGrid = Grid(size) { x, y ->
            if (x == 2 && (y == 1 || y == 2 || y == 3)) 1 else 0
        }
        val engine = Engine(size, initialGrid)
        val rule = ConwayLifeRule()

        // Выполняем один шаг
        val resultGrid = engine.step(rule, 1) { /* игнорируем аудит */ }

        // Ожидаем, что линия станет горизонтальной вокруг центра (2, 2)
        // Новые координаты: (1, 2), (2, 2), (3, 2)

        // 1. Центр должен выжить
        assertEquals(1, resultGrid.getValue(2, 2), "Центр (2,2) должен остаться живым")

        // 2. Боковые клетки должны ожить
        assertEquals(1, resultGrid.getValue(1, 2), "Клетка (1,2) должна ожить")
        assertEquals(1, resultGrid.getValue(3, 2), "Клетка (3,2) должна ожить")

        // 3. Концы вертикальной линии должны умереть
        assertEquals(0, resultGrid.getValue(2, 1), "Верхняя клетка (2,1) должна умереть")
        assertEquals(0, resultGrid.getValue(2, 3), "Нижняя клетка (2,3) должна умереть")
    }
}