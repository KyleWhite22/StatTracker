package com.mobileapps.stattracker.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mobileapps.stattracker.classes.Game
import com.mobileapps.stattracker.classes.GameSettings
import com.mobileapps.stattracker.classes.PlayerGameStats
import com.mobileapps.stattracker.classes.PlayerTotals
import com.mobileapps.stattracker.classes.WinCondition

class GameViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var currentGame by mutableStateOf<Game?>(null)
        private set

    var finishedGames by mutableStateOf<List<Game>>(emptyList())
        private set

    var activeGames by mutableStateOf<List<Game>>(emptyList())
        private set

    var leaderboard by mutableStateOf<Map<String, PlayerTotals>>(emptyMap())
        private set

    fun startGame(groupId: String, settings: GameSettings, team1: List<String>, team2: List<String>, onGameStarted: (String) -> Unit) {
        val docRef = db.collection("games").document()
        val allPlayers = team1 + team2
        val initialStats = allPlayers.associateWith { PlayerGameStats() }
        
        val game = Game(
            id = docRef.id,
            groupId = groupId,
            settings = settings,
            team1 = team1,
            team2 = team2,
            playerStats = initialStats,
            status = "ACTIVE",
            durationSeconds = settings.timerDurationMinutes * 60,
            startTime = if (settings.winCondition == WinCondition.TIMER) System.currentTimeMillis() else null
        )
        
        docRef.set(game).addOnSuccessListener {
            currentGame = game
            onGameStarted(docRef.id)
        }
    }

    fun loadGame(gameId: String) {
        db.collection("games").document(gameId).addSnapshotListener { snapshot, _ ->
            currentGame = snapshot?.toObject(Game::class.java)
        }
    }

    fun loadActiveGames(groupId: String) {
        db.collection("games")
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("status", "ACTIVE")
            .get()
            .addOnSuccessListener { result ->
                activeGames = result.documents.mapNotNull { it.toObject(Game::class.java) }
            }
    }

    fun loadLeaderboard(groupId: String) {
        db.collection("games")
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("status", "FINISHED")
            .get()
            .addOnSuccessListener { result ->
                val games = result.documents.mapNotNull { it.toObject(Game::class.java) }
                val totals = mutableMapOf<String, PlayerTotals>()
                
                games.forEach { game ->
                    val winningTeam = if (game.score1 > game.score2) game.team1 else if (game.score2 > game.score1) game.team2 else null
                    val allPlayers = game.team1 + game.team2
                    
                    allPlayers.forEach { playerName ->
                        val stats = game.playerStats[playerName] ?: PlayerGameStats()
                        val current = totals[playerName] ?: PlayerTotals()
                        
                        val isWin = winningTeam?.contains(playerName) == true
                        val isLoss = winningTeam != null && !isWin
                        
                        totals[playerName] = current.copy(
                            wins = current.wins + (if (isWin) 1 else 0),
                            losses = current.losses + (if (isLoss) 1 else 0),
                            points = current.points + stats.points,
                            rebounds = current.rebounds + stats.rebounds,
                            blocks = current.blocks + stats.blocks,
                            steals = current.steals + stats.steals
                        )
                    }
                }
                leaderboard = totals
            }
    }

    fun loadFinishedGames(groupId: String? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        if (groupId != null && groupId != "all") {
            db.collection("games")
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("status", "FINISHED")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    finishedGames = result.documents.mapNotNull { it.toObject(Game::class.java) }
                }
        } else {
            db.collection("groups").whereEqualTo("ownerID", userId).get().addOnSuccessListener { groupDocs ->
                val groupIds = groupDocs.documents.map { it.id }
                if (groupIds.isNotEmpty()) {
                    db.collection("games")
                        .whereIn("groupId", groupIds)
                        .whereEqualTo("status", "FINISHED")
                        .orderBy("date", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { result ->
                            finishedGames = result.documents.mapNotNull { it.toObject(Game::class.java) }
                        }
                } else {
                    finishedGames = emptyList()
                }
            }
        }
    }

    fun logStat(playerName: String, statType: String, points: Int = 0, onGameEnded: (String) -> Unit = {}) {
        val game = currentGame ?: return
        val currentStats = game.playerStats[playerName] ?: PlayerGameStats()
        
        val newStats = when (statType) {
            "Points" -> currentStats.copy(points = currentStats.points + points)
            "Rebounds" -> currentStats.copy(rebounds = currentStats.rebounds + 1)
            "Blocks" -> currentStats.copy(blocks = currentStats.blocks + 1)
            "Steals" -> currentStats.copy(steals = currentStats.steals + 1)
            else -> currentStats
        }

        val isTeam1 = game.team1.contains(playerName)
        var newScore1 = game.score1
        var newScore2 = game.score2

        if (statType == "Points") {
            if (isTeam1) newScore1 += points
            else newScore2 += points
        }

        val updatedPlayerStats = game.playerStats.toMutableMap()
        updatedPlayerStats[playerName] = newStats

        val updates = mutableMapOf<String, Any>(
            "playerStats" to updatedPlayerStats,
            "score1" to newScore1,
            "score2" to newScore2
        )

        // Check Win Condition: First to 21
        if (game.settings.winCondition == WinCondition.FIRST_TO_21 && (newScore1 >= 21 || newScore2 >= 21)) {
            updates["status"] = "FINISHED"
            db.collection("games").document(game.id).update(updates).addOnSuccessListener {
                onGameEnded(game.id)
            }
        } else {
            db.collection("games").document(game.id).update(updates)
        }
    }

    fun updateTimer(remainingSeconds: Int) {
        val game = currentGame ?: return
        db.collection("games").document(game.id).update("durationSeconds", remainingSeconds)
    }

    fun togglePause() {
        val game = currentGame ?: return
        val isNowPaused = !game.paused

        val updates = mutableMapOf<String, Any>("paused" to isNowPaused)
        
        if (game.settings.winCondition == WinCondition.TIMER) {
            val now = System.currentTimeMillis()
            if (isNowPaused) { // We are pausing it now
                val elapsedMs = if (game.startTime != null) now - game.startTime else 0L
                val elapsedSeconds = (elapsedMs / 1000).toInt()
                val newRemaining = (game.durationSeconds - elapsedSeconds).coerceAtLeast(0)
                updates["durationSeconds"] = newRemaining
                updates["startTime"] = FieldValue.delete() // Clear start time while paused
            } else { // We are resuming it now
                updates["startTime"] = now
            }
        }
        
        db.collection("games").document(game.id).update(updates)
            .addOnSuccessListener {
                Log.d("GameViewModel", "Successfully toggled pause to: $isNowPaused")
            }
            .addOnFailureListener {
                Log.e("GameViewModel", "Failed to toggle pause: ${it.message}")
            }
    }

    fun endGame(onComplete: (String) -> Unit) {
        val game = currentGame ?: return
        db.collection("games").document(game.id).update("status", "FINISHED")
            .addOnSuccessListener { 
                onComplete(game.id) 
            }
    }
}
