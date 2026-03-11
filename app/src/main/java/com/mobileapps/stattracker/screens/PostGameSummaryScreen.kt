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
import com.mobileapps.stattracker.classes.Game
import com.mobileapps.stattracker.classes.PlayerGameStats
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor
import com.mobileapps.stattracker.ui.theme.SurfaceColor
import com.mobileapps.stattracker.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostGameSummaryScreen(
    gameId: String,
    onDoneClick: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    var game by remember { mutableStateOf<Game?>(null) }

    LaunchedEffect(gameId) {
        gameViewModel.loadGame(gameId)
    }

    game = gameViewModel.currentGame

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Summary", color = MainColor, fontWeight = FontWeight.Bold) },
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
                // Final Score
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("FINAL SCORE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${game?.score1}", color = if ((game?.score1 ?: 0) > (game?.score2 ?: 0)) MainColor else Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                            Text("-", color = Color.Gray, fontSize = 32.sp)
                            Text("${game?.score2}", color = if ((game?.score2 ?: 0) > (game?.score1 ?: 0)) MainColor else Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Player Stats", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allPlayers = (game?.team1 ?: emptyList()) + (game?.team2 ?: emptyList())
                    items(allPlayers) { playerName ->
                        val stats = game?.playerStats?.get(playerName) ?: PlayerGameStats()
                        StatSummaryCard(playerName, stats)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDoneClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainColor)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun StatSummaryCard(name: String, stats: PlayerGameStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.width(100.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem("PTS", stats.points)
                StatItem("REB", stats.rebounds)
                StatItem("BLK", stats.blocks)
                StatItem("STL", stats.steals)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Text("$value", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
