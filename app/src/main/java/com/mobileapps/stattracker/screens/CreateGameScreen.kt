package com.mobileapps.stattracker.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var timerInput by remember { mutableStateOf("10") }
    var newPlayerName by remember { mutableStateOf("") }
    var teamSizeError by remember { mutableStateOf("") }

    LaunchedEffect(groupId) {
        groupViewModel.loadGroupById(groupId) { group = it }
        groupViewModel.loadWinRates(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Up Game", color = MainColor, fontWeight = FontWeight.Bold) },
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

                        // Win Condition
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

                        if (settings.winCondition == WinCondition.TIMER) {
                            OutlinedTextField(
                                value = timerInput,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() }) {
                                        timerInput = it
                                        settings = settings.copy(timerDurationMinutes = it.toIntOrNull() ?: 10)
                                    }
                                },
                                label = { Text("Duration (minutes)", color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = MainColor,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Scoring
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

                        Spacer(Modifier.height(8.dp))

                        // Team Size
                        Text("Team Size", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf(1, 2, 3, 4, 5).forEach { size ->
                                val selected = settings.teamSize == size
                                Surface(
                                    onClick = {
                                        settings = settings.copy(teamSize = size)
                                        team1 = emptyList()
                                        team2 = emptyList()
                                        teamSizeError = ""
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) MainColor else Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "$size",
                                            color = if (selected) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val members = group?.members ?: emptyList()
                    val needed = settings.teamSize * 2
                    if (members.size < needed) {
                        teamSizeError = "Not enough players — need $needed, have ${members.size} \n Add more players in group screen"
                        team1 = emptyList()
                        team2 = emptyList()
                    } else {
                        teamSizeError = ""
                        // Sort all members by win rate descending (0.0 for players with no history)
                        val ranked = members.sortedByDescending { groupViewModel.winRates[it] ?: 0.0 }
                        // Snake draft: pick teamSize players per team from the ranked list
                        // Positions 0,2,4,... → team1 picks; 1,3,5,... → team2 picks
                        val t1 = mutableListOf<String>()
                        val t2 = mutableListOf<String>()
                        ranked.take(needed).forEachIndexed { index, player ->
                            if (index % 2 == 0) t1.add(player) else t2.add(player)
                        }
                        team1 = t1
                        team2 = t2
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generate Fair Teams (${settings.teamSize}v${settings.teamSize})", color = Color.White)
            }

            if (teamSizeError.isNotEmpty()) {
                Text(teamSizeError, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // Team columns
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TeamColumn("Team 1", team1, team2, { team1 = it }, group?.members ?: emptyList())
                TeamColumn("Team 2", team2, team1, { team2 = it }, group?.members ?: emptyList())
            }

            Spacer(modifier = Modifier.weight(1f))

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
fun TeamColumn(label: String, team: List<String>, otherTeam: List<String>, onUpdate: (List<String>) -> Unit, allMembers: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val availableMembers = allMembers.filter { !team.contains(it) && !otherTeam.contains(it) }

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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            member,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "✕",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onUpdate(team - member) }.padding(4.dp)
                        )
                    }
                }
            }
        }
        Button(onClick = { expanded = true }, modifier = Modifier.padding(top = 4.dp)) {
            Text("Add", fontSize = 12.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (availableMembers.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No players available", color = Color.Gray) },
                    onClick = { expanded = false })
            } else {
                availableMembers.forEach { member ->
                    DropdownMenuItem(
                        text = { Text(member) },
                        onClick = {
                            onUpdate(team + member)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}