package com.example.educards2

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.example.educards2.Stats.StreakManager
import com.example.educards2.database.Achievement
import com.example.educards2.database.Card
import com.example.educards2.database.CardDao

class AchievementManager(private val context: Context, private val cardDao: CardDao, private val streakManager: StreakManager) {
    private val prefs: SharedPreferences = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)

    val allAchievements = listOf(
        Achievement(
            "Новичок",
            "Создана первая карточка",
            R.drawable.ic_achievement_locked,
            false
        ),
        Achievement(
            "Серийный ученик",
            "5 дней подряд",
            R.drawable.ic_achievement_locked,
            false
        ),
        Achievement(
            "Карточный магнат",
            "Создано 50 карт",
            R.drawable.ic_achievement_locked,
            false
        ),
        Achievement(
            "Усердный ученик",
            "Оценено 20 карт",
            R.drawable.ic_achievement_locked,
            false
        ),
        Achievement(
            "Без ошибок",
            "10 верных ответов подряд",
            R.drawable.ic_achievement_locked,
            false
        ),
        Achievement(
            "Разнообразие в оценках",
            "Использованы все оценки",
            R.drawable.ic_achievement_locked,
            false
        ),Achievement(
            "Мастер повторений",
            "Повторение без ошибок",
            R.drawable.ic_achievement_locked,
            false
        ),Achievement(
            "100 карточек",
            "Оценено 100 карточек",
            R.drawable.ic_achievement_locked,
            false
         ),Achievement(
            "Легенда обучения",
            "Получены все ачивки",
            R.drawable.ic_achievement_locked,
            false
        )
    )
    suspend fun checkAllAchievements() {
        val allCards = cardDao.getAllCardsSync()
        val userCards = allCards.filter { !it.isBuiltIn }
        checkFirstCardCreated(userCards)
        checkStreakAchievement()
        checkCardsCreated(userCards)
        checkCardsRated(allCards)
        checkPerfectAnswers(allCards)
        checkVarietyInRatings(allCards)
        checkRepetitionMaster(allCards)
        checkAllAchievementsUnlocked()
    }

    private suspend fun checkFirstCardCreated(cards: List<Card>) {
        if (cards.isNotEmpty()) {
            unlockAchievement("Новичок")
        }
    }

    private suspend fun checkCardsCreated(cards: List<Card>) {
        when {
            cards.size >= 50 -> unlockAchievement("Карточный магнат")
            cards.size >= 100 -> unlockAchievement("100 карточек!")
        }
    }
    private fun checkStreakAchievement() {
        val currentStreak = streakManager.getCurrentStreak()
        if (currentStreak >= 5 && !isAchievementUnlocked("Серийный ученик")) {
            unlockAchievement("Серийный ученик")
        }
    }


    private suspend fun checkCardsRated(cards: List<Card>) {
        val ratedCount = cards.count { it.rating != null }
        if (ratedCount >= 20) {
            unlockAchievement("Усердный ученик")
        }
    }

    private suspend fun checkPerfectAnswers(cards: List<Card>) {
        val perfectCount = cards.count { it.rating == 5 }
        if (perfectCount >= 10) {
            unlockAchievement("Без ошибок!")
        }
    }

    private suspend fun checkVarietyInRatings(cards: List<Card>) {
        val existingRatings = cards.mapNotNull { it.rating }.distinct()
        if ((0..5).all { it in existingRatings }) {
            unlockAchievement("Разнообразие в оценках")
        }
    }

    fun isAchievementUnlocked(title: String): Boolean {
        return prefs.getBoolean("achievement_$title", false)
    }

    private fun checkAllAchievementsUnlocked() {
        val allUnlocked = allAchievements.all { it.unlocked }
        if (allUnlocked && !isAchievementUnlocked("Легенда обучения")) {
            unlockAchievement("Легенда обучения")
        }
    }
    private suspend fun checkRepetitionMaster(cards: List<Card>) {
        val perfectSession = cards.takeLast(10).all { it.rating == 5 }
        if (perfectSession && !isAchievementUnlocked("Мастер повторений")) {
            unlockAchievement("Мастер повторений")
        }
    }
    fun unlockAchievement(title: String) {
        if (!isAchievementUnlocked(title)) {
            prefs.edit().putBoolean("achievement_$title", true).apply()
            // Обновляем состояние в списке
            allAchievements.find { it.title == title }?.unlocked = true
            showAchievementNotification(title)
        }
    }

    private fun showAchievementNotification(title: String) {
        val achievement = allAchievements.find { it.title == title }
        achievement?.let {
            Toast.makeText(
                context,
                "Ачивка разблокирована: $title",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}