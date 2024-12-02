package minesweeper

import kotlin.system.exitProcess

const val USER_INPUT_MESSAGE = "Set/unset mine marks or claim a cell as free:"
const val WIN_MESSAGE = "Congratulations! You found all the mines!"
const val GREET_MESSAGE = "How many mines do you want on the field?"
const val FAIL_MESSAGE = "You stepped on a mine and failed!"

enum class Mark(val mark: String) {
    Marked(mark = "*"),
    Unexplored(mark = "."),
    Explored(mark = "/"),
}

enum class Command(val command: String) {
    Free(command = "free"),
    Mine(command = "mine")
}

data class Cell(
    var value: String,
    var isMarked: Boolean,
    var isExplored: Boolean,
    var isMined: Boolean
)

data class UserInput(
    val y: Int,
    val x: Int,
    val cell: Cell,
    val command: String
)

fun main() {
    println(GREET_MESSAGE)
    val minesCount = readln().toInt()

    val minefield = Minefield(9, 9, minesCount)

    Game(minefield).start()
}

class Minefield(
    private val xSize: Int,
    private val ySize: Int,
    private val countOfMines: Int
) {
    var minefield: MutableList<MutableList<Cell>>

    init {
        minefield = calculateMinesAround(
            generateMinefield(
                generateMinePositions()
            )
        )
    }

    fun getCell(y: Int, x: Int): Cell {
        return minefield[y][x]
    }

    fun markCell(y: Int, x: Int) {
        val cell = minefield[y][x]
        cell.isMarked = !cell.isMarked
    }

    fun revealCell(y: Int, x: Int, isFirst: Boolean): Boolean {
        val cell = getCell(y, x)

        if (cell.isMined) {
            if (isFirst) {
                moveMineToOtherCeil(y, x)
            } else {
                return false
            }
        }

        revealAllCellsAround(y, x)
        return true
    }

    private fun moveMineToOtherCeil(y: Int, x: Int) {
        getCell(y, x).isMined = false

        var isDone = false

        while (!isDone) {
            val yPosCandidate = (Math.random() * ySize).toInt()
            val xPosCandidate = (Math.random() * xSize).toInt()

            val candidate = getCell(yPosCandidate, xPosCandidate)

            if (!candidate.isMined) {
                candidate.isMined = true
                isDone = true
            }
        }
    }

    private fun revealAllCellsAround(y: Int, x: Int) {
        val positionsToExplore = mutableListOf<String>(
            CellPosition.encode(y, x)
        )
        val visitedPositions = mutableListOf<String>()

        while (positionsToExplore.isNotEmpty()) {
            val pos = positionsToExplore.removeLast()

            if (visitedPositions.contains(pos)) continue
            visitedPositions.add(pos)

            val (yPos, xPos) = CellPosition.decode(pos)
            getCell(yPos, xPos).isExplored = true
            val cellsAround = getValidPositionsAround(yPos, xPos)

            val canExplore = cellsAround.all { cellPos ->
                val (yCell, xCell) = CellPosition.decode(cellPos)
                !getCell(yCell, xCell).isMined
            }

            if (canExplore) {
                positionsToExplore.addAll(
                    cellsAround.filter { !positionsToExplore.contains(it) && !visitedPositions.contains(it) }
                )
            }
        }
    }

    private fun generateMinePositions(): List<String> {
        val positions = mutableListOf<String>()

        while (positions.size < countOfMines) {
            val yPosCandidate = (Math.random() * ySize).toInt()
            val xPosCandidate = (Math.random() * xSize).toInt()

            val posCandidate = CellPosition.encode(yPosCandidate, xPosCandidate)

            if (!positions.contains(posCandidate)) {
                positions.add(posCandidate)
            }
        }

        return positions
    }

    private fun generateMinefield(minePositions: List<String>): MutableList<MutableList<Cell>> {
        val field = mutableListOf<MutableList<Cell>>()

        repeat(ySize) { y ->
            field.add(mutableListOf())

            repeat(xSize) { x ->
                val isMined = minePositions.contains(
                    CellPosition.encode(y, x)
                )

                field[y].add(
                    Cell(
                        value = Mark.Explored.mark,
                        isMined = isMined,
                        isMarked = false,
                        isExplored = false
                    )
                )
            }
        }

        return field
    }

    private fun calculateMinesAround(minefield: MutableList<MutableList<Cell>>): MutableList<MutableList<Cell>> {
        minefield.forEachIndexed { yIdx, y ->
            y.forEachIndexed { xIdx, cell ->
                if (cell.isMined) {
                    getValidPositionsAround(yIdx, xIdx).forEach { pos ->
                        val (yPos, xPos) = CellPosition.decode(pos)
                        val targetCell = minefield[yPos][xPos]

                        if (!targetCell.isMined) {
                            if (targetCell.value.toIntOrNull() == null) {
                                targetCell.value = "1"
                            } else {
                                targetCell.value = (targetCell.value.toInt() + 1).toString()
                            }
                        }
                    }
                }
            }
        }

        return minefield
    }

    private fun getValidPositionsAround(y: Int, x: Int): List<String> {
        val possiblePositionsToCheck = mutableListOf<Pair<Int, Int>>(
            Pair(y - 1, x - 1),
            Pair(y - 1, x),
            Pair(y - 1, x + 1),

            Pair(y, x - 1),
            Pair(y, x + 1),

            Pair(y + 1, x - 1),
            Pair(y + 1, x),
            Pair(y + 1, x + 1),
        )

        return possiblePositionsToCheck.filter {
            val (yInt, xInt) = it
            yInt in 0..<ySize && xInt in 0..<xSize
        }.map { CellPosition.encode(it.first, it.second) }
    }
}

class Game(private val minefield: Minefield) {
    fun start() {
        var isFirstFreeCommand = true
        Game@ while (true) {
            println()
            printMinefield()
            println(USER_INPUT_MESSAGE)

            var userInput = getCellByUserInputCoordinate()

            while (userInput.cell.value.toIntOrNull() != null) {
                println("There is a number here!")
                println(USER_INPUT_MESSAGE)

                userInput = getCellByUserInputCoordinate()
            }

            when (userInput.command) {
                Command.Mine.command -> {
                    minefield.markCell(userInput.y, userInput.x)
                }

                Command.Free.command -> {
                    val revealed = minefield.revealCell(userInput.y, userInput.x, isFirstFreeCommand)
                    isFirstFreeCommand = false

                    if (!revealed) {
                        printMinefield()
                        println(FAIL_MESSAGE)
                        break@Game
                    }
                }
            }


            if (checkIfWin()) {
                println(WIN_MESSAGE)
                break
            }
        }

        exitProcess(0)
    }

    private fun checkIfWin(): Boolean {
        val flatMinefield = minefield.minefield.flatten()
        val markedCells = flatMinefield.filter { it.isMarked }
        val minedCells = flatMinefield.filter { it.isMined }
        val safeCells = flatMinefield.filter { !it.isMined }

        val allSafeCellsExplored = safeCells.all { it.isExplored }
        val allMinesMarked = markedCells.containsAll(minedCells) && minedCells.containsAll(markedCells)

        return allMinesMarked || allSafeCellsExplored
    }

    private fun printMinefield() {
        var str = " │123456789│\n" + "—│—————————│\n"

        minefield.minefield.forEachIndexed { yIdx, y ->
            if (yIdx != 0) str += "\n"
            str += "${yIdx + 1}│"
            y.forEachIndexed { xIdx, cell ->
                val symbolToAdd = if (cell.isMarked) {
                    Mark.Marked.mark
                } else if (cell.isExplored) {
                    cell.value
                } else {
                    Mark.Unexplored.mark
                }

                str += symbolToAdd
            }
            str += "│"
        }

        str += "\n—│—————————│"

        println(str)
    }

    private fun getCellByUserInputCoordinate(): UserInput {
        val (first, second, third) = readln().split(" ")
        val x = first.toInt() - 1
        val y = second.toInt() - 1

        return UserInput(
            y = y,
            x = x,
            cell = minefield.getCell(y, x),
            command = third
        )
    }
}

object CellPosition {
    fun decode(str: String): Pair<Int, Int> {
        val (y, x) = str.split("-")
        return Pair(y.toInt(), x.toInt())
    }

    fun encode(y: Int, x: Int): String {
        return "$y-$x"
    }
}
