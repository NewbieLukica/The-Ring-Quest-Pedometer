package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.StepLog
import com.example.data.repository.StepRepository
import com.example.receiver.StepWidgetProvider
import com.example.ui.screens.MiddleEarthMilestones
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class StepTrackerService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var repository: StepRepository? = null
    private val CHANNEL_ID = "step_tracker_channel"
    private val NOTIFICATION_ID = 2026

    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null

    private val TAG = "StepTrackerService"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val database = AppDatabase.getDatabase(applicationContext)
        repository = StepRepository(database.stepLogDao())

        setupSensors()

        // Start collecting step logs reactively from database
        serviceScope.launch {
            repository?.allStepLogs?.collect { logs ->
                val todayDateStr = getTodayDateString()
                val todaySteps = logs.find { it.date == todayDateStr }?.steps ?: 0
                val cumulativeSteps = logs.sumOf { it.steps }

                // Update the real-time persistent notification
                showOrUpdateNotification(todaySteps, cumulativeSteps)
            }
        }
    }

    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepCounterSensor != null) {
            sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d(TAG, "Registered TYPE_STEP_COUNTER")
        } else if (stepDetectorSensor != null) {
            sensorManager?.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d(TAG, "Registered TYPE_STEP_DETECTOR as fallback")
        } else {
            Log.e(TAG, "No step sensors available on this device")
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            handleStepCounterEvent(event.values[0].toInt())
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            handleStepDetectorEvent()
        }
    }

    private fun handleStepCounterEvent(currentSensorValue: Int) {
        val prefs = getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
        val todayDateStr = getTodayDateString()
        
        val baselineDate = prefs.getString("baseline_date", "")
        var dailyBaseline = prefs.getInt("daily_baseline", -1)
        var lastSensorValue = prefs.getInt("last_sensor_value", -1)
        var cumulativeSteps = prefs.getInt("cumulative_steps", 0)

        Log.d(TAG, "Sensor Event: current=$currentSensorValue, last=$lastSensorValue, baseline=$dailyBaseline, date=$todayDateStr")

        // 1. Reboot Detection
        if (lastSensorValue != -1 && currentSensorValue < lastSensorValue) {
            Log.i(TAG, "Device reboot detected. Resetting daily baseline.")
            dailyBaseline = currentSensorValue
            lastSensorValue = currentSensorValue
            prefs.edit()
                .putInt("daily_baseline", dailyBaseline)
                .putInt("last_sensor_value", lastSensorValue)
                .putString("baseline_date", todayDateStr)
                .apply()
            return // Skip delta calculation for this specific reboot event
        }

        // 2. New Day Detection
        if (baselineDate != todayDateStr || dailyBaseline == -1) {
            Log.i(TAG, "New day or first initialization detected ($todayDateStr). Setting baseline.")
            dailyBaseline = currentSensorValue
            prefs.edit()
                .putInt("daily_baseline", dailyBaseline)
                .putString("baseline_date", todayDateStr)
                .apply()
        }

        // 3. Safe Delta Calculation
        var delta = 0
        if (lastSensorValue != -1) {
            delta = currentSensorValue - lastSensorValue
            if (delta < 0) delta = 0
            if (delta > 1000) {
                Log.w(TAG, "Suspiciously large delta ignored: $delta")
                delta = 0
            }
        }

        // 4. Update Steps
        val todaySteps = (currentSensorValue - dailyBaseline).coerceAtLeast(0)
        cumulativeSteps += delta

        Log.d(TAG, "Calculated: delta=$delta, todaySteps=$todaySteps, cumulativeSteps=$cumulativeSteps")

        // 5. Persist
        prefs.edit()
            .putInt("last_sensor_value", currentSensorValue)
            .putInt("cumulative_steps", cumulativeSteps)
            .putInt("today_steps", todaySteps)
            .apply()

        // 6. Database Update
        serviceScope.launch {
            val repository = repository ?: return@launch
            val existing = repository.getStepLogByDateSync(todayDateStr)
            if (existing != null) {
                repository.updateSteps(todayDateStr, todaySteps)
            } else {
                repository.insertStepLog(StepLog(todayDateStr, todaySteps, 8000))
            }
        }

        // 7. Widget Update
        StepWidgetProvider.sendUpdateBroadcast(applicationContext, todaySteps, cumulativeSteps)
    }

    private fun handleStepDetectorEvent() {
        val prefs = getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
        val todayDateStr = getTodayDateString()
        
        val baselineDate = prefs.getString("baseline_date", "")
        var todaySteps = prefs.getInt("today_steps", 0)
        var cumulativeSteps = prefs.getInt("cumulative_steps", 0)

        // New Day Check for detector
        if (baselineDate != todayDateStr) {
            todaySteps = 0
            prefs.edit().putString("baseline_date", todayDateStr).apply()
        }

        todaySteps += 1
        cumulativeSteps += 1

        Log.d(TAG, "Detector Event: todaySteps=$todaySteps, cumulativeSteps=$cumulativeSteps")

        prefs.edit()
            .putInt("today_steps", todaySteps)
            .putInt("cumulative_steps", cumulativeSteps)
            .apply()

        serviceScope.launch {
            val repository = repository ?: return@launch
            val existing = repository.getStepLogByDateSync(todayDateStr)
            if (existing != null) {
                repository.updateSteps(todayDateStr, todaySteps)
            } else {
                repository.insertStepLog(StepLog(todayDateStr, todaySteps, 8000))
            }
        }

        StepWidgetProvider.sendUpdateBroadcast(applicationContext, todaySteps, cumulativeSteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
        val todaySteps = prefs.getInt("today_steps", 0)
        val cumulativeSteps = prefs.getInt("cumulative_steps", 0)
        val initialNotification = buildNotification(todaySteps, cumulativeSteps)
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, initialNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun showOrUpdateNotification(todaySteps: Int, cumulativeSteps: Int) {
        val notification = buildNotification(todaySteps, cumulativeSteps)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(todaySteps: Int, cumulativeSteps: Int): Notification {
        val df = DecimalFormat("#,###")
        val currentMilestone = MiddleEarthMilestones.lastOrNull { cumulativeSteps >= it.stepsNeeded }
            ?: MiddleEarthMilestones.first()
        val nextMilestone = MiddleEarthMilestones.firstOrNull { it.stepsNeeded > cumulativeSteps }

        val contentTitle = "💍 Daily Paces: ${df.format(todaySteps)} steps"
        val contentText: String
        val subText: String

        if (nextMilestone != null) {
            val stepsLeft = nextMilestone.stepsNeeded - cumulativeSteps
            contentText = "📍 Currently at: ${currentMilestone.name}"
            subText = "${df.format(stepsLeft)} steps to ${nextMilestone.name}"
        } else {
            contentText = "🎉 Mount Doom Reached!"
            subText = "The One Ring is cast into the fire!"
        }

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSubText(subText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setOnlyAlertOnce(true) // Crucial: prevents sound/vibration on every update
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Middle-Earth Ring Quest Tracker", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Shows real-time cumulative steps and distance to the next landmark."
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null) // No sound for this channel
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
