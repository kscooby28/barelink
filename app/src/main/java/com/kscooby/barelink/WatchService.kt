package com.kscooby.barelink

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/**
 * Stays alive so a clipboard change can open the invisible cleaner.
 * Android will not let this service read the clipboard itself.
 */
class WatchService : Service() {
    private var nextAllowed = 0L

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        if (!Prefs.auto(this)) return@OnPrimaryClipChangedListener
        val now = SystemClock.elapsedRealtime()
        if (now < Gate.suppressUntil) return@OnPrimaryClipChangedListener
        if (now < nextAllowed) return@OnPrimaryClipChangedListener
        nextAllowed = now + 700
        val open = Intent(this, CleanActivity::class.java).apply {
            action = CleanActivity.ACTION_CLEAN
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
            )
        }
        try {
            startActivity(open)
        } catch (_: Exception) {
            // Overlay permission missing, or the OEM blocked the background start.
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        promote()
        clipboard().addPrimaryClipChangedListener(listener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promote()
        return START_STICKY
    }

    override fun onDestroy() {
        clipboard().removePrimaryClipChangedListener(listener)
        super.onDestroy()
    }

    private fun clipboard() =
        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    private fun promote() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL,
                    "Link cleaner",
                    NotificationManager.IMPORTANCE_MIN,
                ).apply {
                    description = "On while Barelink is watching copied links."
                    setShowBadge(false)
                },
            )
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("Barelink")
            .setContentText("Copied links lose everything after ?")
            .setContentIntent(pending)
            .setOngoing(true)
            .setSilent(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTE_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL = "watch"
        private const val NOTE_ID = 1

        fun start(context: android.content.Context) {
            ContextCompat.startForegroundService(context, Intent(context, WatchService::class.java))
        }
    }
}
