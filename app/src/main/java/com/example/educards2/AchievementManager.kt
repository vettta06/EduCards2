package com.example.educards2

import android.content.Context
import android.content.SharedPreferences
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.educards2.Stats.StreakManager
import com.example.educards2.database.Achievement
import com.example.educards2.database.Card
import com.example.educards2.database.CardDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AchievementManager(private val context: Context, private val cardDao: CardDao, private val streakManager: StreakManager) {
    private val prefs: SharedPreferences = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)
    private val _achievementsUpdated = MutableLiveData<Boolean>()
    val achievementsUpdated: LiveData<Boolean> = _achievementsUpdated
    var allAchievements = listOf(
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
            "Создано 50 карточек",
            R.drawable.ic_achievement_locked,
            false
        ),
        Achievement(
            "Усердный ученик",
            "Оценено 20 карточек",
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
            "Оценочник",
            "Использованы все оценки",
            R.drawable.ic_achievement_locked,
            false
        ),Achievement(
            "Мастер повторений",
            "Повторение 10 карточек без ошибок",
            R.drawable.ic_achievement_locked,
            false
        ),Achievement(
            "Карточный критик",
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
    init {
        loadAchievementStatuses()
    }
    suspend fun checkAllAchievements() {
        val allCards = cardDao.getAllCardsSync()
        val userCards = allCards.filter { !it.isBuiltIn }
        checkFirstCardCreated()
        checkStreakAchievement()
        checkCardsCreated(userCards)
        checkCardsRated(allCards)
        checkPerfectAnswers(allCards)
        checkVarietyInRatings(allCards)
        checkRepetitionMaster(allCards)
        checkAllAchievementsUnlocked()
    }

    private fun loadAchievementStatuses() {
        // Get current status
        for (achievement in allAchievements) {
            achievement.unlocked = isAchievementUnlocked(achievement.title)
        }
    }
    private fun isAchievementUnlocked(title: String): Boolean {
        return prefs.getBoolean("achievement_$title", false)
    }
    private suspend fun checkFirstCardCreated() {
        val userCardsCount = cardDao.getUserCardsCount()
        if (userCardsCount >= 1 && !isAchievementUnlocked("Новичок")) {
            unlockAchievement("Новичок")
        }
    }
    private suspend fun checkCardsCreated(cards: List<Card>) {
        when {
            cards.size >= 100 -> unlockAchievement("Карточный критик")
            cards.size >= 50 -> unlockAchievement("Карточный магнат")
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
            unlockAchievement("Без ошибок")
        }
    }

    private suspend fun checkVarietyInRatings(cards: List<Card>) {
        val existingRatings = cards.mapNotNull { it.rating }.distinct()
        if ((0..5).all { it in existingRatings }) {
            unlockAchievement("Оценочник")
        }
    }

    private fun checkAllAchievementsUnlocked() {
        val allUnlocked = allAchievements.all { isAchievementUnlocked(it.title) }
        if (allUnlocked && !isAchievementUnlocked("Легенда обучения")) {
            unlockAchievement("Легенда обучения")
        }
    }
    private suspend fun checkRepetitionMaster(cards: List<Card>) {
        val ratedCards = cards.filter { it.rating != 0 }
        if (ratedCards.size >= 10) {
            val perfectSession = ratedCards.takeLast(10).all { it.rating == 5 }
            if (perfectSession && !isAchievementUnlocked("Мастер повторений")) {
                unlockAchievement("Мастер повторений")
            }
        }
    }
    fun unlockAchievement(title: String) {
        if (!isAchievementUnlocked(title)) {
            prefs.edit().putBoolean("achievement_$title", true).apply()
            allAchievements.find { it.title == title }?.unlocked = true
            CoroutineScope(Dispatchers.Main).launch {
                showAchievementToast(title)
            }
            _achievementsUpdated.postValue(true)
        }
    }
    private fun showAchievementToast(title: String) {
        val achievement = allAchievements.find { it.title == title }
        achievement?.let {
            val toast = Toast.makeText(context, "Ачивка разблокирована: $title", Toast.LENGTH_LONG)
            val view = toast.view
            val text = view?.findViewById<TextView>(android.R.id.message)
            text?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_achievement_unlocked, 0, 0, 0)
            text?.compoundDrawablePadding = 16
            toast.show()
        }
    }

    fun resetAchievementsUpdated() {
        _achievementsUpdated.postValue(false)
    }
}