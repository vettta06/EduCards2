package com.example.educards2.Stats

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class StreakCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val streakManager = StreakManager(applicationContext)
        streakManager.checkStreak()
        return Result.success()
    }
}

