package com.example.educards2.database

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.educards2.CardNotificationReceiver

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val deckId: Long,
    val question: String,
    val answer: String,

    @ColumnInfo(name = "nextReview")
    var nextReview: Long = System.currentTimeMillis(),
    var rating: Int = 0,
    var eFactor: Double = 2.5,
    var currentInterval: Long = 0,
    val isBuiltIn: Boolean = false,
    var isArchived: Boolean = false
) {

    fun updateEFactor(q: Int) {
        val delta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)
        eFactor = (eFactor + delta).coerceIn(1.3, 2.5)
    }

    fun updateIntervals(q: Int, context: Context) {
        val ONE_MONTH = 30L * 24 * 60 * 60 * 1000

        currentInterval = when {
            currentInterval == 0L -> when (q) {
                0 -> 15L * 60 * 1000       // 15 минут
                1 -> 1L * 60 * 60 * 1000   // 1 час
                2 -> 4L * 60 * 60 * 1000   // 4 часа
                3 -> 8L * 60 * 60 * 1000   // 8 часов
                4 -> 16L * 60 * 60 * 1000  // 16 часов
                5 -> 24L * 60 * 60 * 1000  // 24 часа
                else -> 24L * 60 * 60 * 1000
            }

            else -> when (q) {
                0 -> 15L * 60 * 1000
                1 -> (currentInterval * 0.5).toLong()
                2 -> currentInterval
                3 -> (currentInterval * 1.2 * eFactor).toLong()
                4 -> (currentInterval * 1.5 * eFactor).toLong()
                5 -> (currentInterval * 2.0 * eFactor).toLong()
                else -> currentInterval
            }.coerceAtMost(ONE_MONTH)
        }

        nextReview = System.currentTimeMillis() + currentInterval
        scheduleNotification(context)
    }
    fun scheduleNotification(context: Context) {
        if (nextReview <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CardNotificationReceiver::class.java).apply {
            putExtra("card_id", id)
            putExtra("deck_id", deckId)
            putExtra("card_question", question)
            putExtra("IS_BUILT_IN", isBuiltIn)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            flags
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextReview,
                pendingIntent
            )
        } catch (_: Exception) {}
    }
    fun isDue(serverTimeMillis: Long): Boolean {
        return nextReview <= serverTimeMillis
    }
    fun isDue(): Boolean {
        return nextReview <= System.currentTimeMillis()
    }

}