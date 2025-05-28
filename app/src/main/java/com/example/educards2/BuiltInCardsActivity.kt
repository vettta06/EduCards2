package com.example.educards2

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.educards2.database.Card
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.educards2.Stats.StreakManager
import com.example.educards2.database.AppDatabase
import com.example.educards2.database.Deck
import com.example.educards2.database.Stats
import com.example.educards2.database.UserProgress
import com.example.educards2.databinding.ActivityBuiltInCardsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.withContext
import retrofit2.Response

class BuiltInCardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuiltInCardsBinding
    private lateinit var deckAdapter: DeckAdapter
    private var currentDeck: Deck? = null
    private var currentPosition = 0
    private var showingQuestion = true
    private var ratingJob: Job? = null
    private lateinit var db: AppDatabase
    private var cards = emptyList<Card>()
    private lateinit var tvDeckTitle: TextView
    private lateinit var achievementManager: AchievementManager
    private lateinit var streakManager: StreakManager
    private lateinit var apiService: CardApiService
    private var pendingCardId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuiltInCardsBinding.inflate(layoutInflater)
        apiService = RetrofitClient.instance
        setContentView(binding.root)
        db = AppDatabase.getDatabase(this)
        tvDeckTitle = findViewById(R.id.tvDeckTitle)
        achievementManager = AchievementManager(
            this,
            AppDatabase.getDatabase(this).cardDao(),
            StreakManager(this)
        )
        loadDecks()
        setupClickListeners()
        setupDecksRecyclerView()
        requestNotificationPermission()
        val cardId = intent.getLongExtra("CARD_ID", -1L)
        val deckId = intent.getLongExtra("DECK_ID", -1L)
        /*if (cardId != -1L && deckId != -1L) {
            lifecycleScope.launch {
                val deck = withContext(Dispatchers.IO) {
                    db.deckDao().getDeckById(deckId)
                }
                deck?.let {
                    currentDeck = it
                    loadCardsForDeck(it.id)
                    binding.decksRecyclerView.visibility = View.GONE
                    binding.cardsContainer.visibility = View.VISIBLE

                    val cardsInDeck = withContext(Dispatchers.IO) {
                        db.cardDao().getCardsByDeckSync(deckId)
                    }
                    val position = cardsInDeck.indexOfFirst { card: Card ->
                        card.id == cardId
                    }
                    if (position != -1) {
                        currentPosition = position
                        updateCardDisplay()
                    }
                }
            }
        }*/
        if (pendingCardId != -1L && deckId != -1L) {
            lifecycleScope.launch {
                val deck = withContext(Dispatchers.IO) {
                    db.deckDao().getDeckById(deckId)
                }
                deck?.let {
                    currentDeck = it
                    loadCardsForDeck(it.id)
                    binding.decksRecyclerView.visibility = View.GONE
                    binding.cardsContainer.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupDecksRecyclerView() {
        deckAdapter = DeckAdapter { deck ->
            currentDeck = deck
            loadCardsForDeck(deck.id)
            binding.decksRecyclerView.visibility = View.GONE
            binding.cardsContainer.visibility = View.VISIBLE
            tvDeckTitle.text = deck.name
        }

        binding.decksRecyclerView.apply {
            layoutManager = GridLayoutManager(this@BuiltInCardsActivity, 2)
            adapter = deckAdapter
            visibility = View.VISIBLE
        }
    }

    private fun loadDecks() {
        lifecycleScope.launch {
            try {
                val response = apiService.getDecks()
                if (response.isSuccessful) {
                    val decks = response.body() ?: emptyList()
                    deckAdapter.submitList(decks)
                } else {
                    showError("Ошибка загрузки колод: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Ошибка сети: ${e.message}")
            }
        }
    }

   suspend fun getServerTime(): Long? {
       return withContext(Dispatchers.IO) {
           try {
               val response: Response<Long> = apiService.getServerTime()
               if (response.isSuccessful) {
                   response.body()
               } else {
                   Log.e("Card", "Error getting server time: ${response.code()}")
                   null
               }
           } catch (e: IOException) {
               Log.e("Card", "Network error getting server time: ${e.message}")
               null
           }
       }
   }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this@BuiltInCardsActivity, message, Toast.LENGTH_LONG).show()
        }
    }
    private fun setupClickListeners() {
        binding.apply {
            btnPrev.setOnClickListener { showPreviousCard() }
            btnNext.setOnClickListener { showNextCard() }
            cardView.setOnClickListener { flipCard() }
            binding.btnBackToDecks.setOnClickListener {
                binding.cardsContainer.visibility = View.GONE
                binding.decksRecyclerView.visibility = View.VISIBLE
                currentDeck = null
                currentPosition = 0
                showingQuestion = true
                ratingJob?.cancel()
                loadDecks()
            }
            binding.btnBack.setOnClickListener {
                if (currentDeck != null) {
                    currentDeck = null
                    binding.decksRecyclerView.visibility = View.VISIBLE
                    binding.cardsContainer.visibility = View.GONE
                } else {
                    finish()
                }
            }
        }
    }

    private fun showPreviousCard() {
        if (cards.isEmpty()) return
        ratingJob?.cancel()

        if (currentPosition > 0) {
            currentPosition--
        }
        showingQuestion = true
        updateCardDisplay()
    }

    private fun showNextCard() {
        if (cards.isEmpty() || currentPosition !in cards.indices) {
            AlertDialog.Builder(this)
                .setTitle("Сессия завершена")
                .setMessage("Карточки для повторения закончились")
                .setPositiveButton("OK") { _, _ ->
                    resetCardState()
                    currentDeck?.let { loadCardsForDeck(it.id) }
                }
                .setOnCancelListener {
                    resetCardState()
                    updateCardDisplay()
                }
                .show()
            return
        }
        ratingJob?.cancel()
        if (currentPosition < cards.size - 1) {
            showingQuestion = true

            saveCardSolved()
        }
        showingQuestion = true
        if (currentPosition < cards.size - 1) {
            currentPosition++
        }
        updateCardDisplay()
    }

    private fun resetCardState() {
        currentPosition = 0
        showingQuestion = true
        ratingJob?.cancel()
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
                tvCardCount.text = "0/0"
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this@BuiltInCardsActivity,
                        R.color.card_default
                    )
                )
                btnPrev.isEnabled = false
                btnNext.isEnabled = false
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
    // Добавим эти функции в BuiltInCardsActivity
    private fun updateEFactor(currentEFactor: Double, rating: Int): Double {
        val delta = 0.1 - (5 - rating) * (0.08 + (5 - rating) * 0.02)
        return (currentEFactor + delta).coerceIn(1.3, 2.5)
    }

    private fun calculateNextReview(
        rating: Int,
        currentInterval: Long,
        eFactor: Double,
        repetitionCount: Int
    ): Pair<Long, Long> {
        val ONE_MONTH = 30L * 24 * 60 * 60 * 1000
        val now = System.currentTimeMillis()

        val newInterval = when {
            repetitionCount == 0 -> when (rating) {
                0 -> 15L * 60 * 1000
                1 -> 1L * 60 * 60 * 1000
                2 -> 4L * 60 * 60 * 1000
                3 -> 8L * 60 * 60 * 1000
                4 -> 16L * 60 * 60 * 1000
                5 -> 24L * 60 * 60 * 1000
                else -> 24L * 60 * 60 * 1000
            }

            else -> when (rating) {
                0 -> 15L * 60 * 1000
                1 -> (currentInterval * 0.5).toLong()
                2 -> currentInterval
                3 -> (currentInterval * 1.2 * eFactor).toLong()
                4 -> (currentInterval * 1.5 * eFactor).toLong()
                5 -> (currentInterval * 2.0 * eFactor).toLong()
                else -> currentInterval
            }.coerceAtMost(ONE_MONTH)
        }

        val nextReview = now + newInterval
        return Pair(newInterval, nextReview)
    }

    private fun scheduleNotification(
        context: Context,
        cardId: Long,
        deckId: Long,
        question: String,
        isBuiltIn: Boolean,
        nextReview: Long
    ) {
        if (nextReview <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CardNotificationReceiver::class.java).apply {
            putExtra("card_id", cardId)
            putExtra("deck_id", deckId)
            putExtra("card_question", question)
            putExtra("IS_BUILT_IN", isBuiltIn)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            cardId.hashCode(),
            intent,
            flags
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextReview,
                pendingIntent
            )
        } catch (_: Exception) {}
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

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Оцените свой ответ")
            .setItems(ratingsWithDescriptions) { _, which ->
                binding.cardView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                saveCardSolved()

                                val currentProgress = db.userProgressDao().getByCardId(currentCard.id)
                                    ?: UserProgress(
                                        cardId = currentCard.id,
                                        interval = 0,
                                        nextReview = 0,
                                        eFactor = 2.5,
                                        repetitionCount = 0
                                    )

                                // Обновляем eFactor
                                val newEFactor = updateEFactor(
                                    currentEFactor = currentProgress.eFactor,
                                    rating = which
                                )

                                // Рассчитываем новый интервал
                                val (newInterval, nextReview) = calculateNextReview(
                                    rating = which,
                                    currentInterval = currentProgress.interval,
                                    eFactor = newEFactor,
                                    repetitionCount = currentProgress.repetitionCount
                                )

                                // Создаем обновленный прогресс
                                val newProgress = currentProgress.copy(
                                    interval = newInterval,
                                    nextReview = nextReview,
                                    eFactor = newEFactor,
                                    repetitionCount = currentProgress.repetitionCount + 1
                                )

                                // Сохраняем прогресс
                                db.userProgressDao().upsert(newProgress)

                                // Планируем уведомление
                                scheduleNotification(
                                    context = this@BuiltInCardsActivity,
                                    cardId = currentCard.id,
                                    deckId = currentCard.deckId,
                                    question = currentCard.question,
                                    isBuiltIn = currentCard.isBuiltIn,
                                    nextReview = nextReview
                                )

                                // Обновляем список карточек
                                loadCardsForDeck(currentCard.deckId)

                                withContext(Dispatchers.Main) {
                                    // Фильтруем текущую карточку из списка
                                    cards = cards.filterIndexed { i, _ -> i != currentPosition }
                                    currentPosition = currentPosition.coerceAtMost(cards.size - 1)

                                    val intervalText = formatIntervalDuration(newInterval)
                                    Toast.makeText(
                                        this@BuiltInCardsActivity,
                                        "Оценка: $which\nИнтервал: $intervalText",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    updateCardDisplay()
                                    binding.cardView.alpha = 1f

                                    if (cards.isEmpty()) {
                                        Toast.makeText(
                                            this@BuiltInCardsActivity,
                                            "Все карточки пройдены!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    showError("Ошибка: ${e.message}")
                                    binding.cardView.alpha = 1f
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
    private fun loadCardsForDeck(deckId: Long) {
        lifecycleScope.launch {
            try {
                val response = apiService.getCards(deckId)
                if (response.isSuccessful) {
                    val serverCards = response.body() ?: emptyList()

                    val userProgress = db.userProgressDao().getAll().associateBy { it.cardId }

                    val mergedCards = serverCards.map { card ->
                        userProgress[card.id]?.let { progress ->
                            card.copy(
                                currentInterval = progress.interval,
                                nextReview = progress.nextReview,
                                eFactor = progress.eFactor,
                                repetitionCount = progress.repetitionCount
                            )
                        } ?: card
                    }

                    val now = System.currentTimeMillis()
                    val filteredCards = mergedCards.filter { it.isDue(now) }

                    withContext(Dispatchers.Main) {
                        this@BuiltInCardsActivity.cards = filteredCards
                        showingQuestion = true

                        if (pendingCardId != -1L) {
                            val position = filteredCards.indexOfFirst { it.id == pendingCardId }
                            currentPosition = if (position != -1) position else 0
                            pendingCardId = -1L
                        } else {
                            currentPosition = 0
                        }
                        updateCardDisplay()
                    }
                } else {
                    showError("Ошибка загрузки карточек: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Ошибка сети: ${e.message}")
            }
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
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
        val today = System.currentTimeMillis()

        lifecycleScope.launch(Dispatchers.IO) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(today))
            val existing = dao.getStatsByDate(dateStr)
            if (existing != null) {
                dao.insert(existing.copy(cardsSolved = existing.cardsSolved + 1))
            } else {
                dao.insert(Stats(date = dateStr, cardsSolved = 1))
            }
        }
    }

    private fun updateCardOnServer(card: Card) {
        lifecycleScope.launch {
            try {
                apiService.updateCard(card.id, card)
            } catch (e: Exception) {
                Log.e("CardUpdate", "Ошибка обновления: ${e.message}")
            }
        }
    }

}