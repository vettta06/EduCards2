package com.example.educards2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CardNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cardId = intent.getLongExtra("card_id", -1L)
        val question = intent.getStringExtra("card_question") ?: run {
            return
        }
        NotificationHelper(context).showNotification(
            cardId,
            "Пора повторить карточку",
            question
        )
    }
}