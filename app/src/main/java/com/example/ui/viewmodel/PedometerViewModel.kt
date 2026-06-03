package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.StepLog
import com.example.data.repository.StepRepository
import com.example.receiver.StepWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PedometerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StepRepository
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateStr: String = sdf.format(Date())

    // UI state states
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _isSensorActive = MutableStateFlow(false)
    val isSensorActive: StateFlow<Boolean> = _isSensorActive.asStateFlow()

    private val _statusMessage = MutableStateFlow("Pedometer ready")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StepRepository(database.stepLogDao())

        // Ensure database today's record exists
        viewModelScope.launch {
            val todayLog = repository.getStepLogByDateSync(todayDateStr)
            if (todayLog == null) {
                repository.insertStepLog(StepLog(todayDateStr, 0, 8000))
            }
        }
    }

    // Reactive streams
    val todayLogFlow: Flow<StepLog> = repository.getStepLogByDate(todayDateStr)
        .map { it ?: StepLog(todayDateStr, 0, 8000) }

    val allHistoricalLogsFlow: Flow<List<StepLog>> = repository.allStepLogs

    suspend fun getTodayStepsDirectly(): Int {
        return repository.getStepLogByDateSync(todayDateStr)?.steps ?: 0
    }

    // Simulates taking steps manually
    fun simulateSteps(count: Int) {
        viewModelScope.launch {
            val currentRecord = repository.getStepLogByDateSync(todayDateStr) ?: StepLog(todayDateStr, 0, 8000)
            val newSteps = (currentRecord.steps + count).coerceAtLeast(0)
            repository.updateSteps(todayDateStr, newSteps)
            
            // Sync with SharedPreferences for cumulative total consistency
            val prefs = getApplication<Application>().getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
            val currentCumulative = prefs.getInt("cumulative_steps", 0)
            val currentToday = prefs.getInt("today_steps", 0)
            
            val newCumulative = currentCumulative + count
            val newToday = (currentToday + count).coerceAtLeast(0)
            
            prefs.edit()
                .putInt("cumulative_steps", newCumulative)
                .putInt("today_steps", newToday)
                .apply()
                
            StepWidgetProvider.sendUpdateBroadcast(getApplication(), newToday, newCumulative)
            _statusMessage.value = "Simulated walk: +$count steps!"
        }
    }

    // Updates the daily targets
    fun updateDailyGoal(newGoal: Int) {
        viewModelScope.launch {
            val currentRecord = repository.getStepLogByDateSync(todayDateStr) ?: StepLog(todayDateStr, 0, 8000)
            repository.insertStepLog(currentRecord.copy(goal = newGoal))
            _statusMessage.value = "Daily goal updated to $newGoal steps."
        }
    }

    // Reset today's steps count
    fun resetTodaySteps() {
        viewModelScope.launch {
            val currentRecord = repository.getStepLogByDateSync(todayDateStr) ?: StepLog(todayDateStr, 0, 8000)
            repository.updateSteps(todayDateStr, 0)
            
            val prefs = getApplication<Application>().getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
            val lastSensor = prefs.getInt("last_sensor_value", 0)
            val currentCumulative = prefs.getInt("cumulative_steps", 0)
            val currentToday = prefs.getInt("today_steps", 0)
            
            // Reset today and baseline in prefs
            prefs.edit()
                .putInt("daily_baseline", lastSensor)
                .putInt("today_steps", 0)
                .putInt("cumulative_steps", (currentCumulative - currentToday).coerceAtLeast(0))
                .apply()

            val updatedCumulative = prefs.getInt("cumulative_steps", 0)
            StepWidgetProvider.sendUpdateBroadcast(getApplication(), 0, updatedCumulative)
            _statusMessage.value = "Today's steps reset to 0."
        }
    }

    // Erase all logs
    fun clearAllHistory() {
        viewModelScope.launch {
            // Delete all entries
            val all = repository.allStepLogs.first()
            for (item in all) {
                repository.deleteStepLog(item.date)
            }
            // Re-insert empty today
            repository.insertStepLog(StepLog(todayDateStr, 0, 8000))
            
            val prefs = getApplication<Application>().getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("cumulative_steps", 0)
                .putInt("today_steps", 0)
                .putInt("last_sensor_value", -1)
                .putInt("daily_baseline", -1)
                .apply()

            StepWidgetProvider.sendUpdateBroadcast(getApplication(), 0, 0)
            _statusMessage.value = "Database cleared! Today's metrics reset."
        }
    }

    // Continuous walking simulation job
    private var simulationJob: Job? = null

    fun toggleContinuousSimulation() {
        if (_isSimulating.value) {
            simulationJob?.cancel()
            _isSimulating.value = false
            _statusMessage.value = "Continuous walking simulation stopped."
        } else {
            _isSimulating.value = true
            _statusMessage.value = "Continuous walking simulation active..."
            simulationJob = viewModelScope.launch {
                while (_isSimulating.value) {
                    val delayMs = (1500..3000).random().toLong()
                    delay(delayMs)
                    val stepsAdded = (1..2).random()
                    simulateSteps(stepsAdded)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
