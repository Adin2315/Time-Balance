package com.adindaapriliawahyupp_231111015.timebalance.notification

data class NotificationSettings(
    val isEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val ringtoneType: String = "default",
    val customRingtoneUri: String = "",
    val volume: Int = 50 // 0-100
)