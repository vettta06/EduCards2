package com.example.educards2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    private val channelId = "card_notification_channel"

    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        NotificationChannel(
            channelId,
            "Card Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications about available cards"
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(this)
        }
    }

    fun showNotification(cardId: Long, deckId: Long, isBuiltIn: Boolean, title: String, message: String) {
        val intent = Intent(context, if (isBuiltIn) BuiltInCardsActivity::class.java else UserCardsActivity::class.java).apply {
            putExtra("CARD_ID", cardId)
            putExtra("DECK_ID", deckId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    cardId.hashCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setAutoCancel(true)
            .build()
            .let { notification ->
                context.getSystemService(NotificationManager::class.java)
                    ?.notify(cardId.hashCode(), notification)
            }
    }
}