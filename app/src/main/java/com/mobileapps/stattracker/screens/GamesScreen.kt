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

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            
            Text(
                "Final Result: ${if (game.score1 > game.score2) "Team 1 Win" else if (game.score2 > game.score1) "Team 2 Win" else "Draw"}",
                color = MainColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
