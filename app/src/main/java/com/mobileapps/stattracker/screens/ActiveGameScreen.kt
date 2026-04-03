package com.mobileapps.stattracker.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.classes.ScoringType
import com.mobileapps.stattracker.classes.WinCondition
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor
import com.mobileapps.stattracker.ui.theme.SurfaceColor
import com.mobileapps.stattracker.viewmodels.GameViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    gameId: String,
    onGameEnded: (String) -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val game = gameViewModel.currentGame
    val context = LocalContext.current
    
    // Always-on ticking state to drive timer smoothly
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    // Derived timer value
    val timeLeft = remember(game, currentTime) {
        if (game?.settings?.winCondition == WinCondition.TIMER) {
            val totalRemainingMs = game.durationSeconds * 1000L
            if (game.status == "ACTIVE" && !game.paused && game.startTime != null) {
                val elapsed = currentTime - game.startTime
                (totalRemainingMs - elapsed).coerceAtLeast(0L)
            } else {
                // If paused or just starting, show the saved duration
                totalRemainingMs
            }
        } else 0L
    }

    LaunchedEffect(gameId) {
        gameViewModel.loadGame(gameId)
    }

    // Always-on ticker loop
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(200)
        }
    }

    // Auto-end game logic
    LaunchedEffect(timeLeft) {
        if (game?.settings?.winCondition == WinCondition.TIMER && game.status == "ACTIVE" && !game.paused && timeLeft <= 0) {
            gameViewModel.endGame(onGameEnded)
        }
    }

    // Shake Detection logic
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        var lastShakeTime = 0L
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                if (acceleration > 12) { 
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > 1000) { 
                        lastShakeTime = now
                        if (gameViewModel.currentGame?.status == "ACTIVE") {
                            gameViewModel.togglePause()
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
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
                        if (game?.paused == true) {
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
                // Timer
                if (game.settings.winCondition == WinCondition.TIMER) {
                    val minutes = (timeLeft / 1000) / 60
                    val seconds = (timeLeft / 1000) % 60
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                        color = if (timeLeft < 10000) Color.Red else MainColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                    TeamStatList("Team 1", game.team1, gameViewModel, onGameEnded)
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamStatList("Team 2", game.team2, gameViewModel, onGameEnded)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controls
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { gameViewModel.togglePause(); Log.d("Game", "Paused: ${game.paused}") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (game.paused) Color.Green else Color.Gray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (game.paused) "Resume\n(Shake to Resume)" else "Pause\n(Shake to Pause)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
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
fun TeamStatList(label: String, players: List<String>, viewModel: GameViewModel, onGameEnded: (String) -> Unit) {
    Column(modifier = Modifier.width(180.dp)) {
        Text(label, color = MainColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(players) { playerName ->
                PlayerStatCard(playerName, viewModel, onGameEnded)
            }
        }
    }
}

@Composable
fun PlayerStatCard(name: String, viewModel: GameViewModel, onGameEnded: (String) -> Unit) {
    val game = viewModel.currentGame ?: return
    val scoringType = game.settings.scoringType
    
    val (point1, point2) = if (scoringType == ScoringType.ONES_AND_TWOS) {
        1 to 2
    } else {
        2 to 3
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatButton("+$point1", { if (!game.paused) viewModel.logStat(name, "Points", point1, onGameEnded) }, enabled = !game.paused)
                StatButton("+$point2", { if (!game.paused) viewModel.logStat(name, "Points", point2, onGameEnded) }, enabled = !game.paused)
                StatButton("REB", { if (!game.paused) viewModel.logStat(name, "Rebounds") }, enabled = !game.paused)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatButton("BLK", { if (!game.paused) viewModel.logStat(name, "Blocks") }, enabled = !game.paused)
                StatButton("STL", { if (!game.paused) viewModel.logStat(name, "Steals") }, enabled = !game.paused)
            }
        }
    }
}

@Composable
fun StatButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(
        onClick = if (enabled) onClick else ({}),
        color = if (enabled) MainColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.size(width = 40.dp, height = 30.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (enabled) MainColor else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
