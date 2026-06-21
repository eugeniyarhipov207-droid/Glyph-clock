package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import com.example.R
import java.text.SimpleDateFormat
import java.util.*

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_THEME) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentTheme = prefs.getInt(KEY_THEME, 0)
            val nextTheme = (currentTheme + 1) % 3
            prefs.edit().putInt(KEY_THEME, nextTheme).apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, ClockWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        private const val ACTION_TOGGLE_THEME = "com.example.ui.widget.TOGGLE_THEME"
        private const val PREFS_NAME = "com.example.ui.widget.ClockWidgetProvider"
        private const val KEY_THEME = "widget_theme"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val themeMode = prefs.getInt(KEY_THEME, 0)

            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
            val now = Date()

            views.setTextViewText(R.id.widget_time, timeFormat.format(now))
            views.setTextViewText(R.id.widget_date, dateFormat.format(now))

            when (themeMode) {
                0 -> { // Obsidian Monochromatic Dark
                    views.setTextColor(R.id.widget_time, Color.parseColor("#FFFFFF"))
                    views.setTextColor(R.id.widget_date, Color.parseColor("#8E8E93"))
                    views.setTextColor(R.id.widget_location, Color.parseColor("#8E8E93"))
                    views.setTextViewText(R.id.widget_theme_btn, "DARK")
                    views.setInt(R.id.widget_theme_btn, "setBackgroundColor", Color.parseColor("#1C1C1E"))
                    views.setInt(R.id.widget_theme_btn, "setTextColor", Color.parseColor("#FFFFFF"))
                }
                1 -> { // Pure Minimalist Light
                    views.setTextColor(R.id.widget_time, Color.parseColor("#000000"))
                    views.setTextColor(R.id.widget_date, Color.parseColor("#5F5F5F"))
                    views.setTextColor(R.id.widget_location, Color.parseColor("#5F5F5F"))
                    views.setTextViewText(R.id.widget_theme_btn, "LIGHT")
                    views.setInt(R.id.widget_theme_btn, "setBackgroundColor", Color.parseColor("#E5E5EA"))
                    views.setInt(R.id.widget_theme_btn, "setTextColor", Color.parseColor("#000000"))
                }
                2 -> { // Nothing Red Neon
                    views.setTextColor(R.id.widget_time, Color.parseColor("#FF2E2E"))
                    views.setTextColor(R.id.widget_date, Color.parseColor("#FFA3A3"))
                    views.setTextColor(R.id.widget_location, Color.parseColor("#FFA3A3"))
                    views.setTextViewText(R.id.widget_theme_btn, "NEON")
                    views.setInt(R.id.widget_theme_btn, "setBackgroundColor", Color.parseColor("#330A0A"))
                    views.setInt(R.id.widget_theme_btn, "setTextColor", Color.parseColor("#FF2E2E"))
                }
            }

            val intent = Intent(context, ClockWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_THEME
            }

            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, flag)
            views.setOnClickPendingIntent(R.id.widget_theme_btn, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
