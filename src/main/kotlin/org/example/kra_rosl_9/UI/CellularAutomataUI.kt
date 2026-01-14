package org.example.kra_rosl_9.UI

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.example.kra_rosl_9.cell.ICellLife
import org.example.kra_rosl_9.cell.impl.ConwayCell
import org.example.kra_rosl_9.cell.impl.DayAndNightCell
import org.example.kra_rosl_9.cell.impl.SeedsCell
import org.example.kra_rosl_9.logic.Engine
import org.example.kra_rosl_9.logic.Grid
import java.awt.*
import javax.swing.*
import java.util.Random

@Suppress("UNCHECKED_CAST")
class CellularAutomataUI : JFrame("Клеточные автоматы — Курсовая работа") {
    private var gridSize = 100
    private val maxCanvasSize = 600

    // Основная область для корутин привязана к Главному UI потоку
    private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Инициализируем сразу корректно
    private var currentGrid: Grid<out ICellLife<*>> = Grid(gridSize) { _, _ ->
        ConwayCell(Random().nextDouble() > 0.8)
    }

    private var engine: Engine<*> = Engine(gridSize, currentGrid)

    private val ruleNames = arrayOf("Conway", "Seeds", "DayAndNight")
    private var selectedRuleName = ruleNames[0]
    private var job: Job? = null

    private val canvas = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val gridSnapshot = currentGrid // Локальная копия для отрисовки
            val size = gridSnapshot.size
            if (size <= 0) return

            val drawCellSize = (maxCanvasSize / size).coerceAtLeast(1)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    val cell = gridSnapshot.getValue(x, y)
                    g.color = if (cell.isAlive) Color.BLACK else Color.WHITE
                    g.fillRect(x * drawCellSize, y * drawCellSize, drawCellSize, drawCellSize)
                }
            }
        }
    }

    private val auditArea = JTextArea(20, 30).apply {
        isEditable = false
        background = Color.BLACK
        foreground = Color.GREEN
        font = Font("Monospaced", Font.PLAIN, 12)
    }

    private val initTypes = arrayOf("Случайно", "Планер", "Осциллятор", "Квадрат", "Пустое поле")
    private val initSelector = JComboBox(initTypes)

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()

        canvas.preferredSize = Dimension(maxCanvasSize, maxCanvasSize)
        add(canvas, BorderLayout.CENTER)

        val controls = JPanel(FlowLayout(FlowLayout.LEFT))

        val sizeInput = JTextField(gridSize.toString(), 5)
        val ruleSelector = JComboBox(ruleNames)
        ruleSelector.addActionListener {
            selectedRuleName = ruleSelector.selectedItem as String
        }

        val btnStart = JButton("Старт")
        val btnStop = JButton("Стоп")
        val btnApplySize = JButton("Применить и сброс")

        fun createInitialState(x: Int, y: Int, type: String, size: Int): Boolean {
            return when (type) {
                "Случайно" -> Random().nextDouble() > 0.8
                "Планер" -> listOf(1 to 0, 2 to 1, 0 to 2, 1 to 2, 2 to 2).contains(x to y)
                "Осциллятор" -> {
                    val mid = size / 2
                    x == mid && (y == mid || y == mid - 1 || y == mid + 1)
                }
                "Квадрат" -> {
                    val mid = size / 2
                    x in mid..mid+1 && y in mid..mid+1
                }
                else -> false
            }
        }

        btnApplySize.addActionListener {
            try {
                val selectedInit = initSelector.selectedItem as String
                val newSize = sizeInput.text.toInt()
                if (newSize in 10..500) {
                    job?.cancel()
                    btnStart.isEnabled = true
                    gridSize = newSize

                    val newGrid = when (selectedRuleName) {
                        "Seeds" -> Grid(gridSize) { x, y -> SeedsCell(createInitialState(x, y, selectedInit, gridSize)) }
                        "DayAndNight" -> Grid(gridSize) { x, y -> DayAndNightCell(createInitialState(x, y, selectedInit, gridSize)) }
                        else -> Grid(gridSize) { x, y -> ConwayCell(createInitialState(x, y, selectedInit, gridSize)) }
                    }

                    currentGrid = newGrid as Grid<ICellLife<*>>
                    engine = Engine(gridSize, currentGrid)
                    canvas.repaint()
                    auditArea.setText("Система сброшена. Правило: $selectedRuleName, Размер: $gridSize\n")
                } else {
                    JOptionPane.showMessageDialog(this, "Введите размер от 10 до 500")
                }
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(this, "Некорректный формат числа!")
            }
        }

        btnStart.addActionListener {
            btnStart.isEnabled = false
            // Запускаем симуляцию в UI области
            job = uiScope.launch {
                while (isActive) {
                    // Расчеты в Default (фоновом потоке)
                    val next = withContext(Dispatchers.Default) {
                        engine.step { report ->
                            // Важно: обновление текста ДОЛЖНО быть в Swing потоке
                            launch(Dispatchers.Swing) {
                                auditArea.append(report + "\n")
                                auditArea.caretPosition = auditArea.document.length
                            }
                        }
                    }
                    // Обновляем сетку и рисуем
                    currentGrid = next
                    canvas.repaint()
                    delay(100)
                }
            }
        }

        btnStop.addActionListener {
            job?.cancel()
            btnStart.isEnabled = true
        }

        // Компоновка нижней панели
        controls.add(JLabel("Размер:"))
        controls.add(sizeInput)
        controls.add(JLabel("Правило:"))
        controls.add(ruleSelector)
        controls.add(JLabel("Старт данные:"))
        controls.add(initSelector)
        controls.add(btnApplySize)
        controls.add(btnStart)
        controls.add(btnStop)

        add(controls, BorderLayout.SOUTH)

        val scrollAudit = JScrollPane(auditArea)
        scrollAudit.border = BorderFactory.createTitledBorder("Системный аудит")
        scrollAudit.preferredSize = Dimension(300, maxCanvasSize)
        add(scrollAudit, BorderLayout.EAST)

        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }
}