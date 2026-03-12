package com.mobileapps.stattracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.classes.Group
import com.mobileapps.stattracker.classes.PlayerTotals
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor
import com.mobileapps.stattracker.ui.theme.SurfaceColor
import com.mobileapps.stattracker.viewmodels.GameViewModel
import com.mobileapps.stattracker.viewmodels.GroupViewModel

enum class SortStat(val label: String) {
    WINS("Wins"), POINTS("Points"), REBOUNDS("Rebounds"), BLOCKS("Blocks"), STEALS("Steals")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    groupId: String,
    onBackClick: () -> Unit,
    onStartGameClick: (String) -> Unit,
    onViewPastGamesClick: (String) -> Unit,
    groupViewModel: GroupViewModel = viewModel(),
    gameViewModel: GameViewModel = viewModel()
) {
    var group by remember { mutableStateOf<Group?>(null) }
    var memberName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var sortBy by remember { mutableStateOf(SortStat.WINS) }

    LaunchedEffect(groupId) {
        groupViewModel.loadGroupById(groupId) { fetchedGroup ->
            group = fetchedGroup
            isLoading = false
        }
        gameViewModel.loadLeaderboard(groupId)
    }

    val sortedLeaderboard = remember(gameViewModel.leaderboard, sortBy) {
        gameViewModel.leaderboard.entries.sortedByDescending {
            when (sortBy) {
                SortStat.WINS -> it.value.wins
                SortStat.POINTS -> it.value.points
                SortStat.REBOUNDS -> it.value.rebounds
                SortStat.BLOCKS -> it.value.blocks
                SortStat.STEALS -> it.value.steals
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Loading...", color = MainColor, fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainColor)
            }
        } else if (group == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Group not found", color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Action buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onStartGameClick(groupId) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MainColor)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start Game", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Button(
                        onClick = { onViewPastGamesClick(groupId) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MainColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Past Games", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MainColor)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Compact add member row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = memberName,
                        onValueChange = { memberName = it },
                        label = { Text("Add player", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MainColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = MainColor
                        )
                    )
                    Button(
                        onClick = {
                            if (memberName.isNotBlank()) {
                                groupViewModel.addMemberToGroup(groupId, memberName) {
                                    memberName = ""
                                    groupViewModel.loadGroupById(groupId) { group = it }
                                    gameViewModel.loadLeaderboard(groupId)
                                }
                            }
                        },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MainColor),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Leaderboard header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Leaderboard", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${group?.members?.size ?: 0} players", color = Color.Gray, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sort chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SortStat.entries) { stat ->
                        FilterChip(
                            selected = sortBy == stat,
                            onClick = { sortBy = stat },
                            label = { Text(stat.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MainColor,
                                selectedLabelColor = Color.Black,
                                containerColor = SurfaceColor,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Column headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Spacer(modifier = Modifier.width(28.dp))
                    }
                    listOf("WIN", "PTS", "REB", "BLK", "STL").forEach { col ->
                        Text(
                            col,
                            color = if (
                                (col == "WIN" && sortBy == SortStat.WINS) ||
                                (col == "PTS" && sortBy == SortStat.POINTS) ||
                                (col == "REB" && sortBy == SortStat.REBOUNDS) ||
                                (col == "BLK" && sortBy == SortStat.BLOCKS) ||
                                (col == "STL" && sortBy == SortStat.STEALS)
                            ) MainColor else Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Leaderboard rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(SurfaceColor, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (sortedLeaderboard.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No stats yet — play some games!", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        itemsIndexed(sortedLeaderboard) { index, (name, totals) ->
                            LeaderboardRow(rank = index + 1, name = name, totals = totals, sortBy = sortBy)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, name: String, totals: PlayerTotals, sortBy: SortStat) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text("$rank", color = rankColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            listOf(
                Pair(totals.wins, SortStat.WINS),
                Pair(totals.points, SortStat.POINTS),
                Pair(totals.rebounds, SortStat.REBOUNDS),
                Pair(totals.blocks, SortStat.BLOCKS),
                Pair(totals.steals, SortStat.STEALS)
            ).forEach { (value, stat) ->
                Text(
                    "$value",
                    color = if (sortBy == stat) MainColor else Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (sortBy == stat) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}