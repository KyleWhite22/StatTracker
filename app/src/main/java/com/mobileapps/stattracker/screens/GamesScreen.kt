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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.R
import com.mobileapps.stattracker.classes.Game
import com.mobileapps.stattracker.classes.PlayerGameStats
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor
import com.mobileapps.stattracker.ui.theme.SurfaceColor
import com.mobileapps.stattracker.viewmodels.GameViewModel
import com.mobileapps.stattracker.viewmodels.GroupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    groupId: String?,
    onBackClick: () -> Unit,
    gameViewModel: GameViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    Log.d("Lifecycle", "Games composed")
    val finishedGames = gameViewModel.finishedGames

    // Use a remembered map that persists across recompositions and page changes.
    // We only fetch a group name once — if we already have it, we skip the Firestore call.
    val groupNames = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(groupId) {
        gameViewModel.loadFinishedGames(groupId)
    }

    // Only fetch names for IDs we haven't seen yet
    LaunchedEffect(finishedGames) {
        val unseenIds = finishedGames
            .map { it.groupId }
            .distinct()
            .filter { it.isNotBlank() && !groupNames.containsKey(it) }

        unseenIds.forEach { id ->
            groupViewModel.loadGroupById(id) { group ->
                if (group != null) groupNames[id] = group.name
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (groupId == null || groupId == "all") stringResource(R.string.all_past_games) else stringResource(R.string.past_games),
                        color = MainColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MainColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        if (finishedGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_past_games), color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(finishedGames, key = { it.id }) { game ->
                        GameResultCard(game = game, groupName = groupNames[game.groupId] ?: "")
                    }
                }

                // Pagination controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { gameViewModel.prevPage() },
                        enabled = gameViewModel.hasPrevPage,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor)
                    ) {
                        Text(
                            stringResource(R.string.prev),
                            color = if (gameViewModel.hasPrevPage) MainColor else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { gameViewModel.nextPage() },
                        enabled = gameViewModel.hasNextPage,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor)
                    ) {
                        Text(
                            stringResource(R.string.next),
                            color = if (gameViewModel.hasNextPage) MainColor else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameResultCard(game: Game, groupName: String) {
    val sdf = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }
    val dateString = remember(game.date) { sdf.format(Date(game.date)) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(groupName, color = MainColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(dateString, color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.team_1), color = Color.White, fontSize = 14.sp)
                    Text(
                        "${game.score1}",
                        color = if (game.score1 > game.score2) MainColor else Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(stringResource(R.string.vs), color = Color.Gray, fontWeight = FontWeight.ExtraBold)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.team_2), color = Color.White, fontSize = 14.sp)
                    Text(
                        "${game.score2}",
                        color = if (game.score2 > game.score1) MainColor else Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(8.dp))

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.player_header), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    listOf(
                        stringResource(R.string.header_pts),
                        stringResource(R.string.header_reb),
                        stringResource(R.string.header_blk),
                        stringResource(R.string.header_stl)
                    ).forEach {
                        Text(
                            it,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val allPlayers = remember(game.team1, game.team2) { game.team1 + game.team2 }
                allPlayers.forEach { playerName ->
                    val stats = game.playerStats[playerName] ?: PlayerGameStats()
                    val isTeam1 = game.team1.contains(playerName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                            Text(
                                "$value",
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Text(
                if (expanded) stringResource(R.string.collapse) else stringResource(R.string.tap_to_expand),
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            )
        }
    }
}