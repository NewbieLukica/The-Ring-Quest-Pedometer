package com.example.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.ui.screens.MiddleEarthMilestones
import java.text.DecimalFormat

class StepWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
        val todaySteps = prefs.getInt("today_steps", 0)
        val cumulativeSteps = prefs.getInt("cumulative_steps", 0)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, todaySteps, cumulativeSteps)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.action.UPDATE_WIDGET") {
            val prefs = context.getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
            val todaySteps = prefs.getInt("today_steps", 0)
            val cumulativeSteps = prefs.getInt("cumulative_steps", 0)
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, StepWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, todaySteps, cumulativeSteps)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        todaySteps: Int,
        cumulativeSteps: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.step_widget_layout)

        // 1. Calculate current and next milestones based on cumulativeSteps
        val currentMilestone = MiddleEarthMilestones.lastOrNull { cumulativeSteps >= it.stepsNeeded }
            ?: MiddleEarthMilestones.first()
        val nextMilestone = MiddleEarthMilestones.firstOrNull { it.stepsNeeded > cumulativeSteps }

        // 2. Format UI values
        val df = DecimalFormat("#,###")
        views.setTextViewText(R.id.widget_steps_count, "${df.format(todaySteps)} steps")
        views.setTextViewText(R.id.widget_current_location, "📍 ${currentMilestone.name}")
        views.setTextViewText(R.id.widget_region, currentMilestone.region)

        if (nextMilestone != null) {
            val stepsLeft = nextMilestone.stepsNeeded - cumulativeSteps
            
            val currentNeeded = currentMilestone.stepsNeeded
            val nextNeeded = nextMilestone.stepsNeeded
            val range = nextNeeded - currentNeeded
            val progressPercent = if (range > 0) {
                (((cumulativeSteps - currentNeeded).toFloat() / range.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            views.setTextViewText(R.id.widget_progress_text, "${df.format(stepsLeft)} steps to ${nextMilestone.name}")
            views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
        } else {
            // Mount doom reached! Complete victory
            views.setTextViewText(R.id.widget_progress_text, "🎉 Mount Doom reached! Quest Complete!")
            views.setProgressBar(R.id.widget_progress_bar, 100, 100, false)
        }

        // 3. Add Tap Intent to Launch App
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_steps_count, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_current_location, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

        // 4. Update widget instantiation
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        fun sendUpdateBroadcast(context: Context, todaySteps: Int, cumulativeSteps: Int) {
            // Write steps to shared preferences
            val prefs = context.getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("today_steps", todaySteps)
                .putInt("cumulative_steps", cumulativeSteps)
                .apply()

            // Trigger broadcast
            val intent = Intent(context, StepWidgetProvider::class.java).apply {
                action = "com.example.action.UPDATE_WIDGET"
            }
            context.sendBroadcast(intent)
        }
    }
}
