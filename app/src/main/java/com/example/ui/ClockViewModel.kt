package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Alarm
import com.example.data.repository.AlarmRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class ClockMode {
    CLOCK, ALARM, STOPWATCH, TIMER
}

data class CityClock(
    val name: String,
    val country: String,
    val timezoneId: String,
    var isIncluded: Boolean = false
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AlarmRepository(database.alarmDao)
    }

    // Current Mode
    var currentMode by mutableStateOf(ClockMode.CLOCK)

    // Alarms List from Room
    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Time Flow (updated every 100ms for responsiveness)
    private val _currentTime = MutableStateFlow(Calendar.getInstance())
    val currentTime: StateFlow<Calendar> = _currentTime.asStateFlow()

    // World clocks list
    val availableCities = listOf(
        CityClock("LONDON", "UK", "Europe/London", true),
        CityClock("TOKYO", "JAPAN", "Asia/Tokyo", true),
        CityClock("NEW YORK", "USA", "America/New_York", true),
        CityClock("MOSCOW", "RUSSIA", "Europe/Moscow", false),
        CityClock("PARIS", "FRANCE", "Europe/Paris", false),
        CityClock("SYDNEY", "AUSTRALIA", "Australia/Sydney", false),
        CityClock("DUBAI", "UAE", "Asia/Dubai", false),
        CityClock("REYKJAVIK", "ICELAND", "Atlantic/Reykjavik", false)
    )

    private val _worldClocks = MutableStateFlow(availableCities)
    val worldClocks: StateFlow<List<CityClock>> = _worldClocks.asStateFlow()

    // Database Actions
    fun addAlarm(hour: Int, minute: Int, label: String, repeatDays: String) {
        viewModelScope.launch {
            repository.insertAlarm(
                Alarm(
                    hour = hour,
                    minute = minute,
                    label = label,
                    repeatDays = repeatDays,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun toggleCityWatchlist(cityName: String) {
        val updated = _worldClocks.value.map {
            if (it.name == cityName) it.copy(isIncluded = !it.isIncluded) else it
        }
        _worldClocks.value = updated
    }

    // --- STOPWATCH STATE ---
    var stopwatchTime by mutableStateOf(0L) // milliseconds
    var isStopwatchRunning by mutableStateOf(false)
    private val _stopwatchLaps = MutableStateFlow<List<Long>>(emptyList())
    val stopwatchLaps: StateFlow<List<Long>> = _stopwatchLaps.asStateFlow()

    private var stopwatchJob: Job? = null
    private var stopwatchStartRealTime = 0L
    private var stopwatchAccumulatedTime = 0L

    fun startStopwatch() {
        if (isStopwatchRunning) return
        isStopwatchRunning = true
        stopwatchStartRealTime = System.currentTimeMillis()
        stopwatchJob = viewModelScope.launch {
            while (isStopwatchRunning) {
                stopwatchTime = stopwatchAccumulatedTime + (System.currentTimeMillis() - stopwatchStartRealTime)
                delay(10) // tick every 10ms
            }
        }
    }

    fun pauseStopwatch() {
        if (!isStopwatchRunning) return
        isStopwatchRunning = false
        stopwatchAccumulatedTime += System.currentTimeMillis() - stopwatchStartRealTime
        stopwatchTime = stopwatchAccumulatedTime
        stopwatchJob?.cancel()
    }

    fun lapStopwatch() {
        _stopwatchLaps.value = listOf(stopwatchTime) + _stopwatchLaps.value
    }

    fun resetStopwatch() {
        pauseStopwatch()
        stopwatchAccumulatedTime = 0L
        stopwatchTime = 0L
        _stopwatchLaps.value = emptyList()
    }

    // --- TIMER STATE ---
    var timerDurationMs by mutableStateOf(5 * 60 * 1000L) // Default 5 mins
    var timerRemainingMs by mutableStateOf(5 * 60 * 1000L)
    var isTimerRunning by mutableStateOf(false)
    var isTimerFinishedAlert by mutableStateOf(false)

    private var timerJob: Job? = null
    private var timerStartRealTime = 0L
    private var timerAccumulatedMs = 0L

    fun startTimer() {
        if (isTimerRunning) return
        if (timerRemainingMs <= 0) {
            timerRemainingMs = timerDurationMs
        }
        isTimerRunning = true
        isTimerFinishedAlert = false
        timerStartRealTime = System.currentTimeMillis()
        timerAccumulatedMs = timerRemainingMs
        
        timerJob = viewModelScope.launch {
            while (isTimerRunning) {
                val elapsed = System.currentTimeMillis() - timerStartRealTime
                val remaining = timerAccumulatedMs - elapsed
                if (remaining <= 0) {
                    timerRemainingMs = 0L
                    isTimerRunning = false
                    isTimerFinishedAlert = true
                    break
                }
                timerRemainingMs = remaining
                delay(30)
            }
        }
    }

    fun pauseTimer() {
        if (!isTimerRunning) return
        isTimerRunning = false
        timerAccumulatedMs -= System.currentTimeMillis() - timerStartRealTime
        timerRemainingMs = maxOf(0L, timerAccumulatedMs)
        timerJob?.cancel()
    }

    fun resetTimer() {
        isTimerRunning = false
        timerJob?.cancel()
        timerRemainingMs = timerDurationMs
        isTimerFinishedAlert = false
    }

    fun setTimerDuration(minutes: Int, seconds: Int) {
        val totalMs = (minutes * 60 + seconds) * 1000L
        if (totalMs > 0) {
            timerDurationMs = totalMs
            timerRemainingMs = totalMs
            isTimerFinishedAlert = false
        }
    }

    fun dismissTimerAlert() {
        isTimerFinishedAlert = false
    }

    // Main Tick Loop
    init {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = Calendar.getInstance()
                delay(100) // keep local clocks perfectly in sync
            }
        }
    }
}
