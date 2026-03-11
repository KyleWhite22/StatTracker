package com.mobileapps.stattracker.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.classes.GameSettings
import com.mobileapps.stattracker.classes.Group
import com.mobileapps.stattracker.classes.ScoringType
import com.mobileapps.stattracker.classes.WinCondition
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor
import com.mobileapps.stattracker.ui.theme.SurfaceColor
import com.mobileapps.stattracker.viewmodels.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGameScreen(
    groupId: String,
    onBackClick: () -> Unit,
    onStartGame: (GameSettings, List<String>, List<String>) -> Unit,
    groupViewModel: GroupViewModel = viewModel()
) {
    Log.d("Lifecycle", "Create Game composed")
    var group by remember { mutableStateOf<Group?>(null) }
    var settings by remember { mutableStateOf(GameSettings()) }
    var team1 by remember { mutableStateOf<List<String>>(emptyList()) }
    var team2 by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        groupViewModel.loadGroupById(groupId) { group = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Game", color = MainColor, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Game Settings Toggle
            Button(
                onClick = { showSettings = !showSettings },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (showSettings) "Hide Settings" else "Game Settings", color = MainColor)
            }

            if (showSettings) {
                Card(
                    modifier = Modifier.padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Win Condition", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settings.winCondition == WinCondition.FIRST_TO_21,
                                onClick = { settings = settings.copy(winCondition = WinCondition.FIRST_TO_21) },
                                colors = RadioButtonDefaults.colors(selectedColor = MainColor)
                            )
                            Text("First to 21", color = Color.White)
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = settings.winCondition == WinCondition.TIMER,
                                onClick = { settings = settings.copy(winCondition = WinCondition.TIMER) },
                                colors = RadioButtonDefaults.colors(selectedColor = MainColor)
                            )
                            Text("Timer", color = Color.White)
                        }

                        Spacer(Modifier.height(8.dp))

                        Text("Scoring", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settings.scoringType == ScoringType.ONES_AND_TWOS,
                                onClick = { settings = settings.copy(scoringType = ScoringType.ONES_AND_TWOS) },
                                colors = RadioButtonDefaults.colors(selectedColor = MainColor)
                            )
                            Text("1s & 2s", color = Color.White)
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = settings.scoringType == ScoringType.TWOS_AND_THREES,
                                onClick = { settings = settings.copy(scoringType = ScoringType.TWOS_AND_THREES) },
                                colors = RadioButtonDefaults.colors(selectedColor = MainColor)
                            )
                            Text("2s & 3s", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Team Selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TeamColumn("Team 1", team1, { team1 = it }, group?.members ?: emptyList())
                TeamColumn("Team 2", team2, { team2 = it }, group?.members ?: emptyList())
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val shuffled = (group?.members ?: emptyList()).shuffled()
                    val mid = shuffled.size / 2
                    team1 = shuffled.take(mid)
                    team2 = shuffled.drop(mid)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor)
            ) {
                Text("Generate Fair Teams", color = Color.White)
            }

            Button(
                onClick = { onStartGame(settings, team1, team2) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainColor),
                enabled = team1.isNotEmpty() && team2.isNotEmpty()
            ) {
                Text("Start Game", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun TeamColumn(label: String, team: List<String>, onUpdate: (List<String>) -> Unit, allMembers: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.width(150.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MainColor, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(SurfaceColor, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            LazyColumn {
                items(team) { member ->
                    Text(member, color = Color.White, modifier = Modifier.padding(4.dp).clickable { onUpdate(team - member) })
                }
            }
        }
        Button(onClick = { expanded = true }, modifier = Modifier.padding(top = 4.dp)) {
            Text("Add", fontSize = 12.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            allMembers.forEach { member ->
                DropdownMenuItem(
                    text = { Text(member) },
                    onClick = {
                        if (!team.contains(member)) onUpdate(team + member)
                        expanded = false
                    }
                )
            }
        }
    }
}
