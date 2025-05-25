package com.example.educards2.Stats

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Date

class StreakCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        Log.d("StreakDebug", "Запуск проверки стрика в ${Date()}")

        val streakManager = StreakManager(applicationContext)
        streakManager.checkStreak()
        Log.d("StreakDebug", "Текущий стрик: ${streakManager.getCurrentStreak()}")

        return Result.success()
    }
}

