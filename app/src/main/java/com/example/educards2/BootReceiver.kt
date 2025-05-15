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
                try {
                    val db = AppDatabase.getDatabase(context)
                    val cards = db.cardDao().getAllCardsSync()
                    cards.forEach { card ->
                        if (card.nextReviewDate > System.currentTimeMillis()) {
                            card.scheduleNotification(context)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}