package com.example.educards2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CardNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cardId = intent.getLongExtra("card_id", -1L)
        val deckId = intent.getLongExtra("deck_id", -1L)
        val isBuiltIn = intent.getBooleanExtra("IS_BUILT_IN", false)
        val question = intent.getStringExtra("card_question") ?: return
        NotificationHelper(context).showNotification(
            cardId,
            deckId,
            isBuiltIn,
            "Пора повторить карточку",
            question
        )
    }
}