package com.example.educards2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.educards2.Stats.StreakCheckWorker
import com.example.educards2.Stats.StreakManager
import com.example.educards2.database.Achievement
import com.example.educards2.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import java.util.concurrent.TimeUnit
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.educards2.database.AppDatabase
import com.example.educards2.database.CardDao
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var streakManager: StreakManager
    private lateinit var achievementManager: AchievementManager
    lateinit var cardDao: CardDao

   /* private val achievements = listOf(
        Achievement("Новичок", "Создана первая карточка", R.drawable.ic_achievement_locked, false),
        Achievement("Серийный ученик", "5 дней подряд", R.drawable.ic_achievement_locked, false),
        Achievement("Карточный магнат", "Создано 50 карт", R.drawable.ic_achievement_locked, false),
        Achievement("Усердный ученик", "Оценено 20 карт", R.drawable.ic_achievement_locked, false),
        Achievement("Без ошибок", "10 верных ответов подряд", R.drawable.ic_achievement_locked, false),
        Achievement("Разнообразие в оценках", "Использованы все оценки", R.drawable.ic_achievement_locked, false),
        Achievement("Мастер повторений", "Повторие без ошибок", R.drawable.ic_achievement_locked, false),
        Achievement("100 карточек", "Оценено 100 карточек", R.drawable.ic_achievement_locked, false),
        Achievement("Легенда обучения", "Получены все ачивки", R.drawable.ic_achievement_locked, false)
    )*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout
        navigationView = binding.navView
        streakManager = StreakManager(applicationContext)
        cardDao = AppDatabase.getDatabase(this).cardDao()
        achievementManager = AchievementManager(
            this,
            cardDao,
            streakManager
        )
        setupUI()
        updateStreakViews()
        setupWorkManager()
        setupAchievements()
    }

    private fun setupUI() {
        setupToolbar()
        setupNavigationDrawer()
        setupClickListeners()
        setupColors()
        lifecycleScope.launch {
            achievementManager.checkAllAchievements()
            updateAchievementsUI()
        }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            streakManager.updateStreak()
            lifecycleScope.launch {
                achievementManager.checkAllAchievements()
                updateAchievementsUI()
            }
            openActivity(UserCardsActivity::class.java)
        }
    }

    private fun setupWorkManager() {
        val dailyRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<StreakCheckWorker>(
            1,
            TimeUnit.DAYS
        )
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueue(dailyRequest)
    }

    private fun setupAchievements() {
        updateAchievementsUI()
    }

    fun updateAchievementsUI() {
        val gridLayout = binding.achievementsGrid
        gridLayout.removeAllViews()

        achievementManager.allAchievements.forEach { achievement ->
            val achievementView = LayoutInflater.from(this)
                .inflate(R.layout.item_achievement, gridLayout, false)

            val icon = achievementView.findViewById<ImageView>(R.id.achievement_icon)
            val title = achievementView.findViewById<TextView>(R.id.achievement_title)

            title.text = achievement.title

            if (achievement.unlocked) {
                icon.setImageResource(R.drawable.ic_achievement_unlocked)
                achievementView.setOnClickListener {
                    showAchievementPopup(achievement)
                }
            } else {
                icon.setImageResource(R.drawable.ic_achievement_locked)
                achievementView.setOnClickListener {
                    Toast.makeText(this, "Не получено: ${achievement.description}",
                        Toast.LENGTH_SHORT).show()
                }
            }

            gridLayout.addView(achievementView)
        }
    }

    private fun showAchievementPopup(achievement: Achievement) {
        AlertDialog.Builder(this)
            .setTitle(achievement.title)
            .setMessage("Поздравляем! Вы получили ачивку: ${achievement.description}")
            .setPositiveButton("OK", null)
            .show()
    }

    /*private fun loadAchievementsStatus() {
        val prefs = getSharedPreferences("achievements", MODE_PRIVATE)
        achievements.forEach {
            it.unlocked = prefs.getBoolean(it.title, false)
        }
    }*/

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupNavigationDrawer() {
        val toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem.itemId)
            true
        }
    }
    private fun setupColors() {
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_activity))
        binding.btnContinue.setBackgroundColor(ContextCompat.getColor(this, R.color.button_continue))
        binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.app_title_background))
    }

    private fun handleNavigationItemSelected(itemId: Int) {
        when (itemId) {
            R.id.nav_built_in_cards -> openActivity(BuiltInCardsActivity::class.java)
            R.id.nav_lectures -> openActivity(LecturesActivity::class.java)
            R.id.nav_user_cards -> openActivity(UserCardsActivity::class.java)
            R.id.nav_statistics -> openActivity(StatisticsActivity::class.java)
            R.id.nav_archive -> openActivity(ArchivedCardsActivity::class.java)
        }
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun updateStreakViews() {
        val prefs = streakManager.getSharedPreferences()
        binding.tvDays.text = "Дней подряд: ${prefs.getInt("current_streak", 0)}"
        binding.tvRecord.text = "Рекорд: ${prefs.getInt("record_streak", 0)}"
    }

    private fun openActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}