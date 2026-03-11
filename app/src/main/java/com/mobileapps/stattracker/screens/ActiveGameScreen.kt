package com.mobileapps.stattracker.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor
import com.mobileapps.stattracker.ui.theme.SurfaceColor
import com.mobileapps.stattracker.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    gameId: String,
    onGameEnded: (String) -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val game = gameViewModel.currentGame

    LaunchedEffect(gameId) {
        gameViewModel.loadGame(gameId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Game", color = MainColor, fontWeight = FontWeight.Bold)
                        if (game?.isPaused == true) {
                            Text("PAUSED", color = Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        if (game == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scoreboard
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TEAM 1", color = Color.Gray, fontSize = 12.sp)
                            Text("${game.score1}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("-", color = MainColor, fontSize = 32.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TEAM 2", color = Color.Gray, fontSize = 12.sp)
                            Text("${game.score2}", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Interface
                Row(modifier = Modifier.weight(1f)) {
                    TeamStatList("Team 1", game.team1, gameViewModel)
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamStatList("Team 2", game.team2, gameViewModel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controls
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { gameViewModel.togglePause() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (game.isPaused) Color.Green else Color.Gray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (game.isPaused) "Resume" else "Pause", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { gameViewModel.endGame(onGameEnded) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MainColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("End Game", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TeamStatList(label: String, players: List<String>, viewModel: GameViewModel) {
    Column(modifier = Modifier.width(180.dp)) {
        Text(label, color = MainColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(players) { playerName ->
                PlayerStatCard(playerName, viewModel)
            }
        }
    }
}

@Composable
fun PlayerStatCard(name: String, viewModel: GameViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatButton("PTS", { viewModel.logStat(name, "Points") })
                StatButton("REB", { viewModel.logStat(name, "Rebounds") })
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatButton("BLK", { viewModel.logStat(name, "Blocks") })
                StatButton("STL", { viewModel.logStat(name, "Steals") })
            }
        }
    }
}

@Composable
fun StatButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MainColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.size(width = 40.dp, height = 30.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = MainColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
