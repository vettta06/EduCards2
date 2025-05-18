package com.example.educards2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.educards2.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                db.cardDao().getAllCardsSync().forEach { card ->
                    if (card.nextReview > System.currentTimeMillis()) {
                        card.scheduleNotification(context)
                    }
                }
            }
        }
    }
}