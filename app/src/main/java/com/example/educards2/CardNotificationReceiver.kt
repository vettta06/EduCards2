package com.example.educards2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CardNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val question = intent.getStringExtra("card_question") ?: return
        NotificationHelper(context).showNotification(
            "Пора повторить карточку",
            question
        )
    }
}