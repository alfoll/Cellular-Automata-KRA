package org.example.kra_rosl_9.UI

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

    private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
    private val auditArea = JTextArea(20, 50).apply {
        isEditable = false
        background = Color.BLACK
        foreground = Color.GREEN // Сделаем "хакерский" вид для красоты
        font = Font("Monospaced", Font.PLAIN, 12)
    }

    private val initTypes = arrayOf("Случайно", "Планер", "Осциллятор", "Квадрат", "Пустое поле")
    private val initSelector = JComboBox(initTypes)

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
                val selectedInit = initSelector.selectedItem as String
                val newSize = sizeInput.text.toInt()
                if (newSize in 10..500) { // Разумные границы
                    job?.cancel() // Останавливаем старую симуляцию
                    btnStart.isEnabled = true

                    gridSize = newSize
                    currentGrid = Grid(gridSize) { x, y ->

                        when (selectedInit) {
                            "Случайно" -> if (Random().nextDouble() > 0.8) 1 else 0 // Рандом
                            "Планер" -> {
                                // Рисуем планер в углу (координаты 1,0; 2,1; 0,2; 1,2; 2,2)
                                val gliderCoords = listOf(1 to 0, 2 to 1, 0 to 2, 1 to 2, 2 to 2)
                                if (gliderCoords.contains(x to y)) 1 else 0
                            }
                            "Осциллятор" -> {
                                // Линия из 3 клеток в центре
                                val mid = gridSize / 2
                                if (x == mid && (y == mid || y == mid - 1 || y == mid + 1)) 1 else 0
                            }
                            "Квадрат" -> { // Рисуем "Квадрат" в центре
                                val mid = gridSize / 2
                                val glider = listOf(mid to mid, mid to mid+1, mid+1 to mid, mid+1 to mid+1)
                                if (glider.contains(x to y)) 1 else 0
                            }
                            else -> 0 // Пусто
                        }
                    }
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

            job = uiScope.launch(Dispatchers.Swing) {
                while (isActive) {
                    // лямбда-выражение, которое пишет текст в auditArea
                    val next = withContext(Dispatchers.Default) {
                         engine.step(selectedRule, 1) { report ->
                            auditArea.append(report + "\n")
                            // авто-прокрутка вниз
                            auditArea.caretPosition = auditArea.document.length
                        }
                    }
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

        addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent?) {
                uiScope.cancel() // Останавливает все запущенные вычисления
                super.windowClosing(e)
            }
        })

        controls.add(JLabel("Размер:"))
        controls.add(sizeInput)
        controls.add(btnApplySize)
        controls.add(JLabel("Правило:"))
        controls.add(ruleSelector)
        controls.add(btnStart)
        controls.add(btnStop)
        controls.add(JLabel("Начальные данные:"))
        controls.add(initSelector)

        add(controls, BorderLayout.SOUTH)

        pack()
        setLocationRelativeTo(null)
        isVisible = true

        val scrollAudit = JScrollPane(auditArea)
        scrollAudit.border = BorderFactory.createTitledBorder("Системный аудит")
        add(scrollAudit, BorderLayout.EAST) // панель сбоку

        pack()
    }
}

