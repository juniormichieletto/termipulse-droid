package com.juniormichieletto.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.juniormichieletto.MainActivity

class TerminalSessionService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): TerminalSessionService = this@TerminalSessionService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val activeSessionsCount = intent?.getIntExtra("ACTIVE_SESSIONS", 1) ?: 1
        val hasLongJob = intent?.getBooleanExtra("HAS_LONG_JOB", false) ?: false

        val notification = buildNotification(activeSessionsCount, hasLongJob)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun buildNotification(sessionsCount: Int, hasLongJob: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (hasLongJob) {
            "⚡ Active Job Running | $sessionsCount Open Tab(s)"
        } else {
            "🟢 $sessionsCount Active SSH Tab(s) Connected"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TermiPulse SSH Terminal")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun updateNotificationState(activeCount: Int, hasLongJob: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(activeCount, hasLongJob))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TermiPulse Active SSH Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps SSH terminal connections and background long jobs active"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "termipulse_ssh_service"
        const val NOTIFICATION_ID = 8801

        fun startService(context: Context, activeSessions: Int, hasLongJob: Boolean) {
            try {
                val intent = Intent(context, TerminalSessionService::class.java).apply {
                    putExtra("ACTIVE_SESSIONS", activeSessions)
                    putExtra("HAS_LONG_JOB", hasLongJob)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, TerminalSessionService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
