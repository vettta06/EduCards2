package com.example.educards2

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.educards2.database.Card
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.educards2.database.AppDatabase
import com.example.educards2.database.Stats
import com.example.educards2.databinding.ActivityBuiltInCardsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BuiltInCardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuiltInCardsBinding

    private var currentPosition = 0
    private var showingQuestion = true
    private var ratingJob: Job? = null
    private lateinit var db: AppDatabase
    private var cards = emptyList<Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuiltInCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)
        loadCards()
        setupClickListeners()
        requestNotificationPermission()

    }
    private fun loadCards() {
        lifecycleScope.launch {
            db.cardDao().getAllCards().collect { allCards ->
                cards = allCards
                    .filter { it.isBuiltIn }
                    .filter { !it.isArchived }
                    .filter { it.isDue() }

                currentPosition = when {
                    cards.isEmpty() -> -1
                    currentPosition >= cards.size -> cards.size - 1
                    else -> currentPosition.coerceIn(0, cards.size - 1)
                }

                withContext(Dispatchers.Main) {
                    updateCardDisplay()
                }
            }
        }
    }
    private fun setupClickListeners() {
        binding.apply {
            btnPrev.setOnClickListener { showPreviousCard() }
            btnNext.setOnClickListener { showNextCard() }
            cardView.setOnClickListener { flipCard() }
            btnBack.setOnClickListener { finish() }
            btnArchive.setOnClickListener { archiveCurrentCard() }
        }
    }

    private fun showPreviousCard() {
        if (cards.isEmpty()) return
        ratingJob?.cancel()
        if (currentPosition > 0) {
            currentPosition--
            showingQuestion = true
            updateCardDisplay()
        }
    }

    private fun showNextCard() {
        if (cards.isEmpty()) return

        ratingJob?.cancel()

        if (currentPosition < cards.size - 1) {
            currentPosition++
            showingQuestion = true
            updateCardDisplay()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Сессия завершена")
                .setMessage("Вы просмотрели все карточки!")
                .setPositiveButton("OK") { _, _ ->
                    currentPosition = 0
                    showingQuestion = true
                    updateCardDisplay()
                }
                .setOnCancelListener {
                    currentPosition = 0
                    showingQuestion = true
                    updateCardDisplay()
                }
                .show()
        }
        saveCardSolved()
    }

    private fun flipCard() {
        if (cards.isEmpty()) return
        showingQuestion = !showingQuestion
        if (!showingQuestion) {
            ratingJob?.cancel()
            ratingJob = lifecycleScope.launch {
                delay(3000)
                showRatingDialog()
            }
        } else {
            ratingJob?.cancel()
        }

        binding.cardView.animate().withLayer()
            .rotationY(90f)
            .setDuration(150)
            .withEndAction {
                updateCardDisplay()
                binding.cardView.rotationY = -90f
                binding.cardView.animate()
                    .rotationY(0f)
                    .setDuration(150)
                    .start()
            }.start()
    }
    private fun updateCardDisplay() {
        binding.apply {
            if (cards.isEmpty()) {
                tvCardContent.text = "Нет карточек для повторения"
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this@BuiltInCardsActivity,
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
            tvCardCount.text = "${currentPosition + 1}/${cards.size}"

            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    this@BuiltInCardsActivity,
                    if (showingQuestion) R.color.color_question else R.color.color_answer
                )
            )

            btnPrev.isEnabled = currentPosition > 0
            btnNext.isEnabled = currentPosition < cards.size - 1
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
                        currentCard.apply {
                            rating = selectedRating
                            updateEFactor(selectedRating)
                            updateIntervals(selectedRating, this@BuiltInCardsActivity)
                        }
                        lifecycleScope.launch(Dispatchers.IO) {
                            db.cardDao().update(currentCard)
                            loadCards()
                        }
                        showNextCard()
                        binding.cardView.alpha = 1f
                    }
                    .start()

                Toast.makeText(
                    this,
                    "Оценка $selectedRating. Следующий показ: ${formatInterval(selectedRating)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Отмена") { _, _ ->
                showingQuestion = true
                updateCardDisplay()
            }
            .show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }
    private fun formatInterval(rating: Int): String {
        return when (rating) {
            5 -> "1 ${pluralDays(1)}"
            4 -> "12 ${pluralHours(12)}"
            3 -> "4 ${pluralHours(4)}"
            2 -> "2 ${pluralHours(2)}"
            1 -> "1 ${pluralHours(1)}"
            0 -> "15 ${pluralMinutes(15)}"
            else -> error("Недопустимый рейтинг: $rating")
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
                Log.d("Stats", "Updated stats for date $today: ${existing.cardsSolved + 1} cards solved")
            } else {
                dao.insert(Stats(date = today, cardsSolved = 1))
                Log.d("Stats", "Inserted new stats for date $today: 1 card solved")
            }
        }
    }
    private fun archiveCurrentCard() {
        if (cards.isEmpty() || currentPosition !in cards.indices) return
        val currentCard = cards[currentPosition]

        lifecycleScope.launch(Dispatchers.IO) {
            db.cardDao().archiveCard(currentCard.id)
            val updatedCards = db.cardDao().getAllCardsSync()
                .filter { it.isBuiltIn && !it.isArchived && it.isDue() }
            withContext(Dispatchers.Main) {
                cards = updatedCards
                currentPosition = if (cards.isNotEmpty()) 0 else -1
                Toast.makeText(
                    this@BuiltInCardsActivity,
                    "Карточка перемещена в архив",
                    Toast.LENGTH_SHORT
                ).show()
                updateCardDisplay()
            }
        }
    }
}