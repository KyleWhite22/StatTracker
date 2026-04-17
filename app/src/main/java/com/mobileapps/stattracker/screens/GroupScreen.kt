package com.mobileapps.stattracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.R
import com.mobileapps.stattracker.classes.Group
import com.mobileapps.stattracker.classes.PlayerTotals
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor
import com.mobileapps.stattracker.ui.theme.SurfaceColor
import com.mobileapps.stattracker.viewmodels.GameViewModel
import com.mobileapps.stattracker.viewmodels.GroupViewModel

enum class SortStat(val labelRes: Int) {
    WINS(R.string.sort_wins),
    WIN_PCT(R.string.sort_win_pct),
    POINTS(R.string.sort_points),
    REBOUNDS(R.string.sort_rebounds),
    BLOCKS(R.string.sort_blocks),
    STEALS(R.string.sort_steals)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    groupId: String,
    onBackClick: () -> Unit,
    onStartGameClick: (String) -> Unit,
    onResumeGameClick: (String, String) -> Unit,
    onViewPastGamesClick: (String) -> Unit,
    onDeleteGroupClick: () -> Unit,
    groupViewModel: GroupViewModel = viewModel(),
    gameViewModel: GameViewModel = viewModel()
) {
    var group by remember { mutableStateOf<Group?>(null) }
    var memberName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var sortBy by remember { mutableStateOf(SortStat.WINS) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        groupViewModel.loadGroupById(groupId) { fetchedGroup ->
            group = fetchedGroup
            isLoading = false
        }
        gameViewModel.loadLeaderboard(groupId)
        gameViewModel.loadActiveGames(groupId)
    }

    val sortedLeaderboard = remember(gameViewModel.leaderboard, sortBy, group) {
        gameViewModel.leaderboard.entries
            .filter { group?.members?.contains(it.key) == true }
            .sortedByDescending {
                when (sortBy) {
                    SortStat.WINS -> it.value.wins.toFloat()
                    SortStat.WIN_PCT -> it.value.winPct
                    SortStat.POINTS -> it.value.points.toFloat()
                    SortStat.REBOUNDS -> it.value.rebounds.toFloat()
                    SortStat.BLOCKS -> it.value.blocks.toFloat()
                    SortStat.STEALS -> it.value.steals.toFloat()
                }
            }
    }

    // Rename Dialog
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(group?.name ?: "") }
        Dialog(onDismissRequest = { showRenameDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.rename_group), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.group_name), color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = { showRenameDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.cancel), color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    groupViewModel.renameGroup(groupId, newName) {
                                        groupViewModel.loadGroupById(groupId) { group = it }
                                    }
                                    showRenameDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MainColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(stringResource(R.string.save), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = SurfaceColor,
            title = { Text(stringResource(R.string.delete_group), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_group_confirm), color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        groupViewModel.deleteGroup(groupId) { onDeleteGroupClick() }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.delete), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(group?.name ?: stringResource(R.string.loading), color = MainColor, fontWeight = FontWeight.Bold)
                        if (group?.location?.isNotBlank() == true) {
                            Text(group?.location ?: "", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MainColor)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.options), tint = MainColor)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = SurfaceColor
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.rename_group), color = Color.White) },
                                onClick = {
                                    menuExpanded = false
                                    showRenameDialog = true
                                }
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete_group), color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
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
                Text(stringResource(R.string.group_not_found), color = Color.White)
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
                        Text(stringResource(R.string.start_game), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Button(
                        onClick = { onViewPastGamesClick(groupId) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MainColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.past_games), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MainColor)
                    }
                }

                if (gameViewModel.activeGames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.in_progress_games), color = MainColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))
                    gameViewModel.activeGames.forEach { activeGame ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onResumeGameClick(activeGame.id, groupId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MainColor.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stringResource(R.string.game_in_progress), color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("${activeGame.score1} - ${activeGame.score2}", color = MainColor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Button(
                                    onClick = { onResumeGameClick(activeGame.id, groupId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MainColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(stringResource(R.string.resume), color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
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
                        label = { Text(stringResource(R.string.add_player), color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
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
                        modifier = Modifier,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MainColor),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add), tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Leaderboard header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.leaderboard), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.players_count, group?.members?.size ?: 0), color = Color.Gray, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sort chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SortStat.entries) { stat ->
                        FilterChip(
                            selected = sortBy == stat,
                            onClick = { sortBy = stat },
                            label = { Text(stringResource(stat.labelRes), fontSize = 12.sp) },
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
                    listOf(
                        stringResource(R.string.header_win) to SortStat.WINS,
                        stringResource(R.string.header_win_pct) to SortStat.WIN_PCT,
                        stringResource(R.string.header_pts) to SortStat.POINTS,
                        stringResource(R.string.header_reb) to SortStat.REBOUNDS,
                        stringResource(R.string.header_blk) to SortStat.BLOCKS,
                        stringResource(R.string.header_stl) to SortStat.STEALS
                    ).forEach { (col, stat) ->
                        Text(
                            col,
                            color = if (sortBy == stat) MainColor else Color.Gray,
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
                                Text(stringResource(R.string.no_stats), color = Color.Gray, fontSize = 14.sp)
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
                Pair(totals.wins.toFloat(), SortStat.WINS),
                Pair(totals.winPct, SortStat.WIN_PCT),
                Pair(totals.points.toFloat(), SortStat.POINTS),
                Pair(totals.rebounds.toFloat(), SortStat.REBOUNDS),
                Pair(totals.blocks.toFloat(), SortStat.BLOCKS),
                Pair(totals.steals.toFloat(), SortStat.STEALS)
            ).forEach { (value, stat) ->
                val display = if (stat == SortStat.WIN_PCT) "${(value * 100).toInt()}%" else "${value.toInt()}"
                Text(
                    display,
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