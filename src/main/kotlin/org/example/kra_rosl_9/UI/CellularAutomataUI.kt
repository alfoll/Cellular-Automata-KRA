import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.example.kra_rosl_9.logic.Engine
import org.example.kra_rosl_9.logic.Grid
import org.example.kra_rosl_9.rules.AutomationRule
import org.example.kra_rosl_9.rules.impl.*
import java.awt.*
import javax.swing.*
import java.util.Random

class CellularAutomataUI : JFrame("Клеточные автоматы — Курсовая работа") {
    // Параметры отображения
    private var gridSize = 100
    private val maxCanvasSize = 600 // Ограничим размер окна, чтобы оно влезало в экран

    // Используем var, так как при изменении размера мы создадим их заново
    private var currentGrid = Grid(gridSize) { _, _ -> if (Random().nextDouble() > 0.8) 1 else 0 }
    private var engine = Engine(gridSize, currentGrid)

    private val rules = listOf(ConwayLifeRule(), SeedsRule(), DayAndNightRule())
    private var selectedRule: AutomationRule<Int> = rules[0]
    private var job: Job? = null // Ссылка на запущенную симуляцию

    private val canvas = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            // Вычисляем размер клетки динамически, чтобы вся сетка влезала в панель
            val drawCellSize = (maxCanvasSize / gridSize).coerceAtLeast(1)

            for (x in 0 until gridSize) {
                for (y in 0 until gridSize) {
                    val value = currentGrid.getValue(x, y)
                    g.color = if (value == 1) Color.BLACK else Color.WHITE
                    g.fillRect(x * drawCellSize, y * drawCellSize, drawCellSize, drawCellSize)
                }
            }
        }
    }

    init {
        title = "Система управления клеточными автоматами"
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()

        canvas.preferredSize = Dimension(maxCanvasSize, maxCanvasSize)
        add(canvas, BorderLayout.CENTER)

        val controls = JPanel()

        // Поле ввода размера
        val sizeInput = JTextField(gridSize.toString(), 5)

        // Выбор правила
        val ruleSelector = JComboBox(rules.map { it.name }.toTypedArray())
        ruleSelector.addActionListener { selectedRule = rules[ruleSelector.selectedIndex] }

        val btnStart = JButton("Старт")
        val btnStop = JButton("Стоп")
        val btnApplySize = JButton("Применить размер и сброс")

        btnApplySize.addActionListener {
            try {
                val newSize = sizeInput.text.toInt()
                if (newSize in 10..500) { // Разумные границы
                    job?.cancel() // Останавливаем старую симуляцию
                    btnStart.isEnabled = true

                    gridSize = newSize
                    currentGrid = Grid(gridSize) { _, _ -> if (Random().nextDouble() > 0.8) 1 else 0 }
                    engine = Engine(gridSize, currentGrid)

                    canvas.repaint()
                } else {
                    JOptionPane.showMessageDialog(this, "Введите размер от 10 до 500")
                }
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(this, "Некорректный формат числа!")
            }
        }

        btnStart.addActionListener {
            btnStart.isEnabled = false
            job = GlobalScope.launch(Dispatchers.Swing) {
                while (isActive) {
                    currentGrid = engine.step(selectedRule, 1)
                    canvas.repaint()
                    delay(30)
                }
            }
        }

        btnStop.addActionListener {
            job?.cancel()
            btnStart.isEnabled = true
        }

        controls.add(JLabel("Размер:"))
        controls.add(sizeInput)
        controls.add(btnApplySize)
        controls.add(JLabel("Правило:"))
        controls.add(ruleSelector)
        controls.add(btnStart)
        controls.add(btnStop)

        add(controls, BorderLayout.SOUTH)

        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }
}



//import kotlinx.coroutines.*
//import kotlinx.coroutines.swing.Swing
//import org.example.kra_rosl_9.logic.Engine
//import org.example.kra_rosl_9.logic.Grid
//import org.example.kra_rosl_9.rules.AutomationRule
//import org.example.kra_rosl_9.rules.impl.*
//import java.awt.*
//import javax.swing.*
//import java.util.Random
//
//class CellularAutomataUI : JFrame("Клеточные автоматы — Курсовая работа") {
//    // Переименовали во избежание конфликта с JFrame.size
//    private val gridSize = 100
//    private val cellSize = 6
//
//    // Текущее состояние и движок
//    private var currentGrid = Grid(gridSize) { _, _ -> if (Random().nextDouble() > 0.8) 1 else 0 }
//    private val engine = Engine(gridSize, currentGrid)
//
//    // Список доступных правил
//    private val rules = listOf(
//        ConwayLifeRule(),
//        SeedsRule(),
//        DayAndNightRule()
//    )
//    private var selectedRule: AutomationRule<Int> = rules[0]
//
//    private val canvas = object : JPanel() {
//        override fun paintComponent(g: Graphics) {
//            super.paintComponent(g)
//            for (x in 0 until gridSize) {
//                for (y in 0 until gridSize) {
//                    val value = currentGrid.getValue(x, y)
//                    g.color = if (value == 1) Color.BLACK else Color.WHITE
//                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize)
//                }
//            }
//        }
//    }
//
//    init {
//        title = "Система клеточных автоматов"
//        defaultCloseOperation = EXIT_ON_CLOSE
//        layout = BorderLayout()
//
//        canvas.preferredSize = Dimension(gridSize * cellSize, gridSize * cellSize)
//        add(canvas, BorderLayout.CENTER)
//
//        // Панель управления
//        val controls = JPanel()
//
//        // 1. Выбор правила
//        val ruleSelector = JComboBox(rules.map { it.name }.toTypedArray())
//        ruleSelector.addActionListener {
//            selectedRule = rules[ruleSelector.selectedIndex]
//        }
//
//        // 2. Кнопка запуска
//        val btnStart = JButton("Запустить")
//
//        // 3. Кнопка сброса (рандом)
//        val btnReset = JButton("Сброс")
//        btnReset.addActionListener {
//            currentGrid = Grid(gridSize) { _, _ -> if (Random().nextDouble() > 0.8) 1 else 0 }
//            canvas.repaint()
//        }
//
//        controls.add(JLabel("Правило:"))
//        controls.add(ruleSelector)
//        controls.add(btnStart)
//        controls.add(btnReset)
//        add(controls, BorderLayout.SOUTH)
//
//        pack()
//        setLocationRelativeTo(null)
//        isVisible = true
//
//        // Логика работы симуляции
//        btnStart.addActionListener {
//            btnStart.isEnabled = false
//            // GlobalScope использовать не очень хорошо, но для учебного проекта — допустимо
//            GlobalScope.launch(Dispatchers.Swing) {
//                while (isActive) {
//                    // Вызываем наш движок (aliveValue для Int это 1)
//                    currentGrid = engine.step(selectedRule, 1)
//                    canvas.repaint()
//                    delay(40) // Скорость симуляции
//                }
//            }
//        }
//    }
//}
//
