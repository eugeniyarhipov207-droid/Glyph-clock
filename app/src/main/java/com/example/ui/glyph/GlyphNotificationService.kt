package com.example.ui.glyph

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.db.AppDatabase
import kotlinx.coroutines.*
import java.util.*

class GlyphNotificationService : Service() {

    private var glyphHelper: GlyphHelper? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isMonitoringAlarms = false

    override fun onCreate() {
        super.onCreate()
        glyphHelper = GlyphHelper(this)
        startForegroundServiceNotification()
        monitorAlarmsForGlyphTriggers()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "glyph_channel"
        val channelName = "Nothing Glyph Alarm Service"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Glyph Alarm Monitor Active")
            .setContentText("Listening for configured alarms to animate LEDs...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1337, notification)
    }

    private fun monitorAlarmsForGlyphTriggers() {
        if (isMonitoringAlarms) return
        isMonitoringAlarms = true

        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                db.alarmDao.getAllAlarms().collect { alarms ->
                    val calendar = Calendar.getInstance()
                    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = calendar.get(Calendar.MINUTE)

                    val activeMatchingAlarm = alarms.find { alarm ->
                        alarm.isEnabled && alarm.hour == currentHour && alarm.minute == currentMinute
                    }

                    if (activeMatchingAlarm != null) {
                        Log.d("GlyphService", "Active alarm triggered: ${activeMatchingAlarm.label}. Blinking Glyphs!")
                        glyphHelper?.triggerAlarmPattern()
                    }
                }
            } catch (e: Exception) {
                Log.e("GlyphService", "Exception in Alarm monitoring: ${e.message}")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_TRIGGER_GLYPH) {
            glyphHelper?.triggerAlarmPattern()
        } else if (action == ACTION_STOP_GLYPH) {
            glyphHelper?.stopAndRelease()
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        glyphHelper?.stopAndRelease()
        Log.d("GlyphService", "GlyphNotificationService destroyed safely.")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_TRIGGER_GLYPH = "com.example.action.TRIGGER_GLYPH"
        const val ACTION_STOP_GLYPH = "com.example.action.STOP_GLYPH"

        fun startService(context: Context) {
            val intent = Intent(context, GlyphNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, GlyphNotificationService::class.java)
            context.stopService(intent)
        }
    }
}
