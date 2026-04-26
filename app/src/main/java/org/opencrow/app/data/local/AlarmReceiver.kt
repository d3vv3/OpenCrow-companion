package org.opencrow.app.data.local

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import org.opencrow.app.R

/**
 * Fires when an alarm set by the AI expires.
 * Displays a high-priority, full-screen-capable notification that rings and vibrates.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_LABEL = "alarm_label"
        private const val CHANNEL_ID = "opencrow_alarms"
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra(EXTRA_LABEL) ?: "Alarm"
        Log.d(TAG, "onReceive() fired! label=$label intent=$intent")
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ $label")
            .setContentText("Your alarm is ringing")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        nm.notify(label.hashCode(), notification)
        Log.d(TAG, "onReceive() notification posted for label=$label notifId=${label.hashCode()}")
    }
}
