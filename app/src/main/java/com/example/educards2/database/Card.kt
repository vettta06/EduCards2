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
import com.example.educards2.UserCardsActivity


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
        val newInterval = when (q) {
            0 -> 15L * 60 * 1000
            1 -> 1L * 60 * 60 * 1000
            2 -> 2L * 60 * 60 * 1000
            3 -> 4L * 60 * 60 * 1000
            4 -> 12L * 60 * 60 * 1000
            5 -> 24L * 60 * 60 * 1000
            else -> currentInterval
        }

        currentInterval = newInterval
        nextReview = System.currentTimeMillis() + newInterval
        scheduleNotification(context)
    }

    fun scheduleNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }


         /*val intent = Intent(context, CardNotificationReceiver::class.java).apply {
            putExtra("card_id", id)
            putExtra("card_question", question)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )*/
        val intent = Intent(context, UserCardsActivity::class.java).apply {
            putExtra("TARGET_CARD_ID", id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextReview,
            pendingIntent
        )
    }
    fun isDue(): Boolean = System.currentTimeMillis() >= nextReview

}