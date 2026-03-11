package com.mobileapps.stattracker.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mobileapps.stattracker.classes.Game
import com.mobileapps.stattracker.classes.GameSettings
import com.mobileapps.stattracker.classes.PlayerGameStats
import com.mobileapps.stattracker.classes.ScoringType

class GameViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var currentGame by mutableStateOf<Game?>(null)
        private set

    var finishedGames by mutableStateOf<List<Game>>(emptyList())
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
            status = "ACTIVE"
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
                .addOnFailureListener { e ->
                    Log.e("GameViewModel", "loadFinishedGames failed: ${e.message}")
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
                        .addOnFailureListener { e ->
                            Log.e("GameViewModel", "loadFinishedGames failed: ${e.message}")
                        }
                } else {
                    finishedGames = emptyList()
                }
            }
        }
    }

    fun logStat(playerName: String, statType: String) {
        val game = currentGame ?: return
        val currentStats = game.playerStats[playerName] ?: PlayerGameStats()
        
        val newStats = when (statType) {
            "Points" -> {
                val pointsToAdd = if (game.settings.scoringType == ScoringType.ONES_AND_TWOS) 1 else 2
                currentStats.copy(points = currentStats.points + pointsToAdd)
            }
            "Rebounds" -> currentStats.copy(rebounds = currentStats.rebounds + 1)
            "Blocks" -> currentStats.copy(blocks = currentStats.blocks + 1)
            "Steals" -> currentStats.copy(steals = currentStats.steals + 1)
            else -> currentStats
        }

        val isTeam1 = game.team1.contains(playerName)
        val scoreUpdate = if (statType == "Points") {
            val pointsToAdd = if (game.settings.scoringType == ScoringType.ONES_AND_TWOS) 1 else 2
            if (isTeam1) Pair(game.score1 + pointsToAdd, game.score2)
            else Pair(game.score1, game.score2 + pointsToAdd)
        } else Pair(game.score1, game.score2)

        val updatedPlayerStats = game.playerStats.toMutableMap()
        updatedPlayerStats[playerName] = newStats

        db.collection("games").document(game.id).update(
            "playerStats", updatedPlayerStats,
            "score1", scoreUpdate.first,
            "score2", scoreUpdate.second
        )
    }

    fun togglePause() {
        val game = currentGame ?: return
        db.collection("games").document(game.id).update("isPaused", !game.isPaused)
    }

    fun endGame(onComplete: (String) -> Unit) {
        val game = currentGame ?: return
        db.collection("games").document(game.id).update("status", "FINISHED")
            .addOnSuccessListener { 
                onComplete(game.id) 
            }
    }
}
