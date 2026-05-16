/*
 * EduLinkFirebaseMessagingService.kt
 *
 * Version: 1.0
 * Date: 2026-05-11
 *
 */

package com.vectorpeaks.edulink.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vectorpeaks.edulink.MainActivity
import com.vectorpeaks.edulink.R
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import com.vectorpeaks.edulink.security.AuthPreferencesManager

/**
 * Handles incoming FCM push notifications and token refresh events.
 *
 * Responsibilities:
 * - Sending an updated FCM token to the backend whenever it is refreshed by Firebase.
 * - Displaying a system notification when a chat message arrives while the app
 *   is in the foreground (background notifications are shown automatically by the OS).
 *
 * Must be registered in AndroidManifest.xml inside the <application> block:
 * ```xml
 * <service
 *     android:name=".service.EduLinkFirebaseMessagingService"
 *     android:exported="false">
 *     <intent-filter>
 *         <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *     </intent-filter>
 * </service>
 * ```
 */
class EduLinkFirebaseMessagingService : FirebaseMessagingService() {

    companion object {

        private const val CHANNEL_ID = "edulink_chat"
        private const val CHANNEL_NAME = "Chat Messages"
    }

    // -----------------------------------------------------------------------
    // Token refresh
    // -----------------------------------------------------------------------

    /**
     * Called by Firebase when a new FCM registration token is generated.
     * This happens on first app launch and whenever Firebase rotates the token.
     *
     * Reads the logged-in user's ID from SharedPreferences and sends the new
     * token to the backend. If no user is logged in yet the token will be sent
     * the next time the user logs in (see LoginViewModel).
     *
     * @param token the new FCM registration token for this device
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM token refreshed: $token")

        val authPrefs = AuthPreferencesManager(applicationContext)
        val userId = authPrefs.getUserId()

        if (userId == -1) {
            Timber.d("No logged-in user, skipping token upload")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.apiService.updateFcmToken(
                    userId,
                    mapOf("fcmToken" to token)
                )
                Timber.d("FCM token updated on backend for user $userId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to update FCM token on backend - the token will be re-sent on the next login")
            }
        }
    }

    // -----------------------------------------------------------------------
    // Foreground notifications
    // -----------------------------------------------------------------------

    /**
     * Called when a notification arrives while the app is in the foreground.
     * The OS handles notifications automatically when the app is in the background
     * or killed, so manual handling is only needed here.
     *
     * @param message the incoming FCM message containing notification and data payloads
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: ${message.notification?.title}")

        val title = message.notification?.title ?: return
        val body  = message.notification?.body  ?: return

        showNotification(title, body)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Builds and displays a system notification for an incoming chat message.
     * Creates the notification channel on first call (required on Android 8+).
     * Tapping the notification opens [MainActivity].
     *
     * @param title the notification title (sender's name)
     * @param body  the notification body (message preview)
     */
    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel if it does not exist yet (Android 8+)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Tapping the notification opens the app at MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}