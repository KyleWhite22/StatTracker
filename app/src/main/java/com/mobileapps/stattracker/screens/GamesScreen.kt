package com.mobileapps.stattracker.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.classes.Game
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor
import com.mobileapps.stattracker.ui.theme.SurfaceColor
import com.mobileapps.stattracker.viewmodels.GameViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import com.mobileapps.stattracker.classes.PlayerGameStats
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    groupId: String?,
    onBackClick: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    Log.d("Lifecycle", "Games composed")
    val finishedGames = gameViewModel.finishedGames

    LaunchedEffect(groupId) {
        gameViewModel.loadFinishedGames(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (groupId == null || groupId == "all") "All Past Games" else "Group Games", color = MainColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MainColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        if (finishedGames.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No past games found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(finishedGames) { game ->
                    GameResultCard(game)
                }
            }
        }
    }
}

@Composable
fun GameResultCard(game: Game) {
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(game.date))
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(dateString, color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Team 1", color = Color.White, fontSize = 14.sp)
                    Text("${game.score1}", color = if (game.score1 > game.score2) MainColor else Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }

                Text("VS", color = Color.Gray, fontWeight = FontWeight.ExtraBold)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Team 2", color = Color.White, fontSize = 14.sp)
                    Text("${game.score2}", color = if (game.score2 > game.score1) MainColor else Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(8.dp))


            // Expanded player stats
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Spacer(modifier = Modifier.height(12.dp))

                // Header row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Player", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    listOf("PTS", "REB", "BLK", "STL").forEach {
                        Text(it, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val allPlayers = game.team1 + game.team2
                allPlayers.forEach { playerName ->
                    val stats = game.playerStats[playerName] ?: PlayerGameStats()
                    val isTeam1 = game.team1.contains(playerName)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (isTeam1) MainColor else Color.Gray,
                                        RoundedCornerShape(50)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(playerName, color = Color.White, fontSize = 13.sp)
                        }
                        listOf(stats.points, stats.rebounds, stats.blocks, stats.steals).forEach { value ->
                            Text("$value", color = Color.White, fontSize = 13.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Tap hint
            Text(
                if (expanded) "▲ collapse" else "▼ tap to expand",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            )
        }
    }
}