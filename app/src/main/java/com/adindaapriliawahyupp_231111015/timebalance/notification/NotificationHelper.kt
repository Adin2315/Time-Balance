package com.adindaapriliawahyupp_231111015.timebalance.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.adindaapriliawahyupp_231111015.timebalance.R
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter

object NotificationHelper {

    private const val CHANNEL_ID = "time_balance_channel"
    private const val CHANNEL_NAME = "Time Balance Notifications"
    private const val DESCRIPTION_TEXT = "Notifications for your scheduled events"
    private val VIBRATION_PATTERN = longArrayOf(0, 500, 200, 500) // Immediate, vibrate 500ms, pause 200ms, vibrate 500ms

    // Create notification channel with vibration and sound settings
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Delete existing channel to update settings
            notificationManager.deleteNotificationChannel(CHANNEL_ID)

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = DESCRIPTION_TEXT
                enableLights(true)
                lightColor = Color.BLUE
            }

            val settings = getNotificationSettings(context)

            // Configure vibration
            if (settings.isVibrationEnabled) {
                channel.enableVibration(true)
                channel.vibrationPattern = VIBRATION_PATTERN
            } else {
                channel.enableVibration(false)
            }

            // Configure sound
            if (settings.isSoundEnabled) {
                val soundUri = getSoundUriForSettings(settings)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                channel.setSound(soundUri, audioAttributes)
            } else {
                channel.setSound(null, null)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    // Schedule a notification with alarm manager
    fun scheduleNotification(
        context: Context,
        title: String,
        message: String,
        triggerTimeMillis: Long,
        notificationId: Int
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notification_id", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                Toast.makeText(
                    context,
                    "Please enable exact alarms in app settings",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    // Cancel a scheduled notification
    fun cancelNotification(context: Context, notificationId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    // Show notification immediately
    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        createNotificationChannel(context)
        val settings = getNotificationSettings(context)

        if (!settings.isEnabled) {
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Configure vibration
        if (settings.isVibrationEnabled && hasVibrationCapability(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android 8.0+, vibration is controlled by channel settings
            } else {
                // For older versions, set vibration directly
                builder.setVibrate(VIBRATION_PATTERN)
            }
        } else {
            builder.setVibrate(null)
        }

        // Configure sound
        if (settings.isSoundEnabled) {
            builder.setSound(getSoundUriForSettings(settings))
        } else {
            builder.setSound(null)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    // Trigger vibration immediately (for testing)
    fun triggerInstantVibration(context: Context) {
        if (!hasVibrationCapability(context)) {
            Toast.makeText(context, "Vibration not available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        VIBRATION_PATTERN,
                        -1 // Don't repeat
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_PATTERN, -1)
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Vibration failed", e)
            Toast.makeText(context, "Vibration failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Check if device has vibration capability
    fun hasVibrationCapability(context: Context): Boolean {
        return try {
            getVibrator(context).hasVibrator()
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Vibration check failed", e)
            false
        }
    }

    // Update notification settings
    fun updateSettings(context: Context, settings: NotificationSettings) {
        createNotificationChannel(context) // Recreate channel with new settings
    }

    // Helper function to get vibrator service
    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Helper function to get sound URI based on settings
    private fun getSoundUriForSettings(settings: NotificationSettings): Uri {
        return when (settings.ringtoneType) {
            "default" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            "notification" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "alarm" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            "custom" -> if (settings.customRingtoneUri.isNotEmpty()) {
                Uri.parse(settings.customRingtoneUri)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    // Helper function to get notification settings from DB
    private fun getNotificationSettings(context: Context): NotificationSettings {
        val dbAdapter = DBAdapter(context)
        dbAdapter.open()
        val settings = dbAdapter.getNotificationSettings()
        dbAdapter.close()
        return settings
    }
}