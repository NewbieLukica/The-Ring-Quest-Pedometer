package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StepLog
import com.example.ui.theme.*
import com.example.ui.viewmodel.PedometerViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// Milestone Representation on the Ring Quest
data class QuestMilestone(
    val name: String,
    val distanceKm: Double, // The actual distance in kilometers from Hobbiton
    val description: String,
    val region: String,
    val quote: String,
    val rank: String
) {
    // Standard conversion: 1 km = 1312 steps (approx 0.76m per step)
    val stepsNeeded: Int get() = (distanceKm * 1312.3).toInt()
}

val MiddleEarthMilestones = RichMiddleEarthMilestones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedometerScreen(
    viewModel: PedometerViewModel,
    modifier: Modifier = Modifier
) {
    val todayLog by viewModel.todayLogFlow.collectAsStateWithLifecycle(
        initialValue = StepLog(viewModel.todayDateStr, 0, 8000)
    )
    val historyLogs by viewModel.allHistoricalLogsFlow.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()
    val isSensorActive by viewModel.isSensorActive.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    // Quest state: Track if the One Ring has been successfully destroyed
    var isRingDestroyed by rememberSaveable { mutableStateOf(false) }

    var showCelebrationDialog by rememberSaveable { mutableStateOf(false) }
    var lastCelebratedDate by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(todayLog.steps, todayLog.goal) {
        if (todayLog.steps >= todayLog.goal && todayLog.goal > 0 && lastCelebratedDate != todayLog.date) {
            showCelebrationDialog = true
            lastCelebratedDate = todayLog.date
        }
    }

    // Math for Middle-earth milestones: cumulative steps on the road
    val cumulativeSteps = historyLogs.filter { it.date != viewModel.todayDateStr }.sumOf { it.steps } + todayLog.steps

    // Identify active milestone
    val currentMilestone = MiddleEarthMilestones.lastOrNull { cumulativeSteps >= it.stepsNeeded }
        ?: MiddleEarthMilestones.first()

    // Next target milestone
    val nextMilestone = MiddleEarthMilestones.firstOrNull { it.stepsNeeded > cumulativeSteps }

    if (isRingDestroyed) {
        // SPECTACULAR QUEST VICTORY SCREEN
        QuestVictoryScreen(
            totalSteps = cumulativeSteps,
            onRestartQuest = {
                viewModel.clearAllHistory()
                isRingDestroyed = false
            }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brightness5, // Stylized Eye / Glowing Ring symbol
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(26.dp)
                            )
                            Column {
                                Text(
                                    text = "THE RING QUEST",
// Font styling inspired by a grand mythical scroll
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Middle-earth Step RPG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.testTag("reset_today_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Today",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showClearHistoryDialog = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Status toast-like message
                item {
                    AnimatedStatusMessage(statusMessage)
                }

                // Cumulative Quest Progress Banner
                item {
                    QuestTrackerBanner(
                        cumulativeSteps = cumulativeSteps,
                        currentMilestone = currentMilestone,
                        nextMilestone = nextMilestone,
                        isReadyToDestroy = cumulativeSteps >= (MiddleEarthMilestones.find { it.name == "Mount Doom (Orodruin)" }?.stepsNeeded ?: 3757000),
                        onCastRing = { isRingDestroyed = true }
                    )
                }

                // The Ring of Power radial progress gauge (Today's Steps)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StepProgressGauge(
                            steps = todayLog.steps,
                            goal = todayLog.goal
                        )
                    }
                }

                // Show celebration banner if today's goal is complete
                if (todayLog.steps >= todayLog.goal && todayLog.goal > 0) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showCelebrationDialog = true
                                }
                                .testTag("goal_completed_banner"),
                            colors = CardDefaults.cardColors(
                                containerColor = ShireGreen.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Victory",
                                    tint = ShireGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🌟 DAILY MARCH COMPLETE! Read book prophecy quote 🌟",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ShireGreen,
                                    letterSpacing = 0.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Today's Calories Metric Card (Lembas Energy)
                item {
                    val caloriesBurned = todayLog.steps * 0.041
                    val lembasBites = caloriesBurned / 250.0
                    val df = DecimalFormat("0.0")
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "Calories",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "TODAY'S CALORIES BURNED",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${caloriesBurned.toInt()} kcal",
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Provides energy equivalent to ${df.format(lembasBites)} slices of Lembas Bread.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Daily March Goal Setter (small, compact, non-slider design)
                item {
                    GoalControllerSection(
                        currentGoal = todayLog.goal,
                        onGoalChange = { viewModel.updateDailyGoal(it) }
                    )
                }

                // Medieval style Interactive Milestones scroll
                item {
                    QuestMilestonesSection(cumulativeSteps = cumulativeSteps)
                }
            }

            // Confirms resetting today's march
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset Today's Paces?", fontFamily = FontFamily.Serif) },
                    text = { Text("This will reset your logged paces for today back to the Shire entrance (0 steps). The Nazgûl will notice your setback!", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.resetTodaySteps()
                                showResetDialog = false
                            }
                        ) {
                            Text("Reset Paces", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Stay Here", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Confirms purging all historical records
            if (showClearHistoryDialog) {
                AlertDialog(
                    onDismissRequest = { showClearHistoryDialog = false },
                    title = { Text("Abandon Your Campfire History?", fontFamily = FontFamily.Serif) },
                    text = { Text("This will permanently erase all steps from your database history. Perfect for starting completely fresh from the comfortable Green Dragon Inn.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.clearAllHistory()
                                showClearHistoryDialog = false
                            }
                        ) {
                            Text("Abandon History", color = MordorLava, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearHistoryDialog = false }) {
                            Text("Keep Journey", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (showCelebrationDialog) {
                GoalCelebrationDialog(
                    onDismissRequest = { showCelebrationDialog = false },
                    quotes = BookQuotes
                )
            }
        }
    }
}

// Banner showing cumulative distance, active Rank, and the Mount Doom button
@Composable
fun QuestTrackerBanner(
    cumulativeSteps: Int,
    currentMilestone: QuestMilestone,
    nextMilestone: QuestMilestone?,
    isReadyToDestroy: Boolean,
    onCastRing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isReadyToDestroy) MordorLava.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL PATH TRAVELED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${DecimalFormat("#,###").format(cumulativeSteps)} steps",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Rank label pill
                val pillColor = if (isReadyToDestroy) MordorLava else ShireGreen
                val textCol = if (isReadyToDestroy) Color.White else Color.White
                Box(
                    modifier = Modifier
                        .background(pillColor, RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentMilestone.rank,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textCol
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Currently in: ${currentMilestone.name} (${currentMilestone.region})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = currentMilestone.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Character Quote with nice italics
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = currentMilestone.quote,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            var isCurrentLoreExpanded by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { isCurrentLoreExpanded = !isCurrentLoreExpanded }
                    .padding(vertical = 4.dp)
                    .testTag("banner_current_lore_reveal")
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isCurrentLoreExpanded) ShireGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isCurrentLoreExpanded) "Conceal Landmark Lore 📜" else "Read Current Landmark Lore ✨",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = if (isCurrentLoreExpanded) ShireGreen else MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isCurrentLoreExpanded,
                enter = fadeIn(animationSpec = spring()),
                exit = fadeOut(animationSpec = spring())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37), // beautiful gold
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "LORE SECRETS & TRIVIA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFFD4AF37),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentMilestone.funFact,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            if (nextMilestone != null) {
                val stepsLeft = nextMilestone.stepsNeeded - cumulativeSteps
                val kmLeft = stepsLeft / 1312.3
                val currentNeeded = currentMilestone.stepsNeeded
                val nextNeeded = nextMilestone.stepsNeeded
                val range = nextNeeded - currentNeeded
                val ratio = if (range > 0) {
                    ((cumulativeSteps - currentNeeded).toFloat() / range.toFloat()).coerceIn(0f, 1f)
                } else 0f
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "NEXT QUEST DESTINATION: ${nextMilestone.name}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Paces to Landmark",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${DecimalFormat("#,###").format(stepsLeft)} paces",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Distance to Landmark",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${DecimalFormat("0.00").format(kmLeft)} km",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShireGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ShireGreen,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                )
            } else {
                // REVERED ENDPOINT OF THE QUEST reached!
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCastRing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MordorLava,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAST THE ONE RING INTO MOUNT DOOM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// Spectacular full screen end-celebration
@Composable
fun QuestVictoryScreen(
    totalSteps: Int,
    onRestartQuest: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LotrBgDark // Locked in dramatic dark ash cave background for extreme high fidelity
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // One Ring burning effect logic represented on-screen with gold and red gradients
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(MordorLava.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    drawCircle(
                        color = LotrProgressDark,
                        radius = size.width / 2f,
                        style = Stroke(width = 10.dp.toPx())
                    )
                    // Volcanic crack crossing the ring
                    drawLine(
                        color = MordorLava,
                        start = androidx.compose.ui.geometry.Offset(x = 10f, y = size.height / 2f),
                        end = androidx.compose.ui.geometry.Offset(x = size.width - 10f, y = size.height / 2f + 15f),
                        strokeWidth = 5.dp.toPx()
                    )
                }
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = null,
                    tint = MordorLava,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "QUEST ACCOMPLISHED!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp,
                color = LotrProgressDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "The One Ring has been consumed by the fires of Mount Doom. The Dark Lord has fallen, and the eyes of Sauron have collapsed in ruins.",
                fontSize = 14.sp,
                color = LotrTextPrimaryDark,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Middle-earth is saved!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ShireGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = LotrCardBgDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HERO STATS REPORT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LotrTextSecondaryDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total distance: ${DecimalFormat("#,###").format(totalSteps)} steps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LotrTextPrimaryDark
                    )
                    Text(
                        text = "Lembas consumed: ${(totalSteps * 0.041 / 250.0).toInt().coerceAtLeast(1)} slices",
                        fontSize = 13.sp,
                        color = LotrTextSecondaryDark
                    )
                    Text(
                        text = "Leagues marched: ${DecimalFormat("0.0").format((totalSteps / 1312.3) / 4.8)} leagues",
                        fontSize = 13.sp,
                        color = LotrTextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onRestartQuest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShireGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Begin a New Legend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AnimatedStatusMessage(message: String) {
    var previousMessage by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message.isNotEmpty() && message != previousMessage) {
            previousMessage = message
            visible = true
            delay(3000)
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(100.dp),
                border = CardDefaults.outlinedCardBorder(true)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Step Progress circular gauge represented as THE ONE RING OF POWER
@Composable
fun StepProgressGauge(
    steps: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val df = DecimalFormat("#,###")
    val stepsFormatted = df.format(steps)
    val progress = if (goal > 0) (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f

    // Smooth sweeping animation for the progress sweep
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 60f),
        label = "progress"
    )

    val ringColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .size(240.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw the decorative golden Ring in Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            
            // Outer golden-mist shadow
            drawCircle(
                color = ringColor.copy(alpha = 0.05f),
                radius = size.width / 2f - strokeWidth + 8f,
                style = Stroke(width = strokeWidth + 12f)
            )

            // Outer background shadowed track
            drawCircle(
                color = trackColor,
                radius = size.width / 2f - strokeWidth,
                style = Stroke(width = strokeWidth)
            )

            // Gloriously glowing forge progress arc of the Ring
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner display statistics
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stepsFormatted,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-1).sp,
                modifier = Modifier.testTag("steps_display")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "DAILY PACES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ringColor,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
fun GoalControllerSection(
    currentGoal: Int,
    onGoalChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DAILY MARCH GOAL",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${DecimalFormat("#,###").format(currentGoal)} paces",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onGoalChange((currentGoal - 500).coerceAtLeast(1000)) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .testTag("decrease_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease Goal",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { onGoalChange((currentGoal + 500).coerceAtMost(50000)) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .testTag("increase_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase Goal",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Stats table themed like Shire journey parameters
@Composable
fun HealthMetricsGrid(steps: Int) {
    val distanceKm = steps / 1312.3
    
    // Leagues math (1 league = ~4.8 km)
    val distanceLeagues = distanceKm / 4.8
    
    // Lembas bread math (let's say average travelers burn ~250 kcal per bite/slice of standard travel supplies)
    val caloriesBurned = steps * 0.041
    val lembasBites = caloriesBurned / 250.0
    
    // Duration
    val activeSeconds = (steps * 0.75).toLong()
    val activeMinutes = activeSeconds / 60
    val durationStr = if (activeMinutes >= 60) "${activeMinutes / 60}h ${activeMinutes % 60}m" else "${activeMinutes}m ${activeSeconds % 60}s"
    
    // Fangorn Woodland protected (CO2 savings translated equivalent of trees/foliage scale)
    val treesSavedLeaves = steps * 0.013 // arbitrary elven leaves clean factor

    val df = DecimalFormat("0.00")
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = ShireGreen
    val lavaColor = MordorLava
    val blueColor = MoriaStarlight

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthMetricCard(
                title = "March Leagues",
                value = "${df.format(distanceLeagues)} lg",
                subText = "${df.format(distanceKm)} km total",
                icon = Icons.Default.Explore,
                iconTint = secondaryColor,
                modifier = Modifier.weight(1f)
            )
            HealthMetricCard(
                title = "Lembas Energy",
                value = if (lembasBites < 0.1) "Crumb level" else "${df.format(lembasBites)} slices",
                subText = "${caloriesBurned.toInt()} kcal burned",
                icon = Icons.Default.Restaurant,
                iconTint = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthMetricCard(
                title = "Tactical March",
                value = durationStr,
                subText = "Travel time",
                icon = Icons.Default.TrendingUp,
                iconTint = blueColor,
                modifier = Modifier.weight(1f)
            )
            HealthMetricCard(
                title = "Fangorn Growth",
                value = "${df.format(treesSavedLeaves)} leaves",
                subText = "Purity of air stored",
                icon = Icons.Default.Eco,
                iconTint = lavaColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    subText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subText,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Timeline representing points of interest and milestones
@Composable
fun QuestMilestonesSection(
    cumulativeSteps: Int
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Points of Interest & Quest Landmarks",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Paces taken in previous days increase your long-term campaign steps.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val visibleMilestones = if (isExpanded) {
                MiddleEarthMilestones
            } else {
                MiddleEarthMilestones.take(3)
            }

            // Keep track of which milestones' fun facts are expanded
            val expandedFacts = remember { mutableStateMapOf<String, Boolean>() }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                visibleMilestones.forEach { milestone ->
                    val isAchieved = cumulativeSteps >= milestone.stepsNeeded
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Vertical checkpoint node indicators
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(28.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = if (isAchieved) {
                                            if (milestone.name.contains("Mount Doom")) MordorLava else ShireGreen
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAchieved) Icons.Default.Check else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = milestone.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = if (isAchieved) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (milestone.distanceKm == 0.0) "Start" else "${DecimalFormat("#,###.##").format(milestone.distanceKm)} km (${DecimalFormat("#,###").format(milestone.stepsNeeded)} steps)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAchieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = milestone.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            if (isAchieved) {
                                val isFactExpanded = expandedFacts[milestone.name] == true
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { expandedFacts[milestone.name] = !isFactExpanded }
                                        .padding(vertical = 4.dp)
                                        .testTag("reveal_lore_${milestone.name.replace(" ", "_")}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isFactExpanded) ShireGreen else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isFactExpanded) "Conceal Secret Lore 📜" else "Read Secret Lore ✨",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = if (isFactExpanded) ShireGreen else MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                AnimatedVisibility(
                                    visible = isFactExpanded,
                                    enter = fadeIn(animationSpec = spring()),
                                    exit = fadeOut(animationSpec = spring())
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Explore,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD4AF37), // beautiful gold
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "LORE UNLOCKED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Serif,
                                                    color = Color(0xFFD4AF37),
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = milestone.funFact,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontStyle = FontStyle.Italic,
                                                fontFamily = FontFamily.Serif,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Locked hint to motivate walking
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Secret lore locked. Reach this landmark to decode.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (MiddleEarthMilestones.size > 3) {
                Spacer(modifier = Modifier.height(12.dp))
                val hiddenCount = MiddleEarthMilestones.size - 3
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_milestones_button")
                ) {
                    Text(
                        text = if (isExpanded) "Show Less Landmarks ⬆" else "Show All Landmarks (+${hiddenCount}) ⬇",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SimulationControls(
    isSimulating: Boolean,
    isSensorActive: Boolean,
    onSimulateOneStep: () -> Unit,
    onSimulate100Steps: () -> Unit,
    onSimulate1000Steps: () -> Unit,
    onToggleSimulation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Travel Simulator",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (isSensorActive) ShireGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (isSensorActive) "Elven Map Linked" else "Static Camp",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSimulateOneStep,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_1_step_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Take Paces", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSimulate100Steps,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("add_100_steps_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+100 March", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSimulate1000Steps,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("add_1000_steps_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+1k Ride 🐎", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onToggleSimulation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSimulating) MordorLava.copy(alpha = 0.2f) else ShireGreen,
                    contentColor = if (isSimulating) MordorLava else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continuous_simulation_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (isSimulating) Icons.Default.Refresh else Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSimulating) "Stop auto expedition paces" else "Auto-March to Mount Doom 🥾",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StepHistorySection(logs: List<StepLog>) {
    val sortedRecentLogs = logs.sortedBy { it.date }.takeLast(7)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Campfire Archival History",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (sortedRecentLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Haven't stepped out of the tavern. Start marching!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val maxSteps = sortedRecentLogs.maxOfOrNull { it.steps }?.coerceAtLeast(1) ?: 1
                val primaryColor = MaterialTheme.colorScheme.primary
                val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    sortedRecentLogs.forEach { log ->
                        val ratio = log.steps.toFloat() / maxSteps.toFloat()
                        val barHeightFactor = ratio.coerceAtLeast(0.08f)
                        
                        val dayLabel = getDayOfWeekLabel(log.date)
                        val isGoalAchieved = log.steps >= log.goal

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = formatKSteps(log.steps),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoalAchieved) ShireGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Visual bar
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .fillMaxHeight(0.72f * barHeightFactor)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = if (isGoalAchieved) {
                                                listOf(ShireGreen.copy(alpha = 0.8f), ShireGreen)
                                            } else {
                                                listOf(mutedColor.copy(alpha = 0.5f), mutedColor)
                                            }
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dayLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (log.date == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatKSteps(steps: Int): String {
    return if (steps >= 1000) {
        val kValue = steps / 1000.0
        val df = DecimalFormat("0.#")
        "${df.format(kValue)}k"
    } else {
        steps.toString()
    }
}

private fun getDayOfWeekLabel(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return ""
        val cal = Calendar.getInstance()
        cal.time = date
        val todayStr = sdf.format(Date())
        if (dateStr == todayStr) {
            "TODAY"
        } else {
            val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            days[cal.get(Calendar.DAY_OF_WEEK) - 1]
        }
    } catch (e: Exception) {
         ""
    }
}

val BookQuotes = listOf(
    "All that is gold does not glitter, not all those who wander are lost; the old that is strong does not wither, deep roots are not reached by the frost.",
    "Even the smallest person can change the course of the future.",
    "There is some good in this world, and it's worth fighting for.",
    "I will take the Ring, though I do not know the way.",
    "The Road goes ever on and on, down from the door where it began.",
    "Courage is found in unlikely places.",
    "The praise of the praiseworthy is above all rewards.",
    "Not all tears are an evil.",
    "It is a lovely thing, Middle-earth, in the high place than which there is no higher.",
    "A red sun rises. Blood has been spilled this night.",
    "He that breaks a thing to find out what it is has left the path of wisdom.",
    "Despair is only for those who see the end beyond all doubt.",
    "Faithless is he that says farewell when the road darkens.",
    "Often does hatred hurt itself!",
    "May it be a light to you in dark places, when all other lights go out.",
    "Soft as shadow, strong as oak, the wandering hearts of wilderness spoke.",
    "The world is indeed full of peril, and in it there are many dark places; but still there is much that is fair.",
    "Step by step, hope is reborn. The shadows of today are but a passing story.",
    "The morning will come again; indeed, the shadow is but a small and passing thing.",
    "Short cuts make long delays.",
    "Praise the day when the sun is set.",
    "You have nice friends, and you are nice yourself; but you must go, and you must not come back until you are bigger.",
    "A single step begins the longest road; standard is the heart that endures.",
    "Still round the corner there may wait, a new road or a secret gate.",
    "Home is behind, the world ahead, and there are many paths to tread.",
    "I think at last I understand. There are some things that it is better to begin than to refuse.",
    "Let us keep our hearts high. The dawn is ever brighter than the night.",
    "Don't go where I can't follow!",
    "One Ring to rule them all, One Ring to find them, One Ring to bring them all, and in the darkness bind them.",
    "It's a dangerous business, Frodo, going out your door. You step onto the road, and if you don’t keep your feet, there’s no knowing where you might be swept off to."
)

@Composable
fun GoalCelebrationDialog(
    onDismissRequest: () -> Unit,
    quotes: List<String>
) {
    var currentQuote by remember { mutableStateOf(quotes.random()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("celebration_dismiss_button")
            ) {
                Text(
                    text = "Praise the Light",
                    color = ShireGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { currentQuote = quotes.random() },
                modifier = Modifier.testTag("celebration_randomize_button")
            ) {
                Text(
                    text = "Seek Another Prophecy",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DAILY MARCH COMPLETE!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.sp,
                    color = ShireGreen,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "You have met your daily pace goal, securing safe passage through Middle-earth for another day!",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentQuote,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp)
    )
}

