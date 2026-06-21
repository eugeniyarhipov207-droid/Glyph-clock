package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Alarm
import com.example.ui.CityClock
import com.example.ui.ClockMode
import com.example.ui.ClockViewModel
import com.example.ui.components.DotMatrixText
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ClockScreen(
    viewModel: ClockViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val calendarInstance by viewModel.currentTime.collectAsStateWithLifecycle()
    val worldClocks by viewModel.worldClocks.collectAsStateWithLifecycle()
    val stopwatchLaps by viewModel.stopwatchLaps.collectAsStateWithLifecycle()

    var showAddAlarmDialog by remember { mutableStateOf(false) }
    var showCitySelectorDialog by remember { mutableStateOf(false) }

    // Visual flashes or background pulse when timer alerts
    val infiniteTransition = rememberInfiniteTransition(label = "Alert Flash")
    val alertAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alert Pulsate"
    )

    // Full screen timer alert overlay
    if (viewModel.isTimerFinishedAlert) {
        Dialog(onDismissRequest = { viewModel.dismissTimerAlert() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NothingBlack)
                    .border(2.dp, NothingRed.copy(alpha = alertAlpha), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Timer Alarm",
                        tint = NothingRed,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    DotMatrixText(
                        text = "TIME OUT",
                        activeColor = NothingRed,
                        dotSize = 6.dp,
                        spacing = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "YOUR TIMER HAS COMPLETED",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    Button(
                        onClick = { viewModel.dismissTimerAlert() },
                        colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dismiss_timer_button")
                    ) {
                        Text("DISMISS", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // Engineering blueprint retro matrix background dot-grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 16.dp.toPx()
            val dotColor = Color(0x1FBDC3C7) // Ultra faint gray/white dots
            val radius = 1.dp.toPx()
            
            var y = gridSpacing / 2
            while (y < size.height) {
                var x = gridSpacing / 2
                while (x < size.width) {
                    drawCircle(
                        color = dotColor,
                        radius = radius,
                        center = Offset(x, y)
                    )
                    x += gridSpacing
                }
                y += gridSpacing
            }
        }

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant top branding status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GLYPH CHRONO",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "NOTHING STYLE V1.0",
                        color = NothingLightGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.5.sp
                    )
                }
                
                // Red/White status glowing beacon (Nothing Style)
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(NothingRed.copy(alpha = 0.35f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NothingRed, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active screen container with slide animations
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = viewModel.currentMode,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width else -width } + fadeIn() with
                                slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width else width } + fadeOut()
                    },
                    label = "Tab Content Transitions"
                ) { mode ->
                    when (mode) {
                        ClockMode.CLOCK -> WorldClockView(
                            calendarInstance = calendarInstance,
                            worldClocks = worldClocks,
                            onManageCitiesClick = { showCitySelectorDialog = true }
                        )
                        ClockMode.ALARM -> AlarmView(
                            alarms = alarms,
                            onToggleAlarm = { viewModel.toggleAlarm(it) },
                            onDeleteAlarm = { viewModel.deleteAlarm(it) },
                            onAddAlarmClick = { showAddAlarmDialog = true }
                        )
                        ClockMode.STOPWATCH -> StopwatchView(
                            timeMs = viewModel.stopwatchTime,
                            isRunning = viewModel.isStopwatchRunning,
                            laps = stopwatchLaps,
                            onStart = { viewModel.startStopwatch() },
                            onPause = { viewModel.pauseStopwatch() },
                            onLap = { viewModel.lapStopwatch() },
                            onReset = { viewModel.resetStopwatch() }
                        )
                        ClockMode.TIMER -> TimerView(
                            totalDurationMs = viewModel.timerDurationMs,
                            remainingMs = viewModel.timerRemainingMs,
                            isRunning = viewModel.isTimerRunning,
                            onStart = { viewModel.startTimer() },
                            onPause = { viewModel.pauseTimer() },
                            onReset = { viewModel.resetTimer() },
                            onSetDuration = { mins, secs -> viewModel.setTimerDuration(mins, secs) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware inspired tactile bottom tab slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NothingMediumGray, RoundedCornerShape(12.dp))
                    .background(Color(0x800c0c0c), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClockMode.values().forEach { mode ->
                    val isSelected = viewModel.currentMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NothingWhite else Color.Transparent)
                            .clickable { viewModel.currentMode = mode }
                            .padding(vertical = 12.dp)
                            .testTag("tab_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name,
                            color = if (isSelected) NothingBlack else NothingLightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }

    // Add Alarm Dialog
    if (showAddAlarmDialog) {
        AddAlarmDialog(
            onDismiss = { showAddAlarmDialog = false },
            onSave = { hour, minute, label, repeatDays ->
                viewModel.addAlarm(hour, minute, label, repeatDays)
                showAddAlarmDialog = false
            }
        )
    }

    // World City Selector Dialog
    if (showCitySelectorDialog) {
        WorldCitySelectorDialog(
            cities = worldClocks,
            onToggleCity = { viewModel.toggleCityWatchlist(it.name) },
            onDismiss = { showCitySelectorDialog = false }
        )
    }
}

// --- CLOCK TAB ---
@Composable
fun WorldClockView(
    calendarInstance: Calendar,
    worldClocks: List<CityClock>,
    onManageCitiesClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.US) }
    val secFormatter = remember { SimpleDateFormat("ss", Locale.US) }
    val dateFormatter = remember { SimpleDateFormat("EEE, d MMM", Locale.US) }

    val formattedTime = formatter.format(calendarInstance.time)
    val formattedSecs = secFormatter.format(calendarInstance.time)
    val formattedDate = dateFormatter.format(calendarInstance.time).uppercase()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper section: Hero Local Clock Widget
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "LOCAL TIME",
                color = NothingLightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Clock Display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.height(60.dp)
            ) {
                DotMatrixText(
                    text = formattedTime,
                    activeColor = NothingWhite,
                    dotSize = 6.dp,
                    spacing = 2.5.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Scanning red blinking indicator seconds dots
                DotMatrixText(
                    text = formattedSecs,
                    activeColor = NothingRed,
                    dotSize = 3.dp,
                    spacing = 1.5.dp,
                    showBgGrid = false,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Badge
            Box(
                modifier = Modifier
                    .border(1.dp, NothingMediumGray, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = formattedDate,
                    color = NothingWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        // Custom sweeping analog track visualization
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .padding(8.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 12f
            
            // Draw background dial rim (faint)
            drawCircle(
                color = NothingMediumGray,
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw hour ticks in Nothing style (thin, dotted)
            for (angle in 0 until 360 step 30) {
                val angleRad = Math.toRadians(angle.toDouble())
                val startX = center.x + (radius - 6.dp.toPx()) * cos(angleRad).toFloat()
                val startY = center.y + (radius - 6.dp.toPx()) * sin(angleRad).toFloat()
                val endX = center.x + radius * cos(angleRad).toFloat()
                val endY = center.y + radius * sin(angleRad).toFloat()
                
                drawLine(
                    color = if (angle % 90 == 0) NothingWhite else NothingLightGray.copy(alpha = 0.5f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (angle % 90 == 0) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // Draw current second revolving sweeping dot (accented red)
            val secs = calendarInstance.get(Calendar.SECOND)
            val ms = calendarInstance.get(Calendar.MILLISECOND)
            val secondDegrees = (secs * 6f) + (ms * 0.006f) - 90f
            val secondRad = Math.toRadians(secondDegrees.toDouble())
            
            val dotX = center.x + radius * cos(secondRad).toFloat()
            val dotY = center.y + radius * sin(secondRad).toFloat()

            // Draw a subtle sweep glow
            drawCircle(
                color = NothingRed.copy(alpha = 0.2f),
                radius = 8.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            // Active second dial orb
            drawCircle(
                color = NothingRed,
                radius = 4.dp.toPx(),
                center = Offset(dotX, dotY)
            )

            // Minimal center core
            drawCircle(
                color = NothingWhite,
                radius = 2.dp.toPx(),
                center = center
            )
        }

        // Lower portion: Watchlist cities inside formatted rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORLD CLOCKS",
                    color = NothingLightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "[ EDIT WATCHLIST ]",
                    color = NothingWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable { onManageCitiesClick() }
                        .testTag("edit_watchlist_button")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val activeWatchlist = worldClocks.filter { it.isIncluded }
            if (activeWatchlist.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .border(1.dp, NothingMediumGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ADD CITIES TO YOUR WATCHLIST",
                        color = NothingLightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeWatchlist) { city ->
                        val cityCalendar = Calendar.getInstance(TimeZone.getTimeZone(city.timezoneId))
                        cityCalendar.timeInMillis = calendarInstance.timeInMillis
                        val cityHourFormatter = SimpleDateFormat("HH:mm", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone(city.timezoneId)
                        }
                        
                        // Compare hour differences
                        val localOffset = calendarInstance.timeZone.rawOffset
                        val cityOffset = TimeZone.getTimeZone(city.timezoneId).rawOffset
                        val diffHours = (cityOffset - localOffset) / (3600000)
                        val diffText = when {
                            diffHours > 0 -> "+${diffHours}H"
                            diffHours < 0 -> "${diffHours}H"
                            else -> "SAME"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NothingMediumGray, RoundedCornerShape(12.dp))
                                .background(NothingDarkGray)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = city.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = city.country,
                                        color = NothingLightGray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "$diffText RELATIVE TO LOCAL",
                                    color = NothingLightGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            DotMatrixText(
                                text = cityHourFormatter.format(cityCalendar.time),
                                activeColor = NothingWhite,
                                dotSize = 3.5.dp,
                                spacing = 1.5.dp,
                                showBgGrid = false
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- ALARM TAB ---
@Composable
fun AlarmView(
    alarms: List<Alarm>,
    onToggleAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (Alarm) -> Unit,
    onAddAlarmClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Section: Title and Add button
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ALARMS CONFIG",
                color = NothingLightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            IconButton(
                onClick = onAddAlarmClick,
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .size(36.dp)
                    .testTag("add_alarm_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alarm",
                    tint = NothingBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alarms List Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, NothingMediumGray, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "No Alarms",
                            tint = NothingLightGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO ALARMS CONFIGURED",
                            color = NothingLightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms) { alarm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NothingMediumGray, RoundedCornerShape(16.dp))
                                .background(NothingDarkGray)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    DotMatrixText(
                                        text = alarm.formattedTime,
                                        activeColor = if (alarm.isEnabled) NothingWhite else NothingLightGray,
                                        dotSize = 4.5.dp,
                                        spacing = 1.5.dp,
                                        showBgGrid = false
                                    )
                                    if (alarm.repeatDays.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(NothingMediumGray, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = alarm.repeatDays,
                                                color = NothingWhite,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = alarm.label.uppercase(),
                                    color = if (alarm.isEnabled) NothingLightGray else NothingLightGray.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Custom designed slider/switch
                                Switch(
                                    checked = alarm.isEnabled,
                                    onCheckedChange = { onToggleAlarm(alarm) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NothingWhite,
                                        checkedTrackColor = NothingRed,
                                        uncheckedThumbColor = NothingLightGray,
                                        uncheckedTrackColor = NothingMediumGray
                                    ),
                                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteAlarm(alarm) },
                                    modifier = Modifier.testTag("alarm_delete_${alarm.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Alarm",
                                        tint = NothingRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- STOPWATCH TAB ---
@Composable
fun StopwatchView(
    timeMs: Long,
    isRunning: Boolean,
    laps: List<Long>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onLap: () -> Unit,
    onReset: () -> Unit
) {
    // Math to compute formatted stopwatch strings
    val minutes = (timeMs / 60000) % 60
    val seconds = (timeMs / 1000) % 60
    val mills = (timeMs / 10) % 100

    val timeString = String.format("%02d:%02d.%02d", minutes, seconds, mills)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // High fidelity sweeping ring canvas
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 8f

                // Draw 60 dotted points representing seconds
                for (i in 0 until 60) {
                    val angle = i * 6f - 90f
                    val angleRad = Math.toRadians(angle.toDouble())
                    val dotX = center.x + radius * cos(angleRad).toFloat()
                    val dotY = center.y + radius * sin(angleRad).toFloat()

                    // Highlight dots up to current elapsed second
                    val isLit = i <= seconds && timeMs > 0
                    drawCircle(
                        color = if (isLit) NothingWhite else NothingMediumGray,
                        radius = if (isLit) 3.dp.toPx() else 1.5.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }

                // Rotating elegant sweep needle (Nothing Red)
                // Subsecond precision sweep
                val currentSecsWithSub = (timeMs % 60000) / 1000f
                val sweepDegrees = (currentSecsWithSub * 6f) - 90f
                val sweepRad = Math.toRadians(sweepDegrees.toDouble())

                val needleEndX = center.x + (radius - 12f) * cos(sweepRad).toFloat()
                val needleEndY = center.y + (radius - 12f) * sin(sweepRad).toFloat()

                // Glow point
                drawCircle(
                    color = NothingRed.copy(alpha = 0.2f),
                    radius = 12f,
                    center = Offset(needleEndX, needleEndY)
                )

                // Needle Line
                drawLine(
                    color = NothingRed,
                    start = center,
                    end = Offset(needleEndX, needleEndY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Hub circle
                drawCircle(
                    color = NothingWhite,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }
        }

        // Digital display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "STOPWATCH",
                color = NothingLightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            DotMatrixText(
                text = timeString,
                activeColor = if (isRunning) NothingWhite else NothingLightGray,
                dotSize = 5.dp,
                spacing = 2.5.dp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions grid row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Secondary button (LAP or RESET)
            Button(
                onClick = { if (isRunning) onLap() else onReset() },
                enabled = timeMs > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("stopwatch_sec_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NothingGray,
                    contentColor = NothingWhite,
                    disabledContainerColor = NothingDarkGray,
                    disabledContentColor = NothingLightGray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, NothingMediumGray)
            ) {
                Text(
                    text = if (isRunning) "LAP" else "RESET",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            // Primary control button (START or PAUSE)
            Button(
                onClick = { if (isRunning) onPause() else onStart() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("stopwatch_primary_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) NothingRed else NothingWhite,
                    contentColor = if (isRunning) NothingWhite else NothingBlack
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isRunning) "PAUSE" else "START",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Laps listings section
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
                .height(105.dp)
        ) {
            if (laps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, NothingMediumGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LAP SPLITS WILL SHOW HERE",
                        color = NothingLightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(laps.size) { index ->
                        val lapTime = laps[index]
                        val lapNum = laps.size - index
                        val lapMins = (lapTime / 60000) % 60
                        val lapSecs = (lapTime / 1000) % 60
                        val lapMills = (lapTime / 10) % 100
                        val lapFormatted = String.format("%02d:%02d.%02d", lapMins, lapSecs, lapMills)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NothingMediumGray, RoundedCornerShape(8.dp))
                                .background(NothingDarkGray)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LAP ${String.format("%02d", lapNum)}",
                                color = NothingLightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = lapFormatted,
                                color = NothingWhite,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- TIMER TAB ---
@Composable
fun TimerView(
    totalDurationMs: Long,
    remainingMs: Long,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSetDuration: (Int, Int) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(5) }
    var selectedSeconds by remember { mutableStateOf(0) }

    val totalSecsLeft = remainingMs / 1000
    val displayMins = totalSecsLeft / 60
    val displaySecs = totalSecsLeft % 60
    val displayMills = (remainingMs / 10) % 100
    val displayTime = String.format("%02d:%02d.%02d", displayMins, displaySecs, displayMills)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (!isRunning && remainingMs == totalDurationMs) {
            // TIMER UTILITY SETUP LAYOUT
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SET TIME OUT",
                    color = NothingLightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Fine precision digital set dial row (+ / - setup)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Minutes setup column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .border(1.dp, NothingMediumGray, RoundedCornerShape(12.dp))
                            .background(NothingDarkGray)
                            .padding(vertical = 12.dp)
                    ) {
                        IconButton(
                            onClick = { if (selectedMinutes < 99) selectedMinutes++ },
                            modifier = Modifier.size(32.dp).testTag("timer_min_up")
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "+Min", tint = NothingWhite)
                        }
                        
                        DotMatrixText(
                            text = String.format("%02d", selectedMinutes),
                            activeColor = NothingWhite,
                            dotSize = 4.dp,
                            spacing = 2.dp,
                            showBgGrid = false
                        )
                        Text("MINS", color = NothingLightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                        IconButton(
                            onClick = { if (selectedMinutes > 0) selectedMinutes-- },
                            modifier = Modifier.size(32.dp).testTag("timer_min_down")
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "-Min", tint = NothingWhite)
                        }
                    }

                    Text(
                        ":",
                        color = NothingWhite,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Seconds setup column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .border(1.dp, NothingMediumGray, RoundedCornerShape(12.dp))
                            .background(NothingDarkGray)
                            .padding(vertical = 12.dp)
                    ) {
                        IconButton(
                            onClick = { selectedSeconds = (selectedSeconds + 1) % 60 },
                            modifier = Modifier.size(32.dp).testTag("timer_sec_up")
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "+Sec", tint = NothingWhite)
                        }

                        DotMatrixText(
                            text = String.format("%02d", selectedSeconds),
                            activeColor = NothingWhite,
                            dotSize = 4.dp,
                            spacing = 2.dp,
                            showBgGrid = false
                        )
                        Text("SECS", color = NothingLightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                        IconButton(
                            onClick = { selectedSeconds = (selectedSeconds + 59) % 60 },
                            modifier = Modifier.size(32.dp).testTag("timer_sec_down")
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "-Sec", tint = NothingWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Built-in presets for fast action
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(1, 3, 5, 10)
                    presets.forEach { mins ->
                        val isPresetActive = selectedMinutes == mins && selectedSeconds == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isPresetActive) NothingWhite else NothingMediumGray, RoundedCornerShape(8.dp))
                                .background(if (isPresetActive) NothingWhite else NothingDarkGray)
                                .clickable {
                                    selectedMinutes = mins
                                    selectedSeconds = 0
                                    onSetDuration(mins, 0)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}MIN",
                                color = if (isPresetActive) NothingBlack else NothingLightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Apply and start trigger button
                Button(
                    onClick = {
                        onSetDuration(selectedMinutes, selectedSeconds)
                        onStart()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("timer_apply_and_start"),
                    colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("START COUNTDOWN", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
        } else {
            // TOTAL DYNAMIC RUNNING COUNTDOWN VIEW
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Circular Ring indicator mapped to remaining percentage
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - 8f

                        // Calculate percentage of remaining
                        val pct = remainingMs.toFloat() / totalDurationMs.toFloat()
                        val totalDots = 60
                        val litDotsLimit = (pct * totalDots).toInt()

                        for (i in 0 until totalDots) {
                            val angle = i * 6f - 90f
                            val angleRad = Math.toRadians(angle.toDouble())
                            val xPos = center.x + radius * cos(angleRad).toFloat()
                            val yPos = center.y + radius * sin(angleRad).toFloat()

                            val isLit = i <= litDotsLimit
                            drawCircle(
                                color = if (isLit) NothingRed else NothingMediumGray,
                                radius = if (isLit) 3.5.dp.toPx() else 1.5.dp.toPx(),
                                center = Offset(xPos, yPos)
                            )
                        }
                    }

                    // Inside central digital readout
                    DotMatrixText(
                        text = displayTime.take(5), // displays MM:SS
                        activeColor = NothingWhite,
                        charSpacing = 2.dp,
                        dotSize = 3.5.dp,
                        spacing = 1.5.dp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Digital milliseconds countdown label
                DotMatrixText(
                    text = displayTime,
                    activeColor = if (isRunning) NothingWhite else NothingLightGray,
                    dotSize = 4.5.dp,
                    spacing = 2.5.dp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Running timer action toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Secondary (Reset / Cancel)
                    Button(
                        onClick = { onReset() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("timer_btn_sec"),
                        colors = ButtonDefaults.buttonColors(containerColor = NothingGray, contentColor = NothingWhite),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NothingMediumGray)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    }

                    // Primary (Pause / Resume)
                    Button(
                        onClick = { if (isRunning) onPause() else onStart() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("timer_btn_primary"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) NothingRed else NothingWhite,
                            contentColor = if (isRunning) NothingWhite else NothingBlack
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isRunning) "PAUSE" else "RESUME",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

// --- DIALOGS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, label: String, repeatDays: String) -> Unit
) {
    var hour by remember { mutableStateOf(7) }
    var minute by remember { mutableStateOf(0) }
    var alarmLabel by remember { mutableStateOf("") }
    
    // Repeating days checkbox lists
    val daysList = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val selectedDays = remember { mutableStateMapOf<String, Boolean>() }
    daysList.forEach { day -> if (!selectedDays.containsKey(day)) selectedDays[day] = false }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NothingDarkGray),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, NothingMediumGray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SET NEW ALARM",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Time Setter Controllers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hours Column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { hour = (hour + 1) % 24 }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "+Hour", tint = NothingWhite)
                        }
                        DotMatrixText(
                            text = String.format("%02d", hour),
                            activeColor = NothingWhite,
                            dotSize = 5.dp,
                            spacing = 2.dp,
                            showBgGrid = false
                        )
                        IconButton(onClick = { hour = (hour + 23) % 24 }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "-Hour", tint = NothingWhite)
                        }
                    }

                    Text(
                        text = ":",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    // Minutes Column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { minute = (minute + 1) % 60 }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "+Min", tint = NothingWhite)
                        }
                        DotMatrixText(
                            text = String.format("%02d", minute),
                            activeColor = NothingWhite,
                            dotSize = 5.dp,
                            spacing = 2.dp,
                            showBgGrid = false
                        )
                        IconButton(onClick = { minute = (minute + 59) % 60 }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "-Min", tint = NothingWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Alarm Label field (Nothing style line edit)
                OutlinedTextField(
                    value = alarmLabel,
                    onValueChange = { alarmLabel = it },
                    placeholder = { Text("LABEL (EG: WORK, GYM)", color = NothingLightGray, fontSize = 11.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingWhite,
                        unfocusedBorderColor = NothingMediumGray,
                        cursorColor = NothingWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_label_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Repeating Chip selector grids
                Text(
                    text = "REPEAT DAYS",
                    color = NothingLightGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    daysList.forEach { day ->
                        val isSelected = selectedDays[day] == true
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, if (isSelected) NothingRed else NothingMediumGray, RoundedCornerShape(4.dp))
                                .background(if (isSelected) NothingRed else Color.Transparent)
                                .clickable { selectedDays[day] = !isSelected }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.take(2),
                                color = if (isSelected) NothingWhite else NothingLightGray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, NothingMediumGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NothingWhite),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", letterSpacing = 1.sp)
                    }

                    Button(
                        onClick = {
                            val repeatedString = selectedDays.entries
                                .filter { it.value }
                                .joinToString(separator = ",") { it.key }
                            onSave(
                                hour,
                                minute,
                                if (alarmLabel.isBlank()) "ALARM" else alarmLabel.trim(),
                                repeatedString
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_alarm_button")
                    ) {
                        Text("SAVE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WorldCitySelectorDialog(
    cities: List<CityClock>,
    onToggleCity: (CityClock) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NothingDarkGray),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, NothingMediumGray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHOOSE TIMEZONES",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = NothingWhite)
                    }
                }

                Divider(color = NothingMediumGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Grid of available clocks
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cities) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (city.isIncluded) NothingGray else Color.Transparent)
                                .clickable { onToggleCity(city) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${city.country} (${city.timezoneId})",
                                    color = NothingLightGray,
                                    fontSize = 8.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            
                            // Tick checker indicator dot-matrix styled
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(1.dp, if (city.isIncluded) NothingRed else NothingLightGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (city.isIncluded) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(NothingRed, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NothingWhite, contentColor = NothingBlack),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
        }
    }
}
