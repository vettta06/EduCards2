package com.example.educards2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.educards2.Stats.StreakManager
import com.example.educards2.database.AppDatabase
import com.example.educards2.database.Card
import com.example.educards2.database.Stats
import com.example.educards2.databinding.ActivityUserCardsBinding
import com.example.educards2.databinding.DialogCardBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class UserCardsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserCardsBinding
    private lateinit var db: AppDatabase
    private var cards = emptyList<Card>()
    private var currentPosition = 0
    private var showingQuestion = true
    private var ratingJob: Job? = null
    private var isAnimating = false
    private lateinit var achievementManager: AchievementManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)

        achievementManager = AchievementManager(
            this,
            AppDatabase.getDatabase(this).cardDao(),
            StreakManager(this)
        )
        setupClickListeners()
        loadCards()

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt("currentPosition")
            showingQuestion = savedInstanceState.getBoolean("showingQuestion")
        }
        val cardId = intent.getLongExtra("CARD_ID", -1L)
        val deckId = intent.getLongExtra("DECK_ID", -1L)

        if (cardId != -1L && deckId != -1L) {
            lifecycleScope.launch {
                val cards = withContext(Dispatchers.IO) {
                    db.cardDao().getCardsByDeckSync(deckId)
                }
                val position = cards.indexOfFirst { card: Card ->
                    card.id == cardId
                }
                if (position != -1) {
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentPosition", currentPosition)
        outState.putBoolean("showingQuestion", showingQuestion)
    }

    private fun setupClickListeners() {
        binding.apply {
            btnAdd.setOnClickListener { showAddDialog() }
            btnEdit.setOnClickListener { showEditDialog() }
            btnDelete.setOnClickListener { deleteCurrentCard() }
            btnPrev.setOnClickListener { showPreviousCard() }
            btnNext.setOnClickListener { showNextCard() }
            btnBack.setOnClickListener { finish() }
            tvCardContent.setOnClickListener { flipCard() }
            btnArchive.setOnClickListener { archiveCurrentCard() }
        }
    }

    private fun loadCards() {
        lifecycleScope.launch {
            db.cardDao().getAllCards().collect { allCards ->
                cards = allCards
                    .filter { !it.isBuiltIn }
                    .filter { !it.isArchived }
                    .filter { it.isDue() }
                currentPosition = when {
                    cards.isEmpty() -> -1
                    currentPosition >= cards.size -> cards.size - 1
                    else -> currentPosition.coerceIn(0, cards.size - 1)
                }
                updateCardDisplay()
            }
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogCardBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle(R.string.new_card)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_add) { _, _ ->
                val question = dialogBinding.etQuestion.text.toString()
                val answer = dialogBinding.etAnswer.text.toString()

                if (question.isBlank() || answer.isBlank()) {
                    Toast.makeText(this@UserCardsActivity,
                        "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val card = Card(
                    deckId = 0,
                    question = question,
                    answer = answer,
                    eFactor = 2.5,
                    nextReview = System.currentTimeMillis(),
                    currentInterval = 0,
                    isBuiltIn = false,
                    isArchived = false
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    db.cardDao().insert(card)
                    val userCardsCount = db.cardDao().getUserCardsCount()

                    if (userCardsCount == 1) {
                        achievementManager.unlockAchievement("Новичок")
                    }

                    withContext(Dispatchers.Main) {
                        loadCards()
                    }
                    achievementManager.checkAllAchievements()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showEditDialog() {
        val currentCard = cards.getOrNull(currentPosition) ?: return
        val dialogBinding = DialogCardBinding.inflate(layoutInflater)
        dialogBinding.etQuestion.setText(currentCard.question)
        dialogBinding.etAnswer.setText(currentCard.answer)
        AlertDialog.Builder(this)
            .setTitle(R.string.edit_card)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val updatedCard = currentCard.copy(
                    question = dialogBinding.etQuestion.text.toString(),
                    answer = dialogBinding.etAnswer.text.toString()
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    db.cardDao().update(updatedCard)
                    loadCards()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun deleteCurrentCard() {
        val currentCard = cards.getOrNull(currentPosition) ?: return
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы точно хотите удалить эту карточку?")
            .setPositiveButton("Да") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.cardDao().delete(currentCard)
                    loadCards()
                }
            }
            .setNegativeButton("Нет", null)
            .setCancelable(true)
            .show()
    }
    private fun showPreviousCard() {
        ratingJob?.cancel()
        if (currentPosition > 0) {
            currentPosition--
            showingQuestion = true
            updateCardDisplay()
        }
    }

    private fun showNextCard() {
        if (cards.isEmpty()) {
            showEmptyCardsDialog()
            return
        }

        if (currentPosition < cards.size - 1) {
            currentPosition++
            saveCardSolved()
        } else {
            showSessionCompleteDialog()
        }

        showingQuestion = true
        updateCardDisplay()
    }
    private fun showSessionCompleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Сессия завершена")
            .setMessage("Все карточки просмотрены!")
            .setPositiveButton("OK") { _, _ ->
                resetSession()
            }
            .setOnCancelListener {
                resetSession()
            }
            .show()
    }

    private fun resetSession() {
        currentPosition = 0
        loadCards()
        updateCardDisplay()
    }

    private fun showEmptyCardsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Нет карточек")
            .setPositiveButton("OK", null)
            .show()
    }
    private fun flipCard() {
        if (isAnimating) return
        isAnimating = true

        binding.cardView.animate()
            .rotationY(90f)
            .setDuration(150)
            .withEndAction {
                showingQuestion = !showingQuestion
                updateCardDisplay()

                binding.cardView.rotationY = -90f
                binding.cardView.animate()
                    .rotationY(0f)
                    .setDuration(150)
                    .withEndAction {
                        isAnimating = false
                        if (!showingQuestion) startRatingTimer()
                    }
                    .start()
            }
            .start()
    }
    private fun startRatingTimer() {
        ratingJob?.cancel()
        ratingJob = lifecycleScope.launch {
            delay(3000)
            showRatingDialog()
        }
    }

   private fun showRatingDialog() {
       val currentCard = cards.getOrNull(currentPosition) ?: return
       val ratingsWithDescriptions = arrayOf(
           "0 - Совсем забыл(а)",
           "1 - Неправильный ответ, правильный вспомнился с трудом",
           "2 - Неправильный ответ, правильный вспомнился легко",
           "3 - Правильный ответ после длительного размышления",
           "4 - Правильный ответ после небольшой заминки",
           "5 - Идеальный ответ"
       )

       AlertDialog.Builder(this)
           .setTitle("Оцените свой ответ")
           .setItems(ratingsWithDescriptions) { _, which ->
               val selectedRating = which
               binding.cardView.animate()
                   .alpha(0f)
                   .setDuration(300)
                   .withEndAction {
                       lifecycleScope.launch(Dispatchers.IO) {
                           saveCardSolved()
                           val oldInterval = currentCard.currentInterval

                           currentCard.apply {
                               rating = selectedRating
                               updateEFactor(selectedRating)
                               updateIntervals(selectedRating, this@UserCardsActivity)
                           }
                           db.cardDao().update(currentCard)

                           val updatedCards = db.cardDao().getAllCardsSync()
                               .filter { !it.isBuiltIn }
                               .filter { !it.isArchived }
                               .filter { it.isDue() }

                           val intervalText = formatIntervalDuration(currentCard.currentInterval)
                           val newCards = updatedCards.filterNot { it.id == currentCard.id }

                           withContext(Dispatchers.Main) {
                               showingQuestion = true
                               cards = newCards
                               currentPosition = 0
                               updateCardDisplay()
                               binding.cardView.alpha = 1f
                               Toast.makeText(
                                   this@UserCardsActivity,
                                   "Оценка: $selectedRating\n" + "Интервал повторения: $intervalText",
                                   Toast.LENGTH_LONG
                               ).show()

                               if (newCards.isEmpty()) {
                                   Toast.makeText(
                                       this@UserCardsActivity,
                                       "Все карточки пройдены!",
                                       Toast.LENGTH_LONG
                                   ).show()
                               }
                           }
                       }
                   }
                   .start()
           }
           .setNegativeButton("Отмена") { _, _ ->
               showingQuestion = true
               updateCardDisplay()
           }
           .show()
   }


    private fun updateCardDisplay() {
        binding.apply {
            if (cards.isEmpty() || currentPosition !in cards.indices) {
                currentPosition = 0
                tvCardContent.text = getString(R.string.no_cards)
                tvCardCount.text = "0/0"
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this@UserCardsActivity,
                        R.color.card_default
                    )
                )
                btnPrev.isEnabled = false
                btnNext.isEnabled = false
                btnArchive.isEnabled = false
                return
            }

            val card = cards[currentPosition]
            tvCardContent.text = if (showingQuestion) card.question else card.answer

            val targetColor = ContextCompat.getColor(
                this@UserCardsActivity,
                if (showingQuestion) R.color.color_question else R.color.color_answer
            )
            cardView.setCardBackgroundColor(targetColor)

            tvCardCount.text = "${currentPosition + 1}/${cards.size}"

            btnPrev.isEnabled = currentPosition > 0
            btnNext.isEnabled = currentPosition < cards.size - 1
            btnArchive.isEnabled = true
            cardView.invalidate()
        }
    }
    private fun formatIntervalDuration(intervalMillis: Long): String {
        if (intervalMillis <= 0) return "сразу"

        val minutes = TimeUnit.MILLISECONDS.toMinutes(intervalMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(intervalMillis)
        val days = TimeUnit.MILLISECONDS.toDays(intervalMillis)

        return when {
            days > 0 -> "$days ${pluralDays(days.toInt())}"
            hours > 0 -> "$hours ${pluralHours(hours.toInt())}"
            minutes > 0 -> "$minutes ${pluralMinutes(minutes.toInt())}"
            else -> "менее минуты"
        }
    }
    private fun pluralDays(n: Int) = when {
        n % 10 == 1 && n % 100 != 11 -> "день"
        n % 10 in 2..4 && n % 100 !in 12..14 -> "дня"
        else -> "дней"
    }
    private fun pluralHours(n: Int) = when {
        n % 10 == 1 && n % 100 != 11 -> "час"
        n % 10 in 2..4 && n % 100 !in 12..14 -> "часа"
        else -> "часов"
    }
    private fun pluralMinutes(n: Int) = when {
        n % 10 == 1 && n % 100 != 11 -> "минута"
        n % 10 in 2..4 && n % 100 !in 12..14 -> "минуты"
        else -> "минут"
    }
    private fun saveCardSolved() {
        val dao = AppDatabase.getDatabase(this).statsDao()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch(Dispatchers.IO) {
            val existing = dao.getStatsByDate(today)
            if (existing != null) {
                dao.insert(existing.copy(cardsSolved = existing.cardsSolved + 1))
            } else {
                dao.insert(Stats(date = today, cardsSolved = 1))
            }
        }
    }
    private fun archiveCurrentCard() {
        if (cards.isEmpty() || currentPosition !in cards.indices) return
        val currentCard = cards[currentPosition]

        lifecycleScope.launch(Dispatchers.IO) {
            db.cardDao().update(currentCard.copy(isArchived = true))
            val updatedCards = db.cardDao().getAllCardsSync()
                .filter { !it.isBuiltIn }
                .filter { !it.isArchived }
                .filter { it.isDue() }

            withContext(Dispatchers.Main) {
                cards = updatedCards
                currentPosition = when {
                    cards.isEmpty() -> -1
                    currentPosition >= cards.size -> cards.size - 1
                    else -> currentPosition.coerceIn(0, cards.size - 1)
                }
                Toast.makeText(
                    this@UserCardsActivity,
                    "Карточка перемещена в архив",
                    Toast.LENGTH_SHORT
                ).show()
                updateCardDisplay()
            }
        }
    }
}