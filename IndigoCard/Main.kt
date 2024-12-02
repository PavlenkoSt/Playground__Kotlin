package indigo

val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
val suits = listOf("♦", "♥", "♠", "♣")

enum class CurrentTurn {
    Player,
    Computer
}

fun main() {
    IndigoGame().start()
}

class IndigoGame {
    private val deck = Deck()
    private var tableCards = mutableListOf<String>()
    private val player = Player("Player")
    private val computer = Player("Computer")

    private var currentTurn: CurrentTurn = CurrentTurn.Player
    private var lastWinner: CurrentTurn? = null
    private var isExited = false

    fun start() {
        println("Indigo Card Game")
        currentTurn = if (askUserToPlayFirst()) CurrentTurn.Player else CurrentTurn.Computer

        tableCards = deck.drawCards(4).toMutableList()
        println("Initial cards on the table: ${tableCards.joinToString(" ")}")

        player.drawCards(deck, 6)
        computer.drawCards(deck, 6)

        playGame()

        if (!isExited) finalizeGame()
    }

    private fun askUserToPlayFirst(): Boolean {
        println("Play first?")
        var answer = readln().lowercase()
        while (answer != "yes" && answer != "no") {
            println("Play first?")
            answer = readln()
        }
        return answer == "yes"
    }

    private fun playGame() {
        while (!gameOver()) {
            println()
            println(
                if (tableCards.isEmpty()) "No cards on the table"
                else "${tableCards.size} cards on the table, and the top card is ${tableCards.last()}"
            )

            when (currentTurn) {
                CurrentTurn.Player -> playerTurn()
                CurrentTurn.Computer -> computerTurn()
            }

            refillHands()
        }
    }

    private fun playerTurn() {
        println("Cards in hand: ${player.handDescription()}")
        val input = getValidatedInput((1..player.hand.size).map { it.toString() } + "exit",
            "Choose a card to play (1-${player.hand.size}):")

        if (input == "exit") {
            isExited = true
            println("Game Over")
            return
        }

        val cardIndex = input.toInt() - 1
        playCard(player, player.hand[cardIndex])
        currentTurn = CurrentTurn.Computer
    }

    private fun computerTurn() {
        println(computer.hand.joinToString(" "))

        val topCard = tableCards.lastOrNull()
        val candidateCards = computer.hand.filter { card ->
            topCard == null || card.first() == topCard[0] || card.last() == topCard.last()
        }

        val cardToPlay = when {
            computer.hand.size == 1 -> computer.hand.first()
            candidateCards.size == 1 -> candidateCards.first()
            topCard == null -> {
                findRandomCardToPlayWithSameSuitOrRank() ?: computer.hand.random()
            }

            candidateCards.isEmpty() -> {
                findRandomCardToPlayWithSameSuitOrRank() ?: computer.hand.random()
            }

            else -> {
                findCandidateCardByStrategy(candidateCards, topCard) ?: candidateCards.random()
            }
        }

        println("Computer plays $cardToPlay")
        playCard(computer, cardToPlay)
        currentTurn = CurrentTurn.Player
    }

    private fun findRandomCardToPlayWithSameSuitOrRank(): String? {
        val suits = computer.hand.groupBy { it.last() }
        val ranks = computer.hand.groupBy { it.first() }
        val multipleSuitCards = suits.values.find { it.size > 1 }
        val multipleRankCards = ranks.values.find { it.size > 1 }

        return multipleSuitCards?.random() ?: multipleRankCards?.random()
    }

    private fun findCandidateCardByStrategy(candidateCards: List<String>, topCard: String): String? {
        val sameSuitCandidates = candidateCards.filter { it.last() == topCard.last() }
        val sameRankCandidates = candidateCards.filter { it.first() == topCard.first() }

        return when {
            sameSuitCandidates.size > 1 -> sameSuitCandidates.random()
            sameRankCandidates.size > 1 -> sameRankCandidates.random()
            else -> null
        }
    }

    private fun playCard(player: Player, card: String) {
        val isWin = checkIfWinsCards(card)
        player.playCard(card, tableCards)
        if (isWin) {
            println("${player.name} wins cards")
            player.collectCards(tableCards)
            tableCards.clear()

            printScore()

            lastWinner = if (player.name == "Player") CurrentTurn.Player else CurrentTurn.Computer
        }
    }

    private fun printScore() {
        println("Score: Player ${player.points} - Computer ${computer.points}")
        println("Cards: Player ${player.wonCards.size} - Computer ${computer.wonCards.size}")
    }

    private fun refillHands() {
        if (player.hand.isEmpty()) player.drawCards(deck, 6)
        if (computer.hand.isEmpty()) computer.drawCards(deck, 6)
    }

    private fun checkIfWinsCards(card: String): Boolean {
        return tableCards.size >= 1 && (card.first() == tableCards.last()[0] || card.last() == tableCards.last().last())
    }

    private fun gameOver(): Boolean {
        return (deck.isEmpty() && player.hand.isEmpty() && computer.hand.isEmpty()) || isExited
    }

    private fun finalizeGame() {
        lastWinner?.let {
            val winner = if (it == CurrentTurn.Player) player else computer
            winner.collectCards(tableCards)
        }
        assignFinalBonus()

        println(
            if (tableCards.isEmpty()) "No cards on the table"
            else "${tableCards.size} cards on the table, and the top card is ${tableCards.last()}"
        )

        printScore()
        println("Game Over")
    }

    private fun assignFinalBonus() {
        val playerWonMore = player.wonCards.size > computer.wonCards.size
        val computerWonMore = computer.wonCards.size > player.wonCards.size

        if (playerWonMore) player.addPoints(3)
        else if (computerWonMore) computer.addPoints(3)
        else if (currentTurn == CurrentTurn.Player) player.addPoints(3) else computer.addPoints(3)
    }

    private fun getValidatedInput(validOptions: List<String>, message: String): String {
        var input: String
        do {
            println(message)
            input = readln().lowercase()
        } while (input !in validOptions)
        return input
    }
}

class Deck {
    private val cards = (ranks.flatMap { rank -> suits.map { suit -> "$rank$suit" } }).shuffled().toMutableList()

    fun drawCards(count: Int): List<String> {
        val drawnCards = cards.takeLast(count)
        cards.removeAll(drawnCards)
        return drawnCards
    }

    fun isEmpty() = cards.isEmpty()
}

class Player(val name: String) {
    var hand = mutableListOf<String>()
    var wonCards = mutableListOf<String>()
    var points = 0

    fun drawCards(deck: Deck, count: Int) {
        hand.addAll(deck.drawCards(count))
    }

    fun playCard(card: String, tableCards: MutableList<String>) {
        hand.remove(card)
        tableCards.add(card)
    }

    fun collectCards(cards: List<String>) {
        wonCards.addAll(cards)
        points += calculatePoints(cards)
    }

    fun handDescription(): String {
        return hand.mapIndexed { idx, card -> "${idx + 1})$card" }.joinToString(" ")
    }

    fun addPoints(extraPoints: Int) {
        points += extraPoints
    }
}

fun calculatePoints(cards: List<String>): Int {
    return cards.count {
        it.startsWith("A") || it.startsWith("10") || it.startsWith("J") || it.startsWith("Q") || it.startsWith(
            "K"
        )
    }
}
