package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

/**
 * Helper to dispatch real Android system notifications for OTP delivery.
 * Ensures users instantly receive their security codes as native heads-up notifications,
 * even when cellular SMS gateways or third-party email deliveries are delayed.
 */
object OtpNotificationHelper {
    private const val TAG = "OtpNotificationHelper"
    private const val CHANNEL_ID = "socialconnect_otp_channel"
    private const val CHANNEL_NAME = "Security & OTP Verification"
    private const val NOTIFICATION_ID = 90210

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Delivers one-time verification passcodes for SocialConnect"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showOtpNotification(context: Context, target: String, otpCode: String) {
        try {
            createNotificationChannel(context)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Copy OTP to clipboard automatically for convenient user experience
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("SocialConnect OTP", otpCode)
                clipboard?.setPrimaryClip(clip)
            } catch (e: Exception) {
                Log.w(TAG, "Could not auto-copy to clipboard: ${e.message}")
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_OTP_CODE", otpCode)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("🔑 Verification Code: $otpCode")
                .setContentText("Your SocialConnect login code for $target is $otpCode (Copied to clipboard).")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Your one-time login code for $target is **$otpCode**.\n\nCode copied to your clipboard. Enter or tap Auto-Fill to log in securely. Valid for 10 minutes.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 250, 150, 250))
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Successfully dispatched system notification for OTP: $otpCode to $target")
        } catch (e: Exception) {
            Log.e(TAG, "Error posting OTP notification", e)
        }
    }
}
