package com.example.educards2.Stats

import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar

class StreakManager(private val context: Context) {
    private val prefs = context.getSharedPreferences(
        "streak_prefs",
        Context.MODE_PRIVATE
    )
    fun getSharedPreferences(): SharedPreferences {
        return prefs
    }

    fun getCurrentStreak(): Int {
        return prefs.getInt("current_streak", 0)
    }

    fun updateStreak() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis

        val lastDate = prefs.getLong("last_study_date", 0)
        val currentStreak = prefs.getInt("current_streak", 0)

        if (isSameDay(today, lastDate)) return

        val newStreak = if (isConsecutiveDay(today, lastDate)) {
            currentStreak + 1
        } else {
            1
        }
        if (newStreak > prefs.getInt("record_streak", 0)) {
            prefs.edit().putInt("record_streak", newStreak).apply()
        }

        prefs.edit()
            .putInt("current_streak", newStreak)
            .putLong("last_study_date", today)
            .apply()
    }
    private fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    private fun isConsecutiveDay(newDate: Long, lastDate: Long): Boolean {
        if (lastDate == 0L) return false
        val cal = Calendar.getInstance().apply { timeInMillis = lastDate }
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return isSameDay(newDate, cal.timeInMillis)
    }
    fun checkStreak() {
        val lastDate = prefs.getLong("last_study_date", 0)
        val currentStreak = prefs.getInt("current_streak", 0)
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        if (!isConsecutiveDay(today, lastDate) && !isSameDay(today, lastDate)) {
            prefs.edit()
                .putInt("current_streak", 0)
                .apply()
        }
    }
}