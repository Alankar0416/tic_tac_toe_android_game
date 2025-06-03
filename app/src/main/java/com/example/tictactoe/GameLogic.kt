package com.example.tictactoe

class GameLogic {
    companion object {
        const val EMPTY = 0
        const val PLAYER_X = 1 // Human
        const val PLAYER_O = 2 // Computer
    }

    val board = Array(3) { IntArray(3) { EMPTY } }
    private var currentPlayer = PLAYER_X
    private var winner = EMPTY
    private var moveCount = 0
    private var winLine: WinLine? = null

    fun makeMove(x: Int, y: Int): Boolean {
        if (x !in 0..2 || y !in 0..2) return false
        if (board[x][y] != EMPTY || isGameOver()) return false

        board[x][y] = currentPlayer
        moveCount++
        if (checkWin(x, y)) {
            winner = currentPlayer
        } else if (moveCount == 9) {
            winner = -1 // Draw
        } else {
            currentPlayer = if (currentPlayer == PLAYER_X) PLAYER_O else PLAYER_X
        }
        return true
    }

    fun isGameOver(): Boolean = winner != EMPTY || winner == -1

    fun getWinner(): Int = winner

    fun reset() {
        for (i in 0..2) for (j in 0..2) board[i][j] = EMPTY
        currentPlayer = PLAYER_X
        winner = EMPTY
        moveCount = 0
        winLine = null
    }

    fun getComputerMove(): Pair<Int, Int>? {
        // Simple AI: win/block, else random
        for (i in 0..2) for (j in 0..2) {
            if (board[i][j] == EMPTY) {
                // Try win
                board[i][j] = PLAYER_O
                if (isWin(PLAYER_O)) {
                    board[i][j] = EMPTY
                    return Pair(i, j)
                }
                board[i][j] = EMPTY
            }
        }
        for (i in 0..2) for (j in 0..2) {
            if (board[i][j] == EMPTY) {
                // Try block
                board[i][j] = PLAYER_X
                if (isWin(PLAYER_X)) {
                    board[i][j] = EMPTY
                    return Pair(i, j)
                }
                board[i][j] = EMPTY
            }
        }
        val empty = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) for (j in 0..2) if (board[i][j] == EMPTY) empty.add(Pair(i, j))
        return empty.randomOrNull()
    }

    private fun isWin(player: Int): Boolean {
        // Check all win conditions
        for (i in 0..2)
            if ((0..2).all { board[i][it] == player }) return true
        for (j in 0..2)
            if ((0..2).all { board[it][j] == player }) return true
        if ((0..2).all { board[it][it] == player }) return true
        if ((0..2).all { board[it][2 - it] == player }) return true
        return false
    }

    // WinLine: Pair of start (x, y) and end (x, y) in grid coordinates
    data class WinLine(val start: Pair<Int, Int>, val end: Pair<Int, Int>)

    fun getWinLine(): WinLine? = winLine

    private fun checkWin(x: Int, y: Int): Boolean {
        val player = board[x][y]
        // Row
        if ((0..2).all { board[it][y] == player }) {
            winLine = WinLine(Pair(0, y), Pair(2, y))
            return true
        }
        // Column
        if ((0..2).all { board[x][it] == player }) {
            winLine = WinLine(Pair(x, 0), Pair(x, 2))
            return true
        }
        // Diagonal
        if (x == y && (0..2).all { board[it][it] == player }) {
            winLine = WinLine(Pair(0, 0), Pair(2, 2))
            return true
        }
        // Anti-diagonal
        if (x + y == 2 && (0..2).all { board[it][2 - it] == player }) {
            winLine = WinLine(Pair(0, 2), Pair(2, 0))
            return true
        }
        return false
    }
}