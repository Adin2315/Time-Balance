package com.adindaapriliawahyupp_231111015.timebalance.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Time Balance Reminder"
        val message = intent.getStringExtra("message") ?: "Your schedule is due!"
        val notificationId = intent.getIntExtra("notification_id", 0)

        NotificationHelper.showNotification(context, title, message, notificationId)
    }
}