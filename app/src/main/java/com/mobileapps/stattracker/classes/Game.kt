package com.mobileapps.stattracker.classes

import com.google.firebase.firestore.DocumentId

data class Game(
    @DocumentId val id: String = "",
    val groupId: String = "",
    val date: Long = System.currentTimeMillis(),
    val settings: GameSettings = GameSettings(),
    val team1: List<String> = emptyList(), // List of names
    val team2: List<String> = emptyList(), // List of names
    val score1: Int = 0,
    val score2: Int = 0,
    val playerStats: Map<String, PlayerGameStats> = emptyMap(), // name -> stats
    val status: String = "PENDING", // PENDING, ACTIVE, FINISHED
    val isPaused: Boolean = false,
    val durationSeconds: Int = 0
)

data class PlayerGameStats(
    val points: Int = 0,
    val rebounds: Int = 0,
    val blocks: Int = 0,
    val steals: Int = 0
)

data class GameSettings(
    val winCondition: WinCondition = WinCondition.FIRST_TO_21,
    val scoringType: ScoringType = ScoringType.ONES_AND_TWOS
)

enum class WinCondition {
    FIRST_TO_21, TIMER
}

enum class ScoringType {
    ONES_AND_TWOS, TWOS_AND_THREES
}
